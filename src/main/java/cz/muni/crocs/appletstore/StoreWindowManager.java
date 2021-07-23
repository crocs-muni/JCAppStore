package cz.muni.crocs.appletstore;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.util.*;
import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.ui.Notice;
import cz.muni.crocs.appletstore.ui.LoadingPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.lang.module.ModuleDescriptor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.*;

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
    private static final Logger logger = LoggerFactory.getLogger(StoreWindowManager.class);
    private static final ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private Component currentComponent = null;
    private Searchable store;
    private volatile State state = State.UNINITIALIZED;
    private final GridBagConstraints constraints;
    //private final StoreSubMenu submenu;
    private SearchBar searchBar;

    private ScheduledFuture<?> loadingPaneUpdater;

    public StoreWindowManager() {
        setOpaque(false);
        setLayout(new GridBagLayout());
        constraints = new GridBagConstraints();

//        submenu = new StoreSubMenu();
//        submenu.setOnBack(new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                store.showItems(null);
//            }
//        });
    }

    public void redownload() {
        OptionsFactory.getOptions().addOption(Options.KEY_GITHUB_LATEST_VERSION, "");
        setState(State.UNINITIALIZED);
        updateGUI();
    }

//    public void showBackMenu(boolean show) {
//        submenu.setVisible(show);
//    }

    /**
     * Set store state, called from worker
     * @param state state to set
     */
    public synchronized void setState(State state) {
        this.state = state;
    }

    /**
     * Set process mesage, called from worker on initialization
     * @param msg message to show
     */
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

    @Override
    public void registerSearchBar(SearchBar bar) {
        searchBar = bar;
    }

    /**
     * Force the store to update
     */
    public void updateGUI() {
        try {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(this::update);
            } else {
                update();
            }
        } catch (Exception e) {
            logger.error("Failed to load the store: " + e.getMessage(), e);
        }
    }

    private void update() {
        if (loadingPaneUpdater != null && !loadingPaneUpdater.isCancelled()) {
            loadingPaneUpdater.cancel(true);
            loadingPaneUpdater = null;
        }

        try {
            String version = Files.readString(Paths.get(Config.APP_STORE_DIR.getAbsolutePath(), ".version"));
            if (version != null && !version.isEmpty()) {
                if (ModuleDescriptor.Version.parse(Config.VERSION).compareTo(ModuleDescriptor.Version.parse(version)) < 0) {
                    logger.info("Store verson FAIL;" + Config.VERSION);

                    setState(State.UNINITIALIZED);
                    putNewPane(new ErrorPane(textSrc.getString("E_store_jcapp_outdated") + " " + version,
                            "update.png"), false);

                    logger.info("Store TOO OLD;");

                    return;

                }
            } else {
                logger.warn("The store version file is empty. Store might not work correctly.");
            }
        } catch (Exception e) {
            logger.warn("Failed to check the store version. Store might not work correctly.", e);
        }

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
                        "error_white.png", this), false);
                return;
            case FAILED:
                setState(State.UNINITIALIZED);
                putNewPane(new ErrorPane(textSrc.getString("E_store_generic"),
                        "error_white.png", this), false);
                return;
            case INVALID:
                setState(State.UNINITIALIZED);
                putNewPane(new ErrorPane(textSrc.getString("E_store_outdated"),
                        "offline.png", this), false);
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

                if (loadingPaneUpdater != null && !loadingPaneUpdater.isCancelled()) {
                    loadingPaneUpdater.cancel(true);
                    loadingPaneUpdater = null;
                }
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

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        loadingPaneUpdater = executor.scheduleAtFixedRate(() -> {
            loadingPane.update(downloader.getProgress());
            loadingPane.showAbort(abortAction, true);
        }, 50, 300, TimeUnit.MILLISECONDS);
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
        StoreWindowPane store = new StoreWindowPane(this, data);
        store.registerSearchBar(searchBar);
        this.store = store;

        removeAll();
        defaultConstraints();

//        constraints.weighty = 0.001;
//        constraints.gridx = 0;
//        constraints.gridy = 0;
//        submenu.setVisible(false);
//        add(submenu, constraints);
//         constraints.gridy = 0;

        constraints.gridy = 0;
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