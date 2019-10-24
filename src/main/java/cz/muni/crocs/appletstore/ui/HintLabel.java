package cz.muni.crocs.appletstore.ui;


import javax.swing.*;
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

    public HintLabel(Icon icon, String hint) {
        super(icon);
        this.hint = hint;
    }

    public HintLabel(String text, String hint, Icon icon) {
        super(text, icon, JLabel.LEFT);
        this.hint = hint;
    }

    public void setText(String text, String hint) {
        super.setText(text);
        setHint(hint);
    }

    public void setHint(String hint) {
        this.hint = hint;
    }
}