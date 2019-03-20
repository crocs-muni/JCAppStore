package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.iface.Searchable;
import cz.muni.crocs.appletstore.ui.AbsoluteHorizontalWindowFillLayout;
import cz.muni.crocs.appletstore.ui.BackgroundImgPanel;
import cz.muni.crocs.appletstore.ui.UserLogger;
import cz.muni.crocs.appletstore.ui.Warning;
import cz.muni.crocs.appletstore.util.Informer;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class MainPanel extends BackgroundImgPanel implements CallBack {

    /**
     * Layout hierarchy:
     * Main Panel [overlayout]
     *    userLogger
     *    main [borderlayout]
     *        leftMenu
     *        content [overlayout]
     *            storePanel
     *            localPanel
     */
    private AppletStore context;
    private LeftMenu leftMenu;
    private JPanel loggerContainer;
    private UserLogger userLogger;
    private JPanel main;


    /*
    Content panel holds both local and store panels as over layout, switches the visibility
     */
    private JPanel content;
    LocalWindowPane localPanel;
    StoreWindowPane storePanel;
    private boolean isLocalPaneDisplayed;

    private Warning warning;

    public MainPanel(AppletStore context) {
        this.context = context;

        createHierarchy();
        Informer.init(this);
    }

    private void createHierarchy() {
        AbsoluteHorizontalWindowFillLayout absLayout =
                new AbsoluteHorizontalWindowFillLayout(this, context);
        //setLayout(absLayout);
        setLayout(new OverlayLayout(this));

        localPanel = new LocalWindowPane(context);
        storePanel = new StoreWindowPane(context);

        content = new JPanel();
        content.setLayout(new OverlayLayout(content));
        content.setOpaque(false);
        content.add(localPanel);
        content.add(storePanel);
        leftMenu = new LeftMenu(this);

        main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.add(leftMenu, BorderLayout.WEST);
        main.add(content, BorderLayout.CENTER);

        //by default store hidden
        setLocalPanelVisible();

        userLogger = new UserLogger(context, absLayout, 1, new Point(0, AppletStore.PREFFERED_HEIGHT - 92));
        userLogger.setOpaque(false);

        JPanel p = new JPanel();
        p.setOpaque(false);
        //splitPane.setOneTouchExpandable(true);

        add(main);
        absLayout.addAbsolutePositioned(1, 0, 0);
//        add(userLogger);
//        absLayout.addAbsolutePositioned(0, 0, AppletStore.PREFFERED_HEIGHT - 92);
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
    }

    public void closeWarning() {
        if (warning != null) {
            remove(warning);
            warning = null;
        }
        revalidate();
        repaint();
    }

    @Override
    public void callBack() {
        closeWarning();
    }
}
