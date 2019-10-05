package cz.muni.crocs.appletstore;

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
     * @param keyTitle key to find in internacionalized bundle
     * @param keyText key to find in internacionalized bundle
     * @param imgNamec custom image to display
     */
    void showError(String keyTitle, String keyText, String imgNamec);

    void showError(String keyTitle, String text, String imgName, LocalizedException cause);
}
