package cz.muni.crocs.appletstore.util;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

/**
 * Adapter that allows to load web browser on click
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class URLAdapter extends MouseAdapter {
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private static final Logger logger = LoggerFactory.getLogger(URLAdapter.class);
    private final String urlAddress;

    public URLAdapter(String url) {
        urlAddress = url;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            browse(urlAddress);
        } catch (IOException | URISyntaxException ex) {
            logger.warn("Could not open URL + " + urlAddress, ex);
            InformerFactory.getInformer().showMessage(textSrc.getString("E_no_browser"));
        }
    }

    public static void browse(String URL) throws URISyntaxException, IOException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(URL));
        }
    }

    public static void browse(File f) throws IOException, URISyntaxException {
        String path = f.getAbsolutePath();
        if (SystemUtils.IS_OS_WINDOWS) path = path.replaceAll("\\\\", "/");
        browse("file:///" + path);
    }
}
