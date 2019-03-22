package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.iface.ProcessTrackable;
import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.util.LoaderWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.*;


public class SplashScreen extends JWindow {
    private static final Logger logger = LoggerFactory.getLogger(SplashScreen.class);

    private Timer timer1;
    private int progress = 0;
    private Random r = new Random();
    private ProcessTrackable loader = new LoaderWorker();
    private boolean update;

    private SplashScreen() {
        Container container = getContentPane();
        container.setLayout(null);
        JLabel bg = new JLabel(new ImageIcon("src/main/resources/img/splash.png"));
        bg.setBounds(0, 0, 300, 189);
        add(bg);
        loadProgressBar();
        setBackground(new Color(0, 255, 0, 0));
        setSize(370, 215);
        setLocationRelativeTo(null);
        setVisible(true);
        new Thread((SwingWorker) loader).start();
    }

    private void loadProgressBar() {
        ActionListener al = evt -> {
            if (update && progress < 16) {
                update = false;
                progress++;
            } else {
                update = true;
            }

            repaint();
            if (loader.getProgress() > 15) {
                timer1.stop();
                runMainApp();

                progress = 16;
                repaint();
            }
        };
        timer1 = new Timer(120, al);
        timer1.start();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLACK);
        String numbers = "";
        for (int i = 0; i < progress; i++) {
            numbers += String.valueOf(r.nextInt(10));
            if ((i + 1) % 4 == 0) numbers += "  ";
        }
        g2d.setFont(CustomFont.plain.deriveFont(20f));
        g2d.drawString(numbers, 30, 120);
        g2d.setFont(CustomFont.plain.deriveFont(12f));
        g2d.drawString(loader.getInfo(), 78, 98);
    }

    private void runMainApp() {
        try {
            new AppletStore();
        } catch (Exception e) {
            new FeedbackFatalError("Fatal Error", e.getMessage(), e.getMessage(), true,
                    JOptionPane.QUESTION_MESSAGE, null);
            logger.error("Fatal Error: ", e);
            e.printStackTrace();
        }

        setVisible(false);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SplashScreen::new);
    }
};