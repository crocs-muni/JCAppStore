package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;

public class Title extends JLabel {

    public Title(String s, Icon icon, int i) {
        super(s, icon, i);
        setFont(OptionsFactory.getOptions().getFont());
    }

    public Title(String s, int i) {
        super(s, i);
        setFont(OptionsFactory.getOptions().getFont());
    }

    public Title(String s) {
        super(s);
        setFont(OptionsFactory.getOptions().getFont());
    }

    public Title(Icon icon, int i) {
        super(icon, i);
        setFont(OptionsFactory.getOptions().getFont());
    }

    public Title(Icon icon) {
        super(icon);
        setFont(OptionsFactory.getOptions().getFont());
    }

    public Title() {
        setFont(OptionsFactory.getOptions().getFont());
    }

    public Title(String s, Icon icon, float size, int i) {
        super(s, icon, i);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public Title(String s, float size, int i) {
        super(s, i);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public Title(String s, float size) {
        super(s);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public Title(Icon icon, float size, int i) {
        super(icon, i);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public Title(Icon icon, float size) {
        super(icon);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public Title(float size) {
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public Title(int style) {
        setFont(OptionsFactory.getOptions().getFont(style));
    }

    public Title(String s, Icon icon, int style, float size, int i) {
        super(s, icon, i);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    public Title(String s, int style, float size, int i) {
        super(s, i);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    public Title(String s, int style, float size) {
        super(s);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    public Title(Icon icon, int style, float size, int i) {
        super(icon, i);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    public Title(Icon icon, int style, float size) {
        super(icon);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    public Title(int style, float size) {
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }
}
