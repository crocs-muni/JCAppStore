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
 * Better one ugly intersection of references than network of arguments that no one can predict what happens, when
 * one component uses other uninitialized component etc. This is all done once, at one place,
 * easy to both access, debug and implement.
 *
 * some components might want to disable window, search in apps... etc.
 * this distributes the rot GUI utilities among
 *
 * WHEN USING THIS CLASS:
 *  - never rely on having these components initialized when creating a GUI class instance (unless it is being created
 *  after the aplication started).
 *  - always prefer direct request over storing the variable in some class and call it later
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
