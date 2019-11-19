package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.action.CardDetectionRoutine;
import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.ui.BackgroundImgPanel;
import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.ui.Notice;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.ui.GlassPaneBlocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * App main window
 *
 * @author Jiří Horák
 * @version 1.0
 */

public class AppletStore extends JFrame implements BackgroundChangeable {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());
    private static final Logger logger = LoggerFactory.getLogger(AppletStore.class);
    private static final int PREFFERED_WIDTH = 1100;
    private static final int PREFFERED_HEIGHT = 550;

    private volatile boolean windowOpened = true;
    private MainPanel window;
    private Menu menu;
    private GlassPaneBlocker blocker = new GlassPaneBlocker();

    public AppletStore() {
        this(null);
    }

    public AppletStore(Exception fromLoading) {
        logger.info("------- App started --------");

        setup();
        //save options on close & kill routine
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                OptionsFactory.getOptions().save();
                windowOpened = false;
            }
        });
        setBar();
        initComponents(fromLoading);
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

    public synchronized boolean isWindowOpened() {
        return windowOpened;
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

        UIManager.put("MenuBar.borderColor", Color.BLACK);
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
    private void initComponents(Exception fromLoading) {
        setSize(PREFFERED_WIDTH, PREFFERED_HEIGHT);
        window = new MainPanel(this);
        setContentPane(window);

        if (fromLoading != null) {
            fromLoading.printStackTrace();
            logger.error("Store initialization failed: " + fromLoading.getMessage(), fromLoading);
            window.getRefreshablePane().showError(new ErrorPane(textSrc.getString("load_failed"),
                    fromLoading.getLocalizedMessage(), "plug-in-out.png"));
        }

        menu = new Menu(this);
        CardInstance card = CardManagerFactory.getManager().getCard();

        menu.setCard(card == null ? null : card.getDescriptor());
        setJMenuBar(menu);
        setGlassPane(blocker);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        new CardDetectionRoutine(this, OnEventCallBack.empty()).start();
        setVisible(true);
    }

    @Override
    public void updateBackground(BufferedImage image) {
        ((BackgroundImgPanel) getContentPane()).setNewBackground(image);
    }

    @Override
    public void switchEnabled(boolean enabled) {
        if (enabled == isEnabled())
            return;
        setEnabled(enabled);
        getGlassPane().setVisible(!enabled);
        revalidate();
    }
}
