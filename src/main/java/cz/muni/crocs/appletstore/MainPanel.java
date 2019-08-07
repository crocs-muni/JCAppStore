package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.BackgroundImgPanel;
import cz.muni.crocs.appletstore.util.InformerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Main panel of the application, holds the left menu and working panes
 * takes care of displaying the information boxes
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class MainPanel extends BackgroundImgPanel implements Informable {

    private LeftMenu leftMenu;
    private LocalWindowPane localPanel;
    private StoreWindowManager storePanel;

    public MainPanel(BackgroundChangeable context) {
        localPanel = new LocalWindowPane(context);
        storePanel = new StoreWindowManager(context);

        createHierarchy();
        InformerFactory.setInformer(this);
    }

    /**
     * Build Swing components
     */
    private void createHierarchy() {
        setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new OverlayLayout(content));
        content.setOpaque(false);
        content.add(localPanel);
        content.add(storePanel);
        leftMenu = new LeftMenu(this);

        setOpaque(false);
        add(leftMenu, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        setLocalPanelVisible();
    }

    /**
     * Switch to local panel
     */
    public void setLocalPanelVisible() {
        localPanel.setVisible(true);
        storePanel.setVisible(false);
    }

    /**
     * Switch to store panel and call store update
     */
    public void setStorePaneVisible() {
        localPanel.setVisible(false);
        storePanel.setVisible(true);
        storePanel.updateGUI(); //always
    }

    /**
     * Get searchable panel: wither store or local panel - depends
     * on where to perform the searching
     * @return currently visible panel
     */
    public Searchable getSearchablePane() {
        return (storePanel.isVisible()) ? storePanel : localPanel;
    }

    public LocalWindowPane getLocalPanel() {
        return localPanel;
    }

    @Override
    public void showInfo(String info) {
        if (info == null || info.isEmpty())
            return;
        leftMenu.addNotification(info);
    }

    @Override
    public void showWarning(JComponent component) {
        add(component, BorderLayout.NORTH);
        revalidate();
//        JOptionPane.showConfirmDialog(this, component);
    }

    @Override
    public void hideWarning(JComponent component) {
        remove(component);
        revalidate();
        repaint();
    }
}
