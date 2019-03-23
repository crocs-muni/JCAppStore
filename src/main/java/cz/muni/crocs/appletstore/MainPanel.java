package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.iface.Searchable;
import cz.muni.crocs.appletstore.ui.BackgroundImgPanel;
import cz.muni.crocs.appletstore.ui.Warning;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class MainPanel extends BackgroundImgPanel implements CallBack<Void> {

    /**
     * Layout hierarchy:
     * Main Panel [borderlayout]
     *       leftMenu
     *       content [overlayout]
     *            storePanel
     *            localPanel
     */
    private AppletStore context;
    private LeftMenu leftMenu;

    private LocalWindowPane localPanel;
    private StoreWindowManager storePanel;
    private boolean isLocalPaneDisplayed;

    private Warning warning;

    public MainPanel(AppletStore context) {
        this.context = context;
        createHierarchy();
        Informer.init(this);
    }

    private void createHierarchy() {
        setLayout(new BorderLayout());

        localPanel = new LocalWindowPane(context);
        storePanel = new StoreWindowManager(context);

        /*
    Content panel holds both local and store panels as over layout, switches the visibility
     */
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

    public boolean isLocalPaneDiplayed() {
        return isLocalPaneDisplayed;
    }

    public void setLocalPanelVisible() {
        localPanel.setVisible(true);
        storePanel.setVisible(false);
        isLocalPaneDisplayed = true;
    }

    public void setUpdateStorePaneVisible() {
        localPanel.setVisible(false);
        storePanel.setVisible(true);
        isLocalPaneDisplayed = false;
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

    public void showWarning(String msg, Warning.Importance status, Warning.CallBackIcon icon, CallBack callable) {
        warning = new Warning(msg, status, icon, callable == null ? this : callable);
        add(warning, BorderLayout.NORTH);
        revalidate();

        new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            closeWarning();
        }).start();
    }

    public void closeWarning() {
        if (warning != null) {
            remove(warning);
            warning = null;
        }
        revalidate();
        repaint();
    }

    public LocalWindowPane getLocalPanel() {
        return localPanel;
    }

    @Override
    public Void callBack() {
        closeWarning();
        return null;
    }
}
