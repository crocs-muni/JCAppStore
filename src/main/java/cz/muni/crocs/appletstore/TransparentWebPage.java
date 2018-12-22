package cz.muni.crocs.appletstore;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;

import java.lang.reflect.Field;

/**
 * Idea taken from Haryanto at
 * https://stackoverflow.com/questions/12421250/transparent-background-in-the-webview-in-javafx
 * @author Jiří Horák
 * @version 1.0
 */
public class TransparentWebPage {

    private WebView webview;
    private Scene scene;
    private WebEngine webengine;

    public TransparentWebPage(){
        webview = new WebView();
        webengine = webview.getEngine();
        scene = new Scene(webview);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        webengine.documentProperty().addListener(new DocListener());
    }

    class DocListener implements ChangeListener<Document> {
        @Override
        public void changed(ObservableValue<? extends Document> observable, Document oldValue, Document newValue) {
//            try {
//                // Use reflection to retrieve the WebEngine's private 'page' field.
//                Field f = webengine.getClass().getDeclaredField("page");
//                f.setAccessible(true);
//                com.sun.webkit.WebPage page = (com.sun.webkit.WebPage) f.get(webengine);
//                page.setBackgroundColor((new java.awt.Color(0, 0, 0, 0)).getRGB());
//
//            } catch (Exception e) {
//            }
        }
    }

    public WebView get() {
        return webview;
    }

    public Scene getScene() {
        return scene;
    }
}