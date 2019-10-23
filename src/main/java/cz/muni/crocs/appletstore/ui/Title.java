package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;

public class Title extends JLabel {
    
    public Title(String s, Icon icon, int i) {
        super(s, icon, i);
        setFont(OptionsFactory.getOptions().getTitleFont());
    }

    public Title(String s, int i) {
        super(s, i);
        setFont(OptionsFactory.getOptions().getTitleFont());
    }

    public Title(String s) {
        super(s);
        setFont(OptionsFactory.getOptions().getTitleFont());
    }

    public Title(Icon icon, int i) {
        super(icon, i);
        setFont(OptionsFactory.getOptions().getTitleFont());
    }

    public Title(Icon icon) {
        super(icon);
        setFont(OptionsFactory.getOptions().getTitleFont());
    }

    public Title() {
        setFont(OptionsFactory.getOptions().getTitleFont());
    }

    public Title(String s, Icon icon, float size, int i) {
        super(s, icon, i);
        setFont(OptionsFactory.getOptions().getTitleFont(size));
    }

    public Title(String s, float size, int i) {
        super(s, i);
        setFont(OptionsFactory.getOptions().getTitleFont(size));
    }

    public Title(String s, float size) {
        super(s);
        setFont(OptionsFactory.getOptions().getTitleFont(size));
    }

    public Title(Icon icon, float size, int i) {
        super(icon, i);
        setFont(OptionsFactory.getOptions().getTitleFont(size));
    }

    public Title(Icon icon, float size) {
        super(icon);
        setFont(OptionsFactory.getOptions().getTitleFont(size));
    }

    public Title(float size) {
        setFont(OptionsFactory.getOptions().getTitleFont(size));
    }
}
