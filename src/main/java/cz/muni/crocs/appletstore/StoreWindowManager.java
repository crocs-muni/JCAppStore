package cz.muni.crocs.appletstore;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.util.*;
import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.util.CallBack;
import cz.muni.crocs.appletstore.ui.Warning;
import cz.muni.crocs.appletstore.ui.LoadingPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Applet store logic implementation
 *
 * CallBack >> allows the store to be reloaded in call() method
 * Searchable >> allows the store to act like searchable object
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreWindowManager extends JPanel implements CallBack<Void>, Searchable {

    private static final Logger logger = LogManager.getLogger(StoreWindowManager.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private BackgroundChangeable context;
    private Component currentComponent = null;
    private Searchable store;
    private volatile StoreState state = StoreState.UNINITIALIZED;
    private GridBagConstraints constraints;

    public StoreWindowManager(BackgroundChangeable context) {
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

    public enum StoreState {
        UNINITIALIZED, NO_CONNECTION, WORKING, OK, INSTALLING, REBUILD, FAILED, TIMEOUT
    }

    public synchronized void setStatus(StoreState state) {
        this.state = state;
    }

    public void setLoadingPaneMessage(String msg) {
        if (currentComponent instanceof LoadingPane)
            ((LoadingPane) currentComponent).setMessage(msg);
    }

    @Override
    public Void callBack() {
        setStatus(StoreState.UNINITIALIZED);
        updateGUI();
        return null;
    }

    @Override
    public void showItems(String query) {
        if (state == StoreState.OK) {
            store.showItems(query);
        }
    }

    /**
     * Force the store to update
     */
    public void updateGUI() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::update);
        } else {
            update();
        }
    }

    private void update() {
        switch (state) {
            case OK:
            case WORKING:
            case INSTALLING: //if OK or WORKING, do nothing
                return;
            case REBUILD:
                setStatus(StoreState.OK);
                setupWindow();
                return;
            case TIMEOUT:
                setStatus(StoreState.UNINITIALIZED);
                putNewPane(new ErrorPane(textSrc.getString("E_store_timeout"),
                        "error.png", this), false);
                return;
            case FAILED:
                setStatus(StoreState.UNINITIALIZED);
                putNewPane(new ErrorPane(textSrc.getString("E_store_generic"),
                        "error.png", this), false);
                return;
            case NO_CONNECTION:
                InformerFactory.getInformer().showWarning(textSrc.getString("W_internet"),
                        Warning.Importance.SEVERE, Warning.CallBackIcon.RETRY, this);
                setupWindow();
                return;
            default:
                setStatus(StoreState.WORKING);
                init();
        }
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
                    OptionsFactory.getOptions().addOption(Options.KEY_GITHUB_LATEST_VERSION, result);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
                setStatus(StoreState.TIMEOUT);
            } finally {
                updateGUI();
            }
        }).start();
    }

    /**
     * Remove all panels and show new one (component)
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
        final LoadingPane loadingPane =
                new LoadingPane(textSrc.getString("waiting_internet"));
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
     * Builds the store panel with items from GitHub.
     * If store file missing, displays error
     */
    private void setupWindow() {
        JsonParser parser = new JsonStoreParser();
        List<JsonObject> data;
        try {
            data = parser.getValues();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            setFailed();
            return;
        }

        if (data == null) {
            setFailed();
            return;
        }
        StoreWindowPane store = new StoreWindowPane(data, context);
        this.store = store;
        putNewPane(store, true);
    }

    /**
     * Display store load failed
     */
    private void setFailed() {
        putNewPane(new ErrorPane(textSrc.getString("W_store_loading"),
                "error.png", this), false);
        setStatus(StoreState.UNINITIALIZED);
        FileCleaner.cleanFolder(Config.APP_STORE_DIR);
    }
}