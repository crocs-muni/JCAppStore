package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.action.CardDetectionRoutine;
import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.ui.BackgroundImgSplitPanel;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.LoggerConsole;
import cz.muni.crocs.appletstore.util.LoggerConsoleImpl;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.ui.GlassPaneBlocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

/**
 * App main window
 *
 * @author Jiří Horák
 * @version 1.0
 */

public class AppletStore extends JFrame implements BackgroundChangeable {
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private static final Logger logger = LoggerFactory.getLogger(AppletStore.class);
    private static final int PREFFERED_WIDTH = 1100;
    private static final int PREFFERED_HEIGHT = 550;

    private MainPanel window;
    private Menu menu;
    private final GlassPaneBlocker blocker = new GlassPaneBlocker();

    /**
     * Create an application
     */
    public AppletStore() {
        logger.info("------- App started --------");

        setup();
        //save options on close & kill routine
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                OptionsFactory.getOptions().save();
            }
        });
        setBar();
        initComponents();
    }

    public MainPanel getWindow() {
        return window;
    }

    private void setBar() {
        setTitle("JCAppStore");
        setIconImage(new ImageIcon(Config.IMAGE_DIR + "icon.png").getImage());
        //todo uncomment for apple branch
//        Application.getApplication().setDockIconImage(
//                new ImageIcon(Config.IMAGE_DIR + "icon.png").getImage());
    }

    public Menu getMenu() {
        return menu;
    }

    /**
     * Environment and style settings
     */
    private void setup() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        HTMLEditorKit kit = new HTMLEditorKit();
        kit.setStyleSheet(OptionsFactory.getOptions().getDefaultStyleSheet());
        UIManager.put("MenuItem.selectionBackground", Color.WHITE);
        UIManager.put("MenuItem.background", Color.BLACK);
        UIManager.put("Menu.background", Color.BLACK);
        UIManager.put("Menu.selectionBackground", Color.BLACK);
        UIManager.put("MenuBar.border", null);
        UIManager.put("PopupMenu.border", null);

        UIManager.put("JMenuBar.borderColor", Color.BLACK);
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        UIManager.put("ToggleButton.focus", new Color(0, 0, 0, 0));
        UIManager.put("CheckBox.focus", new Color(0, 0, 0, 0));
        UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
        UIManager.put("RadioButton.focus", new Color(0, 0, 0, 0));
        UIManager.put("Slider.focus", new Color(0, 0, 0, 0));
        UIManager.put("ComboBox.focus", new Color(0, 0, 0, 0));
    }

    /**
     * Build Swing components and start routine
     */
    private void initComponents() {
        setSize(PREFFERED_WIDTH, PREFFERED_HEIGHT);

        menu = new Menu();
        // menu.setCard(CardManagerFactory.getManager().getCard());

        setJMenuBar(menu);
        setGlassPane(blocker);

        window = new MainPanel();
        setContentPane(window);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        //better to do one ugly thing on one place than many ugly stuff among all GUI classes
        GUIComponents components = GUIFactory.Components();
        components.init(this, menu, window, window.localPanel, window, window);

        InformerFactory.setInformer(window);
        new CardDetectionRoutine(components.defaultActionEventCallback()).start();
        components.getCardStatusNotifiable().updateCardState();
        setVisible(true);
    }

    @Override
    public void updateBackground(BufferedImage image) {
        ((BackgroundImgSplitPanel) getContentPane()).setNewBackground(image);
    }

    @Override
    public void switchEnabled(boolean enabled) {
        if (enabled == isEnabled()) return;
        if (enabled) blocker.setMessage(textSrc.getString("working"));
        setEnabled(enabled);
        getGlassPane().setVisible(!enabled);
        revalidate();
    }

    @Override
    public void setDisabledMessage(String message) {
        blocker.setMessage(message);
    }

    /**
     * Main Panel
     *  split pane with logger (default hidden) and main app panel (content)
     *
     *  content: switches between two panels (StoreWindows iface)
     *
     *  In AppletStore class because of close ties to this class
     */
    public static class MainPanel extends BackgroundImgSplitPanel implements Informable, Searchable, StoreWindows {
        private JPanel content;
        private final LocalWindowPane localPanel;
        private final StoreWindowManager storePanel;
        private Component current = null;
        private LoggerConsole console;

        /**
         * Create a main panel containing left menu, store, my card panels
         */
        public MainPanel() {
            //there was a problem with focus when using search feature, request focus
            requestFocusInWindow();
            setOneTouchExpandable(true);
            setDividerLocation(150);


            localPanel = new LocalWindowPane();
            storePanel = new StoreWindowManager();

            buildStoreContents();
            buildLogger();
        }

        @Override
        public void toggleLogger() {
            setDividerSize(getBottomComponent() == null ? 15 : 0);
            setBottomComponent(getBottomComponent() == null ? (LoggerConsoleImpl)console : null);
        }

        @Override
        public void setCardPanelVisible() {
            localPanel.setVisible(true);
            storePanel.setVisible(false);
        }

        @Override
        public void setStorePanelVisible() {
            localPanel.setVisible(false);
            storePanel.setVisible(true);
            storePanel.updateGUI(); //always
        }

        @Override
        public void refreshStorePanel() {
            storePanel.refresh();
        }

        @Override
        public void refreshCardPanel() {
            localPanel.refresh();
        }

        @Override
        public void showMessage(String info) {
            if (info == null || info.isEmpty())
                return;
            JOptionPane.showMessageDialog(this,
                    "<html><div width=\"350\">" + info + "</div></html>",
                    textSrc.getString("info"),
                    JOptionPane.QUESTION_MESSAGE,
                    new ImageIcon(Config.IMAGE_DIR + "info.png"));
        }

        @Override
        public void showFullScreenInfo(JPanel pane) {
            localPanel.showError(pane);
        }

        @Override
        public void showInfo(JComponent component) {
            current = component;
            content.add(current, BorderLayout.NORTH);
            content.revalidate();
        }

        @Override
        public void hideInfo() {
            if (current == null) return;
            content.remove(current);
            content.revalidate();
            content.repaint();

            current = null;
        }

        @Override
        public void showItems(String query) {
            getSearchablePane().showItems(query);
        }

        @Override
        public void refresh() {
            getSearchablePane().refresh();
        }

        @Override
        public void registerSearchBar(SearchBar bar) {
            localPanel.registerSearchBar(bar);
            storePanel.registerSearchBar(bar);
        }

        /**
         * Get searchable panel: wither store or local panel - depends
         * on where to perform the searching
         * @return currently visible panel
         */
        private Searchable getSearchablePane() {
            return (storePanel.isVisible()) ? storePanel : localPanel;
        }

        /**
         * Get 'my card' panel
         * @return panel for 'my card'
         */
        private LocalWindowPane getRefreshablePane() {
            return localPanel;
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
            LeftMenu leftMenu = new LeftMenu();

            registerSearchBar(leftMenu);

            setOpaque(false);
            content.add(leftMenu, BorderLayout.WEST);
            content.add(pages, BorderLayout.CENTER);
            content.setMinimumSize(new Dimension(content.getMinimumSize().width, 250));

            setCardPanelVisible();
            setLeftComponent(content);
        }

        private void buildLogger() {
            console = new LoggerConsoleImpl();
            setDividerSize(0);
            setBottomComponent(null);
        }
    }
}
