package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.CustomButton;
import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.ui.InputHintTextField;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class TabbedPaneSimulator {

    AppletStore context;

    private JPanel container;
    private LeftMenu leftMenu;

    //to switch panes
    private JPanel content;
    public LocalWindowPane localPanel;
    public StoreWindowPane storePanel;

    public TabbedPaneSimulator(AppletStore context) {
        this.context = context;
        createPanes();
    }

    private void createPanes() {
        container = new JPanel();
        container.setLayout(new BorderLayout());

        leftMenu = new LeftMenu(this);
        //switching between localEnvironment and store panes
        localPanel = new LocalWindowPane(context);
        localPanel.setVisible(true); //screen visible by default
        storePanel = new StoreWindowPane(context);
        storePanel.setOpaque(false);
        storePanel.setVisible(false); //not visible by default
        //container for local and store panes
        content = new JPanel();
        content.setBackground(Color.WHITE); //white background
        content.setLayout(new OverlayLayout(content));
        content.add(localPanel);
        content.add(storePanel);

        container.add(leftMenu, BorderLayout.WEST);
        container.add(content, BorderLayout.CENTER);
    }


    public void setLocalPaneVisible() {
        localPanel.setVisible(true);
        storePanel.setVisible(false);
        context.refresh(false);
    }

    public void setStorePaneVisible() {
        localPanel.setVisible(false);
        storePanel.setVisible(true);
        storePanel.init(); //always
    }

    public JPanel get() {
        return container;
    }

    public boolean isLocal() {
        return leftMenu.isLocal();
    }
    //** FORM GENERATED CODE IN A BINARY FILE **//
}
