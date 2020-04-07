package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * Custom combo box style
 *
 * @author Jiří Horák
 * @version 1.0
 * @param <T> type of the item within combo box
 */
public class StyledComboBox<T> extends JComboBox<T> {

    /**
     * Create a combo box
     */
    public StyledComboBox() {
        setup();
    }

    /**
     * Create a combo box with custom model
     * @param aModel model for inner items
     */
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