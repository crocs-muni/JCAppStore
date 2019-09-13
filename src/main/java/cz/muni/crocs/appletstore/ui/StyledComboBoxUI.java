package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class StyledComboBoxUI extends BasicComboBoxUI {

    @Override
    protected JButton createArrowButton() {
        return new JButton() {
            public int getWidth() {
                return 0;
            }
        };
    }
}
