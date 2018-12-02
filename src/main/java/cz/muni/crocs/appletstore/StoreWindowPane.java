package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.util.DownloaderWorkerThread;
import cz.muni.crocs.appletstore.util.LoadingPane;

import cz.muni.crocs.appletstore.util.WorkerThreadResult;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreWindowPane extends JFXPanel {

    private static final Logger logger = LogManager.getLogger(StoreWindowPane.class);
    private AppletStore context;
    //private ErrorPane loading = new ErrorPane(Config.translation.get(62), );
    private ErrorPane error = null;
    private LoadingPane loadingPane = null;
    private StoreState state = StoreState.OK;

    public StoreWindowPane(AppletStore context) {
        this.context = context;
        setOpaque(false);
        setLayout(new BorderLayout());
        //do not init, it will be called after switching the panes
    }

    public enum StoreState {
        DOWNLOADING, OK, INSTALLING
    }

    /**
     * Add loading and start a new thread with 0,5 sec update
     */
    private void addLoading(DownloaderWorkerThread downloader) {
        loadingPane = new LoadingPane();
        add(loadingPane, BorderLayout.CENTER);
        new Thread(() -> {
            try {
                //update while progress < 100
                while (loadingPane.update(downloader.getProgress())) {
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        context.redraw();
    }

//    private void removeLoading() {
//        remove(loadingPane);
//        loadingPane = null;
//    }

    /**
     * Checks the internet connection and the last version downloaded from github
     * if needed, updates the data
     * runs the window setup
     */
    public void init() {
        state = StoreState.DOWNLOADING;

        DownloaderWorkerThread workerThread = new DownloaderWorkerThread();
        //set loading
        addLoading(workerThread);
        //execute download
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<WorkerThreadResult> future = executorService.submit(workerThread);

        WorkerThreadResult[] result = {null};
        new Thread(() -> {
            try {
                result[0] = future.get(30, TimeUnit.SECONDS);//wait for downloading, break after 15 seconds
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
                result[0] = WorkerThreadResult.FAILED;
            } finally {
                switch (result[0]) {
                    case NO_CONNECTION:
                        noConnection();
                        break;
                    case OK:
                        setupWindow();
                        break;
                    case FAILED:
                        //TODO repeat but set recursion depth
                        //init();
                        break;
                }
            }
        }).start();
    }

    private void noConnection() {
        removeAll();
        invalidate();
        //setLayout(new GridBagLayout());
        error = new ErrorPane(4, "wifi_off.png");
        add(error);
    }

    private void setupWindow() {
        removeAll();
        invalidate();
        Platform.runLater(() -> {
            WebView view = new TransparentWebPage().get();
            //WebView view = new WebView();
            WebEngine engine = view.getEngine();
            engine.setJavaScriptEnabled(true);
            String url = "src/main/resources/web/index.html";
            File f = new File(url);
            try {
                engine.load(f.toURI().toURL().toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            engine.getLoadWorker().stateProperty().addListener(
                    new ChangeListener<State>() {
                        public void changed(ObservableValue ov, State oldState, State newState) {
                            if (newState == State.SUCCEEDED) {
                                String[] titles = {"kocka", "prase", "scéna"};
                                String[] images = {"icon.png", "pig.png", "scene.jpg", "1.png", "2.png", "3.png"};
                                for (int i = 0; i < 16; i++) {
                                    engine.executeScript("add(\"" + titles[i % 3] + "\", \"img/" + images[i % 6] + "\")");
                                }
                            }
                        }
                    });
            setScene(view.getScene());
        });
    }



}
