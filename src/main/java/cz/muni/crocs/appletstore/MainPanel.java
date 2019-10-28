package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.BackgroundImgPanel;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.OnEventCallBack;

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
    private LocalWindowPane localPanel;
    private StoreWindowManager storePanel;
    private Component current = null;

    public MainPanel(BackgroundChangeable context) {
        localPanel = new LocalWindowPane();
        storePanel = new StoreWindowManager();
        OnEventCallBack<Void, Void> callback = new WorkCallback(context, localPanel);

        localPanel.build(callback);
        storePanel.setCallbackOnAction(callback);

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
        LeftMenu leftMenu = new LeftMenu(this);

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
        add(current, BorderLayout.NORTH);
        revalidate();
    }

    @Override
    public void hideWarning() {
        if (current == null) return;
        remove(current);
        current = null;
        revalidate();
        repaint();
    }
}
