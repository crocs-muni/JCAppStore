package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.util.ProcessTrackable;
import cz.muni.crocs.appletstore.util.LoaderWorker;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;
import javax.swing.*;

/**
 * App loading - checks for card readers, initializes basic things
 * needed for app to start & loads settings
 */

public class SplashScreen extends JWindow {
    private static final Logger logger = LoggerFactory.getLogger(SplashScreen.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private Timer timer;
    private int progress = 0;
    private Random r = new Random();
    private ProcessTrackable loader = new LoaderWorker();
    private Font font = new Font("Courier", Font.PLAIN, 14);
    private boolean update;
    private String numbers = "";

    private SplashScreen() {
        Container container = getContentPane();
        container.setLayout(null);
        JLabel bg = new JLabel(new ImageIcon("src/main/resources/img/splash.png")) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.BLACK);
                if (progress > 2) {
                    numbers = numbers.substring(0, progress - 2);
                } else {
                    numbers = "";
                }
                numbers += String.valueOf(r.nextInt(10)) + String.valueOf(r.nextInt(10));
                numbers = numbers.substring(0, progress);

                g2d.setFont(font.deriveFont(20f));
                g2d.drawString(spaced(numbers), 30, 120);
                g2d.setFont(font.deriveFont(12f));
                g2d.drawString(loader.getInfo(), 78, 98);
            }
        };
        bg.setBounds(0, 0, 300, 189);
        add(bg);
        loadProgressBar();
        setBackground(new Color(0, 255, 0, 0));
        setSize(370, 215);
        setLocationRelativeTo(null);
        setVisible(true);
        new Thread(loader).start();
    }

    /**
     * Progress bar in form of card numbers
     */
    private void loadProgressBar() {
        ActionListener al = evt -> {
            if (update && progress < 16) {
                update = false;
                progress++;
            } else {
                update = true;
            }
            revalidate();
            repaint();
            if (loader.getProgress() > 15) {
                timer.stop();
                runMainApp();

                progress = 16;
                repaint();
            }
        };
        timer = new Timer(120, al);
        timer.start();
    }

    private String spaced(String values) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < values.length(); j++) {
            builder.append(values.charAt(j));
            if (j % 4 == 3) builder.append(" ");
        }
        return builder.toString();
    }

    private void runMainApp() {
        try {
            if (SystemUtils.IS_OS_LINUX) {
                //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                if (SystemUtils.IS_OS_MAC) {
                    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "JCAppStore");
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        try {
            new AppletStore();
        } catch (Exception e) {
            e.printStackTrace();
            new FeedbackFatalError(textSrc.getString("reporter"), e.getMessage(), true,
                    JOptionPane.QUESTION_MESSAGE, null);
            logger.error("Fatal Error: ", e);
        }

        setVisible(false);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SplashScreen::new);
    }
};