package cz.muni.crocs.appletstore;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.iface.Searchable;
import cz.muni.crocs.appletstore.ui.CustomFlowLayout;
import cz.muni.crocs.appletstore.ui.CustomScrollBarUI;
import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.ui.StoreItem;
import cz.muni.crocs.appletstore.ui.StoreItemInfo;
import cz.muni.crocs.appletstore.util.Cleaner;
import cz.muni.crocs.appletstore.util.DownloaderWorker;
import cz.muni.crocs.appletstore.util.JSONStoreParser;
import cz.muni.crocs.appletstore.ui.LoadingPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


import static cz.muni.crocs.appletstore.StoreWindowPane.StoreState.*;


/**
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreWindowPane extends JPanel implements Runnable, CallBack, Searchable {

    private static final Logger logger = LogManager.getLogger(StoreWindowPane.class);
    private AppletStore context;
    private Component currentComponent = null;
    private volatile StoreState state = UNINITIALIZED;
    private GridBagConstraints constraints;


    private JPanel storeLayout = new JPanel();
    private JScrollPane storeScroll = new JScrollPane();
    private StoreItemInfo info;
    private ArrayList<StoreItem> items = new ArrayList<>();


    public StoreWindowPane(AppletStore context) {
        this.context = context;
        setOpaque(false);
        setLayout(new GridBagLayout());
        constraints = new GridBagConstraints();
        initLayoutConstraints();
    }

    private void initLayoutConstraints() {
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
    }

    public void setLoadingPaneMessage(int msg) {
        if (currentComponent instanceof LoadingPane)
            ((LoadingPane) currentComponent).setMessage(msg);
    }

    @Override
    public void callBack() {
        setStatus(UNINITIALIZED);
        updateGUI();
    }

    @Override
    public void showItems(String query) {
        if (query.isEmpty()) {
            showPanel(items);
        } else {
            ArrayList<StoreItem> sortedIems = new ArrayList<>();
            for (StoreItem item : items) {
                if (item.getSearchQuery().toLowerCase().contains(query.toLowerCase())) {
                    sortedIems.add(item);
                }
            }
            showPanel(sortedIems);
        }
    }

    public enum StoreState {
        UNINITIALIZED, NO_CONNECTION, WORKING, OK, INSTALLING, REBUILD, FAILED, TIMEOUT
    }

    public synchronized void setStatus(StoreState state) {
        this.state = state;
    }

    public void updateGUI() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this);
        } else {
            run();
        }
    }

    @Override
    public void run() {
        switch (state) {
            case OK:
            case WORKING: //if OK or WORKING, do nothing
                return;
            case NO_CONNECTION:
                setStatus(UNINITIALIZED);
                putNewPane(new ErrorPane(4, "wifi_off.png", this), false);
                return;
            case REBUILD: //just re-init store
                setStatus(OK);
                setupWindow();
                return;
            case TIMEOUT:
                setStatus(UNINITIALIZED);
                putNewPane(new ErrorPane(67, "error.png", this), false);
                return;
            case FAILED:
                setStatus(UNINITIALIZED);
                putNewPane(new ErrorPane(66, "error.png", this), false);
                //TODO repeat but set recursion depth | DO NOT CALL from other thread !!!!!!!!!!!
                //run();
                return;
            default:
                setStatus(WORKING);
                init();
        }
    }

    /**
     * Remove all panels and show new one
     *
     * @param component component to show
     */
    private void putNewPane(Component component, boolean fill) {
        if (fill) constraints.fill = GridBagConstraints.BOTH;
        else constraints.fill = GridBagConstraints.NONE;
        removeAll();
        currentComponent = component;
        add(currentComponent, constraints, 0);
        revalidate();
        repaint();
    }

    /**
     * Add loading and start a new thread with 0,5 sec progress update
     */
    private void addLoading(DownloaderWorker downloader) {
        final LoadingPane loadingPane = new LoadingPane();
        putNewPane(loadingPane, true);
        new Thread(() -> {
            try {
                while (loadingPane.update(downloader.getProgress())) {
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Checks the internet connection and the last version downloaded from github
     * if needed, updates the data
     * runs the window setup
     */
    private void init() {
        //todo check for internet connection every time
        DownloaderWorker workerThread = new DownloaderWorker(this);
        addLoading(workerThread);
        workerThread.execute();
        new Thread(() -> {
            try {
                String result = workerThread.get(200, TimeUnit.SECONDS);
                if (!result.equals("done")) {
                    Config.options.put(Config.OPT_KEY_GITHUB_LATEST_VERSION, result);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                setStatus(TIMEOUT);
            } finally {
                updateGUI();
            }
        }).start();
    }

    private void setupWindow() {
        storeScroll.setOpaque(false);
        storeScroll.getViewport().setOpaque(false);
        storeLayout.setOpaque(false);

        storeScroll.setBorder(BorderFactory.createEmptyBorder());
        //never show horizontal one
        storeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //custom scroll bar design
        storeScroll.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        //speed up scrolling
        storeScroll.getVerticalScrollBar().setUnitIncrement(16);
        storeScroll.getVerticalScrollBar().setOpaque(false);

        storeLayout.setLayout(new CustomFlowLayout(FlowLayout.LEFT, 20, 20));
        storeLayout.setBorder(new EmptyBorder(50, 50, 50, 50));

        try {
            loadStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
        putNewPane(storeScroll, true);
    }

    private boolean loadStore() throws IOException {
        //todo in thread?
        final HashMap<String, JsonObject> data = JSONStoreParser.getValues();
        if (data == null) {
            putNewPane(new ErrorPane(63, "error.png", this), false);
            setStatus(UNINITIALIZED);
            Cleaner.cleanFolder(Config.APP_STORE_DIR);
            return false;
        }

        for (JsonObject dataSet : data.values()) {
            JsonArray versions = dataSet.get(Config.JSON_TAG_VERSION).getAsJsonArray();

            StoreItem item = new StoreItem(dataSet.get(Config.JSON_TAG_TITLE).getAsString(),
                    dataSet.get(Config.JSON_TAG_ICON).getAsString(),
                    dataSet.get(Config.JSON_TAG_AUTHOR).getAsString(),
                    versions.get(versions.size()-1).getAsJsonObject().keySet().iterator().next());
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showInfo(dataSet);
                }
            });
            items.add(item);
        }
        showPanel(items);
        return true;
    }

    private void showInfo(JsonObject dataSet) {
        info = new StoreItemInfo(dataSet);
        storeScroll.setViewportView(info);
    }

    private void showPanel(Collection<StoreItem> sortedItems) {
        //todo empty input -> show dialog nothing found
        storeLayout.removeAll();
        for (StoreItem item : sortedItems) {
            storeLayout.add(item);
        }
        storeLayout.revalidate();
        storeScroll.setViewportView(storeLayout);
    }
}

//    private WebView view;
//    private WebEngine engine;
//    public static final String resources = ("file:///" + Config.APP_STORE_DIR + Config.SEP + "Resources" + Config.SEP)
//            .replaceAll("\\\\", "/");
//
//    private void setupWindow() {
//        Platform.runLater(() -> {
//            view = new TransparentWebPage().get();
//            engine = view.getEngine();
//            engine.setJavaScriptEnabled(true);
//
//            try {
//                if (!loadStore()) {
//                    //todo handle
//                    return;
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                setStatus(UNINITIALIZED);
//                //todo add error
//                return;
//            }
//            //todo check images from jar file
//
//            //remove all errors and loading
//            removeAll();
//            //set webpage visible
//            setScene(view.getScene());
//            setStatus(OK);
//        });
//    }
//
//    private boolean loadStore() throws FileNotFoundException {
//        //todo in thread?
//        final HashMap<String, HashMap<String, String>> data = JSONStoreParser.getDefaultValues();
//        if (data == null) {
//            putNewPane(new ErrorPane(63, "error.png", this), false);
//            setStatus(UNINITIALIZED);
//            Cleaner.cleanFolder(Config.APP_STORE_DIR);
//            return false;
//        }
//        final File f = new File("src/main/resources/web/index.html");
//        try {
//            engine.load(f.toURI().toURL().toString());
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//
//        engine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
//            if (newState == State.SUCCEEDED) {
//                //let the javascript know when user clicks the icon
//                JSObject window = (JSObject) engine.executeScript("window");
//                window.setMember("item", new Bridge());
//
//                data.values().forEach((v) -> engine.executeScript("add(\"" +
//                        v.get(Config.JSON_TAG_TITLE) + "\", \"" + resources +
//                        v.get(Config.JSON_TAG_ICON) + "\", \"" +
//                        v.get(Config.JSON_TAG_VERSION) + "\", \"" +
//                        v.get(Config.JSON_TAG_AUTHOR) + "\")"));
//            }
//        });
//        return true;
//    }
//
//    public class Bridge{
//        public void loadItem() {
//            System.out.println("called");
//            return;
//        }
//    }

