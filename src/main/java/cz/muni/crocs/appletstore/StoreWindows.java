package cz.muni.crocs.appletstore;

/**
 * Iface for application root panels - switch between panels CARD and STORE, toggle logger visibility
 */
public interface StoreWindows {

    void setCardPanelVisible();

    void setStorePanelVisible();

    void refreshStorePanel();

    void refreshCardPanel();

    void toggleLogger();

}
