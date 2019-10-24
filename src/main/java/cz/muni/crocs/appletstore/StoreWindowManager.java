package cz.muni.crocs.appletstore;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.util.*;
import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.util.CallBack;
import cz.muni.crocs.appletstore.ui.Warning;
import cz.muni.crocs.appletstore.ui.LoadingPane;
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
 * ProcessModifiable >> allows the store loading process to give status & result information
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreWindowManager extends JPanel implements CallBack<Void>, Searchable, Store {

    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private OnEventCallBack<Void, Void, Void> callbackOnAction;
    private Component currentComponent = null;
    private Searchable store;
    private volatile State state = State.UNINITIALIZED;
    private GridBagConstraints constraints;

    public StoreWindowManager() {
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

    void setCallbackOnAction(OnEventCallBack<Void, Void, Void> callbackOnAction) {
        this.callbackOnAction = callbackOnAction;
    }

    public synchronized void setState(State state) {
        this.state = state;
    }

    public void setProcessMessage(String msg) {
        if (currentComponent instanceof LoadingPane)
            ((LoadingPane) currentComponent).setMessage(msg);
    }

    @Override
    public Void callBack() {
        setState(State.UNINITIALIZED);
        updateGUI();
        return null;
    }

    @Override
    public void showItems(String query) {
        if (state == State.OK) {
            store.showItems(query);
        }
    }

    @Override
    public void refresh() {
        if (state == State.OK) {
            store.refresh();
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
                setState(State.OK);
                setupWindow();
                return;
            case TIMEOUT:
                setState(State.UNINITIALIZED);
                putNewPane(new ErrorPane(textSrc.getString("E_store_timeout"),
                        "error.png", this), false);
                return;
            case FAILED:
                setState(State.UNINITIALIZED);
                putNewPane(new ErrorPane(textSrc.getString("E_store_generic"),
                        "error.png", this), false);
                return;
            case NO_CONNECTION:
                InformerFactory.getInformer().showWarning(textSrc.getString("W_internet"),
                        Warning.Importance.SEVERE, Warning.CallBackIcon.RETRY, this);
                setupWindow();
                return;
            default:
                setState(State.WORKING);
                init();
        }
    }

    /**
     * Checks the internet connection and the last version downloaded from github
     * if needed, updates the data
     * runs the window setup
     */
    private void init() {
        StoreWorker workerThread = new StoreWorker(this);
        addLoading(workerThread);
        workerThread.execute();

        new Thread(() -> {
            try {
                setState(workerThread.get(200, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
                setState(State.TIMEOUT);
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
    private void addLoading(StoreWorker downloader) {
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
        StoreWindowPane store = new StoreWindowPane(data, callbackOnAction);
        this.store = store;
        putNewPane(store, true);
    }

    /**
     * Display store load failed
     */
    private void setFailed() {
        putNewPane(new ErrorPane(textSrc.getString("W_store_loading"),
                "error.png", this), false);
        setState(State.UNINITIALIZED);
        FileCleaner.cleanFolder(Config.APP_STORE_DIR);
    }
}