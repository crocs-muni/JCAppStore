package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.Terminals;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.iface.Searchable;
import cz.muni.crocs.appletstore.ui.BackgroundImgPanel;
import cz.muni.crocs.appletstore.ui.Warning;
import cz.muni.crocs.appletstore.util.Informer;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class TabbedPaneSimulator extends BackgroundImgPanel {

    private AppletStore context;
    private LeftMenu leftMenu;

    //to switch panes
    private JPanel content;
    LocalWindowPane localPanel;
    StoreWindowPane storePanel;
    private boolean isLocalPaneDiplayed;

    private Warning warning;

    public TabbedPaneSimulator(AppletStore context) {
        this.context = context;
        setLayout(new BorderLayout());
        createPanes();
        Informer.init(this);
    }

    private void createPanes() {

        leftMenu = new LeftMenu(this);
        localPanel = new LocalWindowPane(context);
        //init local panel as it is intended to be visible
        localPanel.updatePanes(Terminals.TerminalState.LOADING);
        storePanel = new StoreWindowPane(context);
        //by default store hidden
        setLocalPanelVisible();
        content = new JPanel();
        content.setBackground(Color.WHITE); //white background
        content.setLayout(new OverlayLayout(content));
        content.setOpaque(false);
        content.add(localPanel);
        content.add(storePanel);

        add(leftMenu, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }

    public boolean isLocalPaneDiplayed() {
        return isLocalPaneDiplayed;
    }

    public void setLocalPanelVisible() {
        localPanel.setVisible(true);
        storePanel.setVisible(false);
        isLocalPaneDiplayed = true;
    }

    public void setUpdateStorePaneVisible() {
        localPanel.setVisible(false);
        storePanel.setVisible(true);
        isLocalPaneDiplayed = false;
        storePanel.run(); //always
    }

    public Searchable getSearchablePane() {
        return (storePanel.isVisible()) ? storePanel : localPanel;
    }

    public boolean isLocal() {
        return leftMenu.isLocal();
    }

    public void showInfo(String info) {
        if (info == null || info.isEmpty())
            return;
        leftMenu.addNotification(info);
    }

    public void showWarning(int translationId, Warning.Importance status, CallBack callable) {
        warning = new Warning(translationId, status, callable);
        add(warning, BorderLayout.NORTH);
        revalidate();
    }

    public void closeWarning() {
        if (warning != null) {
            remove(warning);
            warning = null;
        }
        revalidate();
    }
}
