package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Central GUI-iface intersection
 *
 * this is static, e.g. multiple root windows that implement these interfaces
 * (whose multiple instances are not to be intended anyway!) will not work.
 *
 * some components might want to disable window, search in apps... etc.
 * this distributes the rot GUI utilities among
 */
public interface GUIComponents {

    /**
     * Initialization setup, up until now this feature might throw
     * @param a 1st component BackgroundChangeable
     * @param b 2nd component CardStatusNotifiable
     * @param c 3rd component Informable
     * @param d 4th component Refreshable
     * @param e 5th component Searchable
     * @param f 6th component StoreWindows
     */
    void init(BackgroundChangeable a, CardStatusNotifiable b, Informable c, Refreshable d, Searchable e, StoreWindows f);

    BackgroundChangeable getBackgroundChangeable();

    CardStatusNotifiable getCardStatusNotifiable();

    Informable getInformable();

    Refreshable getRefreshable();

    Searchable getSearchable();

    StoreWindows getStoreWindows();

    OnEventCallBack<Void, Void> defaultActionEventCallback();
}
