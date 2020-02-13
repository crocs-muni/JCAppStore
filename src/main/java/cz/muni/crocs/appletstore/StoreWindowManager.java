package cz.muni.crocs.appletstore;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.util.*;
import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.util.CallBack;
import cz.muni.crocs.appletstore.ui.Notice;
import cz.muni.crocs.appletstore.ui.LoadingPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Applet store logic implementation
 * <p>
 * CallBack >> allows the store to be reloaded in call() method
 * Searchable >> allows the store to act like searchable object
 * ProcessModifiable >> allows the store loading process to give status & result information
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreWindowManager extends JPanel implements CallBack<Void>, Searchable, Store {
    private static Logger logger = LoggerFactory.getLogger(StoreWindowManager.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private OnEventCallBack<Void, Void> callbackOnAction;
    private Component currentComponent = null;
    private Searchable store;
    private volatile State state = State.UNINITIALIZED;
    private GridBagConstraints constraints;
    private StoreSubMenu submenu;

    public StoreWindowManager(OnEventCallBack<Void, Void> callbackOnAction) {
        this.callbackOnAction = callbackOnAction;

        setOpaque(false);
        setLayout(new GridBagLayout());
        constraints = new GridBagConstraints();

        submenu = new StoreSubMenu();
        submenu.setOnReload(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsFactory.getOptions().addOption(Options.KEY_GITHUB_LATEST_VERSION, "");
                setState(State.UNINITIALIZED);
                updateGUI();
            }
        });
        submenu.setOnBack(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                store.showItems(null);
            }
        });
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
                InformerFactory.getInformer().showInfo(textSrc.getString("W_internet"),
                        Notice.Importance.SEVERE, Notice.CallBackIcon.RETRY, this, Informer.INFINITY);
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

        addLoading(workerThread, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                workerThread.cancel(true);
                setState(State.NO_CONNECTION);
            }
        });
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
        defaultConstraints();
        currentComponent = component;
        add(currentComponent, constraints, 0);
        revalidate();
        repaint();
    }

    private void defaultConstraints() {
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
    }

    /**
     * Add loading and start a new thread with 0,5 sec progress update
     */
    private void addLoading(StoreWorker downloader, AbstractAction abortAction) {
        final LoadingPane loadingPane =
                new LoadingPane(textSrc.getString("waiting_internet"));
        putNewPane(loadingPane, true);
        new Thread(() -> {
            int i = 0;

            try {
                while (loadingPane.update(downloader.getProgress())) {
                    Thread.sleep(300);
                    if (i == 15) loadingPane.showAbort(abortAction);
                    i++;
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
            logger.warn("Failed to load store data, initialization failed", e);
            setFailed();
            return;
        }

        if (data == null) {
            setFailed();
            return;
        }
        StoreWindowPane store = new StoreWindowPane(data, callbackOnAction);
        this.store = store;

        removeAll();
        defaultConstraints();
        constraints.weighty = 0.001;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(submenu, constraints);
        constraints.gridy = 1;
        constraints.weighty = 1.0;

        currentComponent = store;
        add(store, constraints);
        revalidate();
        repaint();
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