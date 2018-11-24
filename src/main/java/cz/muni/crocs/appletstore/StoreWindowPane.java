package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.util.InternetConnection;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreWindowPane extends JFXPanel {

    private AppletStore context;

    public StoreWindowPane(AppletStore context) {
        this.context = context;
        setOpaque(false);
        init();
    }

    public void init() {
        removeAll();
        if (InternetConnection.isAvailable()) {
            setupWindow();
        } else {
            noConnection();
        }
        //TODO doesn't remove the no readers found error pane on second attempt
        revalidate();
        repaint();
    }

    private void noConnection() {
        setLayout(new GridBagLayout());
        add(new ErrorPane(4, "wifi_off.png"));
    }

    private void setupWindow() {


        Platform.runLater(() -> {
            WebView view = new WebView();
            WebEngine engine = view.getEngine();
            final Stage stage = new Stage();
            engine.getLoadWorker().stateProperty().addListener(
                    new ChangeListener<State>() {
                        public void changed(ObservableValue ov, State oldState, State newState) {
                            if (newState == State.SUCCEEDED) {
                                stage.setTitle(engine.getLocation());
                            }
                        }
                    });

            engine.setJavaScriptEnabled(true);
            setScene(new Scene(view));

            engine.load("https://github.com/JavaCardSpot-dev/JCAppStore");
        });
    }

}
