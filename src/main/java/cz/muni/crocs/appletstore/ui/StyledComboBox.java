package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

public class StyledComboBox<T> extends JComboBox<T> {

    public StyledComboBox() {
        setup();
    }

    public StyledComboBox(ComboBoxModel<T> aModel) {
        super(aModel);
        setup();
    }

    public StyledComboBox(T[] items) {
        super(items);
        setup();
    }

    public StyledComboBox(Vector<T> items) {
        super(items);
        setup();
    }

    private void setup() {
        setRenderer(new StyledItemRenderer<T>());
        setEditor(new StyledItemEditor());
        setUI(new StyledComboBoxUI());
        setPreferredSize(new Dimension(80, 22));
        setEditable(true);
    }
}