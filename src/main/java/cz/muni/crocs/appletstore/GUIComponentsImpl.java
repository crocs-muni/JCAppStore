package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.iface.OnEventCallBack;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GUIComponentsImpl implements GUIComponents {

    private static OnEventCallBack<Void, Void> callback;

    private BackgroundChangeable backgroundChangeable;
    private CardStatusNotifiable cardStatusNotifiable;
    private Informable informable;
    private Refreshable refreshable;
    private Searchable searchable;
    private StoreWindows storeWindows;

    @Override
    public void init(BackgroundChangeable a, CardStatusNotifiable b, Informable c, Refreshable d, Searchable e, StoreWindows f) {
        backgroundChangeable = a;
        cardStatusNotifiable = b;
        informable = c;
        refreshable = d;
        searchable = e;
        storeWindows = f;
    }

    @Override
    public BackgroundChangeable getBackgroundChangeable() {
        return backgroundChangeable;
    }

    @Override
    public Informable getInformable() {
        return informable;
    }

    @Override
    public CardStatusNotifiable getCardStatusNotifiable() {
        return cardStatusNotifiable;
    }

    @Override
    public Refreshable getRefreshable() {
        return refreshable;
    }

    @Override
    public Searchable getSearchable() {
        return searchable;
    }

    @Override
    public StoreWindows getStoreWindows() {
        return storeWindows;
    }

    @Override
    public OnEventCallBack<Void, Void> defaultActionEventCallback() {
        if (callback == null) {
            if (getBackgroundChangeable() == null || getCardStatusNotifiable() == null || getStoreWindows() == null) {
                throw new IllegalStateException("This component has not been yet initialized.");
            }

            callback = new WorkCallback(
                    new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            getBackgroundChangeable().switchEnabled(false);
                        }
                    }, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getBackgroundChangeable().switchEnabled(true);
                    getCardStatusNotifiable().updateCardState();
                }
            }, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getBackgroundChangeable().switchEnabled(true);
                    getStoreWindows().refreshCardPanel();
                    getCardStatusNotifiable().updateCardState();
                }
            }
            );
        }
        return callback;
    }
}
