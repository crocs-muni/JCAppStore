package cz.muni.crocs.appletstore.ui;


import javax.swing.JLabel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class HintLabel extends JLabel {

    public String hint;

    public HintLabel() {}

    public HintLabel(String labelTitle, String hint) {
        super(labelTitle);
        this.hint = hint;
    }

    public void setText(String text, String hint) {
        super.setText(text);
        this.hint = hint;
    }
}