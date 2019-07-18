package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.iface.Informable;
import cz.muni.crocs.appletstore.iface.Searchable;
import cz.muni.crocs.appletstore.ui.BackgroundImgPanel;
import cz.muni.crocs.appletstore.util.InformerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class MainPanel extends BackgroundImgPanel implements Informable {
    private AppletStore context;

    private LeftMenu leftMenu;
    private LocalWindowPane localPanel;
    private StoreWindowManager storePanel;

    public MainPanel(AppletStore context) {
        this.context = context;
        createHierarchy();
        InformerFactory.setInformer(this);
    }

    private void createHierarchy() {
        setLayout(new BorderLayout());

        localPanel = new LocalWindowPane(context);
        storePanel = new StoreWindowManager(context);

        //Content panel holds both local and store panels as over layout, switches the visibility
        JPanel content = new JPanel();
        content.setLayout(new OverlayLayout(content));
        content.setOpaque(false);
        content.add(localPanel);
        content.add(storePanel);
        leftMenu = new LeftMenu(this);

        setOpaque(false);
        add(leftMenu, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        //by default store hidden
        setLocalPanelVisible();
    }

    public void setLocalPanelVisible() {
        localPanel.setVisible(true);
        storePanel.setVisible(false);
    }

    public void setUpdateStorePaneVisible() {
        localPanel.setVisible(false);
        storePanel.setVisible(true);
        storePanel.run(); //always
    }

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
    }

    @Override
    public void hideWarning(JComponent component) {
        remove(component);
        revalidate();
        repaint();
    }
}
