package cz.muni.crocs.appletstore.util;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class URLAdapter extends MouseAdapter {

    private String urlAddress;

    public URLAdapter(String url) {
        urlAddress = url;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(urlAddress));
            } catch (IOException | URISyntaxException e1) {
                e1.printStackTrace();
            }
        }
    }
}
