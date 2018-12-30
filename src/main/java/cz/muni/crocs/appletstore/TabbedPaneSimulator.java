package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.iface.Searchable;
import cz.muni.crocs.appletstore.ui.BackgroundImgPanel;

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

    public TabbedPaneSimulator(AppletStore context) {
        this.context = context;
        setLayout(new BorderLayout());
        createPanes();
        context.checkTerminalsRoutine();
    }

    private void createPanes() {

        leftMenu = new LeftMenu(this);
        //switching between localEnvironment and store panes
        localPanel = new LocalWindowPane(context);
        //once start the pane
        localPanel.updatePanes(context.terminals().getState());
        storePanel = new StoreWindowPane(context);
        //default store hidden
        setUpdateLocalPaneVisible();
        //container for local and store panes
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

    public void setUpdateLocalPaneVisible() {
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
        return storePanel;
    }

    public boolean isLocal() {
        return leftMenu.isLocal();
    }
}
