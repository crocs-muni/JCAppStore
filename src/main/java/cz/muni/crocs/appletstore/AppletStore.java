package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.action.CardDetectionRoutine;
import cz.muni.crocs.appletstore.ui.BackgroundImgSplitPanel;
import cz.muni.crocs.appletstore.util.*;
import cz.muni.crocs.appletstore.ui.GlassPaneBlocker;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.concurrent.*;


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
    private static final int PREFERRED_WIDTH = 1250;
    private static final int PREFERRED_HEIGHT = 650;

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
        buildComponents();
        initComponents();
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
        blocker.setVisible(!enabled);
        revalidate();
    }

    @Override
    public void setDisabledMessage(String message) {
        blocker.setMessage(message);
    }

    private void setBar() {
        setTitle("JCAppStore");

        setIconImage(new ImageIcon(Config.IMAGE_DIR + "icon.png").getImage());

        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            //nice dirty hack because APPLE
            //https://stackoverflow.com/questions/13400926/how-can-i-call-an-os-x-specific-method-for-my-cross-platform-jar/43873734
            Image image = new ImageIcon(Config.IMAGE_DIR + "icon.png").getImage();
            try {
                // Replace: import com.apple.eawt.Application
                String className = "com.apple.eawt.Application";
                Class<?> cls = Class.forName(className);

                // Replace: Application application = Application.getApplication();
                Object application = cls.getDeclaredConstructor().newInstance().getClass().getMethod("getApplication")
                        .invoke(null);

                // Replace: application.setDockIconImage(image);
                application.getClass().getMethod("setDockIconImage", java.awt.Image.class)
                        .invoke(application, image);
            } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException
                    | InstantiationException e) {
                e.printStackTrace();
            }
        }
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
     * Build Swing components
     */
    private void buildComponents() {
        setSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);

        menu = new Menu();
        // menu.setCard(CardManagerFactory.getManager().getCard());

        setJMenuBar(menu);
        setGlassPane(blocker);

        window = new MainPanel();
        setContentPane(window);
    }

    /**
     * Initialize GUI components and start routine. The order call is IMPORTANT!
     * Some components rely on other to be instantiated, all rely on GUIComponents factory.
     */
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        //better to do one ugly thing on one place than many ugly stuff among all GUI classes
        GUIComponents components = GUIFactory.Components();
        components.init(this, menu, window, window.localPanel, window, window);

        InformerFactory.setInformer(components.getInformable());
        components.getCardStatusNotifiable().updateCardState();
        new CardDetectionRoutine(components.defaultActionEventCallback()).start();

        setVisible(true);
        requestFocusInWindow();
    }

    /**
     * Main Panel
     *  split pane with logger (default hidden) and main app panel (content)
     *
     *  content: switches between two panels (StoreWindows iface)
     *
     *  In AppletStore class because of close ties to this class - there are some not-pretty things done, but in only
     *  one - root class. Others are separated by interfaces.
     */
    public static class MainPanel extends BackgroundImgSplitPanel implements Informable, Searchable, StoreWindows {
        private JPanel content;
        private final LocalWindowPane localPanel;
        private final StoreWindowManager storePanel;
        private JComponent lastInfo = null;
        private int lastInfoDuration = 0;
        private LoggerConsole console;

        private final Queue<Tuple<JComponent, Integer>> delayed = new ConcurrentLinkedQueue<>();
        private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        private ScheduledFuture<?> job;

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
        public void showInfoMessage(Object info, String image) {
            if (info instanceof String) {
                info = "<html><div width=\"350\">" + info + "</div></html>";
            }
            JOptionPane.showMessageDialog(this, info, textSrc.getString("info"),
                    JOptionPane.QUESTION_MESSAGE, new ImageIcon(Config.IMAGE_DIR + image));
        }

        @Override
        public void showMessage(String title, Object message, String image) {
            if (message instanceof String) {
                message = "<html><div width=\"350\">" + message + "</div></html>";
            }
            JOptionPane.showMessageDialog(null, message, title,
                    JOptionPane.INFORMATION_MESSAGE, new ImageIcon(Config.IMAGE_DIR + image));
        }

        @Override
        public void showQuestion(String title, Object message, String image) {
            if (message instanceof String) {
                message = "<html><div width=\"350\">" + message + "</div></html>";
            }
            JOptionPane.showMessageDialog(null, message, title,
                    JOptionPane.INFORMATION_MESSAGE, new ImageIcon(Config.IMAGE_DIR + image));
        }

        @Override
        public void showFullScreenInfo(JPanel pane) {
            localPanel.showError(pane);
        }

        @Override
        public void showInfo(JComponent component, int milis) {
            if (lastInfo != null) {
                delayed.add(new Tuple<>(lastInfo, lastInfoDuration));
                job.cancel(true);
                content.remove(lastInfo);
            }

            lastInfo = component;
            lastInfoDuration = milis;
            if (milis > 0) job = executor.schedule(() ->
                    SwingUtilities.invokeLater(this::hideInfo), milis, TimeUnit.MILLISECONDS);

            content.add(lastInfo, BorderLayout.NORTH);
            content.revalidate();
            content.repaint();
        }

        @Override
        public void hideInfo() {
            if (lastInfo == null) return;
            content.remove(lastInfo);
            lastInfo = null;

            Tuple<JComponent, Integer> prev = delayed.poll();
            if (prev != null) {
                showInfo(prev.first, prev.second);
            } else {
                content.revalidate();
                content.repaint();
            }
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
