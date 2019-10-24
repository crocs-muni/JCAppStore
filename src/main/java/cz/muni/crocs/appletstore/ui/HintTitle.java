package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;

public class HintTitle extends HintLabel {

    public HintTitle() {
        setFont(OptionsFactory.getOptions().getFont());
    }

    public HintTitle(String labelTitle, String hint) {
        super(labelTitle, hint);
        setFont(OptionsFactory.getOptions().getFont());
    }

    public HintTitle(Icon icon, String hint) {
        super(icon, hint);
        setFont(OptionsFactory.getOptions().getFont());
    }

    public HintTitle(String text, String hint, Icon icon) {
        super(text, hint, icon);
        setFont(OptionsFactory.getOptions().getFont());
    }

    public HintTitle(float size) {
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public HintTitle(String labelTitle, String hint, float size) {
        super(labelTitle, hint);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public HintTitle(Icon icon, String hint, float size) {
        super(icon, hint);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public HintTitle(String text, String hint, Icon icon, float size) {
        super(text, hint, icon);
        setFont(OptionsFactory.getOptions().getFont(size));
    }
}
