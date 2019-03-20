package cz.muni.crocs.appletstore;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.iface.Searchable;
import cz.muni.crocs.appletstore.ui.CustomFlowLayout;
import cz.muni.crocs.appletstore.ui.CustomScrollBarUI;
import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.ui.Warning;
import cz.muni.crocs.appletstore.util.FileCleaner;
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
        if (!(state == StoreState.OK)) return;

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
        if (state != NO_CONNECTION)
            context.getWindow().closeWarning();

        switch (state) {
            case OK:
            case WORKING: //if OK or WORKING, do nothing
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
            case NO_CONNECTION:
                context.getWindow().showWarning(Config.translation.get(184), Warning.Importance.SEVERE, Warning.CallBackIcon.RETRY, this);
                setupWindow();
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
            //todo handle, log
        }
        putNewPane(storeScroll, true);
    }

    private boolean loadStore() throws IOException {
        items.clear();
        //todo in thread?
        final HashMap<String, JsonObject> data = JSONStoreParser.getValues();
        if (data == null) {
            putNewPane(new ErrorPane(63, "error.png", this), false);
            setStatus(UNINITIALIZED);
            FileCleaner.cleanFolder(Config.APP_STORE_DIR);
            return false;
        }

        for (JsonObject dataSet : data.values()) {

            StoreItem item = new StoreItem(dataSet.get(Config.JSON_TAG_TITLE).getAsString(),
                    dataSet.get(Config.JSON_TAG_ICON).getAsString(),
                    dataSet.get(Config.JSON_TAG_AUTHOR).getAsString(),
                    dataSet.get(Config.JSON_TAG_LATEST).getAsString());
            item.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        info = new StoreItemInfo(dataSet, this);
        storeScroll.setViewportView(info);
    }

    private void showPanel(Collection<StoreItem> sortedItems) {
        storeLayout.removeAll();
        if (sortedItems.size() == 0) {
            try {
                storeLayout.add(new StoreItem(Config.translation.get(113), "no_results.png", "", ""));
            } catch (IOException e) {
                e.printStackTrace();
                //todo handle, log
            }
        } else {
            for (StoreItem item : sortedItems) {
                storeLayout.add(item);
            }
        }
        storeLayout.revalidate();
        storeScroll.setViewportView(storeLayout);
    }
}