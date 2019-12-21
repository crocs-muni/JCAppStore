package cz.muni.crocs.appletstore.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Adapter that allows to load web browser on click
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class URLAdapter extends MouseAdapter {
    private static Logger logger = LoggerFactory.getLogger(URLAdapter.class);
    private String urlAddress;

    public URLAdapter(String url) {
        urlAddress = url;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(urlAddress));
            } catch (IOException | URISyntaxException ex) {
                ex.printStackTrace();
                logger.warn("Could not open URL + " + urlAddress, ex);
            }
        }
    }
}
