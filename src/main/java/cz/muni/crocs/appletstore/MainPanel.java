package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.BackgroundImgPanel;
import cz.muni.crocs.appletstore.util.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Main panel of the application, holds the left menu and working panes
 * takes care of displaying the information boxes
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class MainPanel extends BackgroundImgPanel implements Informable {

    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());
    private JPanel content;
    private LocalWindowPane localPanel;
    private StoreWindowManager storePanel;
    private Component current = null;
    private LoggerConsole console;

    public MainPanel(BackgroundChangeable context) {
        setOneTouchExpandable(true);
        setDividerLocation(150);

        localPanel = new LocalWindowPane();
        storePanel = new StoreWindowManager();
        OnEventCallBack<Void, Void> callback = new WorkCallback(context, localPanel);

        localPanel.build(callback);
        storePanel.setCallbackOnAction(callback);

        buildStoreContents();
        buildLogger();
        InformerFactory.setInformer(this);
    }

    public void toggleLogger() {
        setDividerSize(getBottomComponent() == null ? 5 : 0);
        setBottomComponent(getBottomComponent() == null ? (LoggerConsoleImpl)console : null);
    }

    /**
     * Build store, upper part of the split pane
     */
    private void buildStoreContents() {
        content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        JPanel pages = new JPanel();
        pages.setLayout(new OverlayLayout(pages));
        pages.setOpaque(false);
        pages.add(localPanel);
        pages.add(storePanel);
        LeftMenu leftMenu = new LeftMenu(this);

        setOpaque(false);
        content.add(leftMenu, BorderLayout.WEST);
        content.add(pages, BorderLayout.CENTER);

        setLocalPanelVisible();
        setLeftComponent(content);
    }

    void buildLogger() {
        console = new LoggerConsoleImpl();
        setRightComponent(null);
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

    public LocalWindowPane getRefreshablePane() {
        return localPanel;
    }

    @Override
    public void showInfo(String info) {
        if (info == null || info.isEmpty())
            return;
        JOptionPane.showMessageDialog(this,
                "<html><div width=\"350\">" + info + "</div></html>",
                textSrc.getString("info"),
                JOptionPane.QUESTION_MESSAGE,
                new ImageIcon(Config.IMAGE_DIR + "info.png"));
    }

    @Override
    public void showWarning(JComponent component) {
        current = component;
        content.add(current, BorderLayout.NORTH);
        content.revalidate();
    }

    @Override
    public void hideWarning() {
        if (current == null) return;
        content.remove(current);
        content.revalidate();
        content.repaint();

        current = null;
    }
}
