package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ResourceBundle;


/**
 * Loading panel, used when downloading the store
 * rectangle - loading bar used
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LoadingPane extends JPanel {
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private int abortDelay = 15;

    private final int width = 300;
    private final int height = 5;
    private int progress = 0;
    private String message;
    private final Rectangle outline = new Rectangle(-(width/2), -(height/2), width, height);

    private final GridBagConstraints constraints;

    /**
     * Create a loading panel
     * @param initialMsg initial message to display
     */
    public LoadingPane(String initialMsg) {
        setLayout(new GridBagLayout());
        setOpaque(false);
        this.message = initialMsg;
        this.constraints = new GridBagConstraints();
        this.constraints.insets = new Insets(80, 0, 0, 0);
    }

    /**
     * Update message on progress
     * @param msg message to show
     */
    public void setMessage(String msg) {
        this.message = msg;
    }

    /**
     * Update graphic on progress
     * @param value progress value, between 0 and 100
     */
    public boolean update(int value) {
        progress = value;
        return progress <= 100;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.translate(this.getWidth() / 2, this.getHeight() / 2);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(Color.WHITE);
        graphics2D.draw(outline);

        Rectangle inline = new Rectangle(-(width/2), -(height/2), width * progress / 100, height);
        graphics2D.draw(inline);
        graphics2D.fill(inline);
        graphics2D.setFont(OptionsFactory.getOptions().getTitleFont(Font.BOLD, 15f));
        if (0 < progress && progress < 100) {
            graphics2D.drawString(message + progress + "%", -(width/2), -(height/2) - 20);
        } else {
            graphics2D.drawString(message,  -(width/2), -(height/2) - 20);
        }
    }

    public void showAbort(AbstractAction abstractAction, boolean delayed) {
        if (delayed) {
            abortDelay -= 1;
            if (abortDelay > 0) return;
        }

        removeAll();
        JButton abort = new JButton(abstractAction);
        abort.setText(textSrc.getString("abort"));
        abort.setForeground(Color.WHITE);
        abort.setBorder(new CompoundBorder(new LineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(3, 15, 3, 15)));
        abort.setFont(OptionsFactory.getOptions().getFont(13f));
        abort.setUI(new CustomButtonUI());
        abort.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        abort.setOpaque(false);
        add(abort, constraints);
        revalidate();
        repaint();
    }
}

