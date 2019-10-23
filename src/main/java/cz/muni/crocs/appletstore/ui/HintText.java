package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;

public class HintText extends HintLabel {

    public HintText() {
        setFont(OptionsFactory.getOptions().getFont());
    }

    public HintText(String labelTitle, String hint) {
        super(labelTitle, hint);
        setFont(OptionsFactory.getOptions().getFont());
    }

    public HintText(Icon icon, String hint) {
        super(icon, hint);
        setFont(OptionsFactory.getOptions().getFont());
    }

    public HintText(String text, String hint, Icon icon) {
        super(text, hint, icon);
        setFont(OptionsFactory.getOptions().getFont());
    }

    public HintText(float size) {
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public HintText(String labelTitle, String hint, float size) {
        super(labelTitle, hint);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public HintText(Icon icon, String hint, float size) {
        super(icon, hint);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    public HintText(String text, String hint, Icon icon, float size) {
        super(text, hint, icon);
        setFont(OptionsFactory.getOptions().getFont(size));
    }
}
