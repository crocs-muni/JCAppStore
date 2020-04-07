package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * Custom buttons style for left menu - store/my card
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LeftMenuButton extends JButton {

    private Color choosedButtonBG = new Color(255, 255, 255, 60);
    private Color emptyBG = new Color(0, 0, 0 ,0);

    private CompoundBorder innerChoosed = BorderFactory.createCompoundBorder(
            new MatteBorder(new Insets(0, 5,0 , 0), Color.BLACK),
            new EmptyBorder(new Insets(4, 8,4 , 4)));
    private CompoundBorder choosedBorder = BorderFactory.createCompoundBorder(
            innerChoosed,
            new EmptyBorder(new Insets(8, 8,8, 8)));
    private EmptyBorder defaultBorder = new EmptyBorder(new Insets(12, 21,12, 12));

    /**
     * Create a button
     * @param imgName image to display
     * @param text text to display
     * @param isTitle whether to use title or default font
     */
    public LeftMenuButton(String imgName, String text, boolean isTitle) {
        super(text, new ImageIcon("src/main/resources/img/" + imgName));
        setOpaque(false);
        setUI(new CustomButtonUI());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(isTitle ? OptionsFactory.getOptions().getTitleFont() : OptionsFactory.getOptions().getFont());
        setHorizontalAlignment(SwingConstants.LEFT);
        setMinimumSize(new Dimension(Integer.MAX_VALUE, 35));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
    }

    /**
     * Set "selected" style
     * @param isSelected true if selected
     */
    public void setSelectedBorder(boolean isSelected) {
        setBorder(isSelected ? choosedBorder : defaultBorder);
    }

    /**
     * Set "selected" background style
     * @param isSelected true if selected
     */
    public void setSelectedBackground(boolean isSelected) {
        setBackground(isSelected ? choosedButtonBG : emptyBG);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        g.setColor( getBackground() );
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }
}
