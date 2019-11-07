package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import cz.muni.crocs.appletstore.card.Terminals;
import cz.muni.crocs.appletstore.ui.BackgroundImgPanel;
import cz.muni.crocs.appletstore.ui.Warning;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.ui.GlassPaneBlocker;
import jnasmartcardio.Smartcardio;
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
    private void initComponents() {
        setSize(PREFFERED_WIDTH, PREFFERED_HEIGHT);
        window = new MainPanel(this);
        setContentPane(window);

        menu = new Menu(this);
        menu.setCard(CardManagerFactory.getManager().getCardDescriptor());
        setJMenuBar(menu);
        setGlassPane(blocker);

        //start routine
        checkTerminalsRoutine();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Looking for terminals present once a 2 sec
     */
    private void checkTerminalsRoutine() {
        CardManager manager = CardManagerFactory.getManager();

        new Thread(() -> {
            int counter = 0;
            logger.info("------- Routine started -------");
            while (windowOpened) {
                try {
                    int result = manager.needsCardRefresh();

                    if (manager.getTerminalState() == Terminals.TerminalState.NO_SERVICE) {
                        SwingUtilities.invokeLater(() -> InformerFactory.getInformer().showWarningToClose(
                                textSrc.getString("H_service"), Warning.Importance.FATAL, 20000));
                        logger.info("[ROUTINE] Card routine detection killed: Smart card service manager offline.");
                        window.getRefreshablePane().refresh();
                        break;
                    }

                    if (result > 0) {
                        if (result == 2) {
                            try {
                                SwingUtilities.invokeLater(() -> switchEnabled(false));
                                manager.loadCard();
                            } catch (LocalizedCardException e) {
                                e.printStackTrace();
                                window.getRefreshablePane().showError("E_loading_failed",
                                        "CARD: " + manager.getLastCardDescriptor() + "<br>",
                                        "announcement_white.png", e);
                                continue;
                            } finally {
                                SwingUtilities.invokeLater(() -> switchEnabled(true));
                            }
                        }

                        SwingUtilities.invokeLater(() -> {
                            if (result == 2) {
                                window.getRefreshablePane().refresh();
                                menu.setCard(manager.getCardDescriptor());
                            }

                            menu.resetTerminalButtonGroup();
                        });
                    }
                    counter = 0;
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                    counter++;
                    window.getRefreshablePane().refresh();
                    if (counter > 10) {
                        logger.info("[ROUTINE] Terminal routine killed after 10 failures.", e);
                        SwingUtilities.invokeLater(() -> InformerFactory.getInformer().showWarningToClose(
                                textSrc.getString("H_routine"), Warning.Importance.FATAL, 20000));
                        break;
                    } else {
                        logger.info("[ROUTINE] Terminal routine caught an error: " + e.getMessage() +
                                ". The routine continues for: " + counter, e);
                    }
                }
            }
        }).start();
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
