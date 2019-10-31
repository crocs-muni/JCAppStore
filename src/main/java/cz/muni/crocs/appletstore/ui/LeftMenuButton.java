package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LeftMenuButton extends JButton {

    private CompoundBorder innerChoosed = BorderFactory.createCompoundBorder(
            new MatteBorder(new Insets(0, 5,0 , 0), Color.BLACK),
            new EmptyBorder(new Insets(4, 8,4 , 4)));
    private CompoundBorder choosedBorder = BorderFactory.createCompoundBorder(
            innerChoosed,
            new EmptyBorder(new Insets(8, 8,8, 8)));
    private EmptyBorder defaultBorder = new EmptyBorder(new Insets(12, 21,12, 12));

    public LeftMenuButton(String imgName, boolean isTitle) {
        super();
        setUI(new CustomButtonUI());
        setFont(isTitle ? OptionsFactory.getOptions().getTitleFont() : OptionsFactory.getOptions().getFont());
        setHorizontalAlignment(SwingConstants.LEFT);
        setMinimumSize(new Dimension(Integer.MAX_VALUE, 35));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        //setOpaque(false);
        //setContentAreaFilled(false);
        setIcon(new ImageIcon("src/main/resources/img/" + imgName));
    }

    public void setBorder(boolean isDefaultChoosed) {
        setBorder((isDefaultChoosed) ? choosedBorder : defaultBorder);
    }

//    @Override
//    protected void paintComponent(Graphics g) {
//        if (getModel().isPressed()) {
//            //on click
//        } else if (getModel().isRollover()) {
//            //g.setColor(hoverBackgroundColor);
//        } else {
//            //when not clicked
//        }
//    }

}
