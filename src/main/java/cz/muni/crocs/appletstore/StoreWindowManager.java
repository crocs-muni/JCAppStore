package cz.muni.crocs.appletstore;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.iface.Searchable;
import cz.muni.crocs.appletstore.sources.Options;
import cz.muni.crocs.appletstore.sources.OptionsFactory;
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
import java.awt.*;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreWindowManager extends JPanel implements Runnable, CallBack<Void>, Searchable {

    private static final Logger logger = LogManager.getLogger(StoreWindowManager.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private AppletStore context;
    private Component currentComponent = null;
    private Searchable store;
    private volatile StoreState state = StoreState.UNINITIALIZED;
    private GridBagConstraints constraints;

    public StoreWindowManager(AppletStore context) {
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
                context.getWindow().showWarning(textSrc.getString("W_internet"),
                        Warning.Importance.SEVERE, Warning.CallBackIcon.RETRY, this);
                setupWindow();
                return;
            default:
                setStatus(StoreState.WORKING);
                init();
        }
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
                setStatus(StoreState.TIMEOUT);
            } finally {
                updateGUI();
            }
        }).start();
    }

    private void setupWindow() {
        List<JsonObject> data;
        try {
            data = JSONStoreParser.getValues();
        } catch (FileNotFoundException e) {
            setFailed();
            return;
        }

        if (data == null) {
            setFailed();
            return;
        }
        StoreWindowPane store = new StoreWindowPane(data);
        this.store = store;
        putNewPane(store, true);
    }

    private void setFailed() {
        putNewPane(new ErrorPane(textSrc.getString("W_store_loading"),
                "error.png", this), false);
        setStatus(StoreState.UNINITIALIZED);
        FileCleaner.cleanFolder(Config.APP_STORE_DIR);
    }
}