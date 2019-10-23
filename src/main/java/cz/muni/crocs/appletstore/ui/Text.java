package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;

public class Text extends JLabel {

    public Text(String s, Icon icon, int i) {
        super(s, icon, i);
        setFont(OptionsFactory.getOptions().getFont());
    }

    public Text(String s, int i) {
        super(s, i);
        setFont(OptionsFactory.getOptions().getFont());
    }

    public Text(String s) {
        super(s);
        setFont(OptionsFactory.getOptions().getFont());
    }

    public Text(Icon icon, int i) {
        super(icon, i);
        setFont(OptionsFactory.getOptions().getFont());
    }

    public Text(Icon icon) {
        super(icon);
        setFont(OptionsFactory.getOptions().getFont());
    }

    public Text() {
        setFont(OptionsFactory.getOptions().getFont());
    }

    public Text(String s, Icon icon, float size, int i) {
        super(s, icon, i);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public Text(String s, float size, int i) {
        super(s, i);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public Text(String s, float size) {
        super(s);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public Text(Icon icon, float size, int i) {
        super(icon, i);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public Text(Icon icon, float size) {
        super(icon);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public Text(float size) {
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public Text(int style) {
        setFont(OptionsFactory.getOptions().getFont(style));
    }

    public Text(String s, Icon icon, int style, float size, int i) {
        super(s, icon, i);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    public Text(String s, int style, float size, int i) {
        super(s, i);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    public Text(String s, int style, float size) {
        super(s);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    public Text(Icon icon, int style, float size, int i) {
        super(icon, i);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    public Text(Icon icon, int style, float size) {
        super(icon);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    public Text(int style, float size) {
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }
}
