package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.util.LocalizedException;

import javax.swing.*;

/**
 * Class enables to rebuild the panel
 */
public interface Refreshable {

    /**
     * Refresh the contents of panel
     */
    void refresh();

    /**
     * Show error with title and error
     */
    void showError(JPanel pane);
    void showError(String keyTitle, String keyText, String imgNamec);
    void showError(String keyTitle, String text, String imgName, LocalizedException cause);
}
