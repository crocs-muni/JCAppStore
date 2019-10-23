package cz.muni.crocs.appletstore.help;

import cz.muni.crocs.appletstore.ui.TextField;

import java.util.Locale;
import java.util.ResourceBundle;

public class MasterKey extends HelpPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    public MasterKey() {
        add(getLabel(textSrc.getString("mk_title"), 35f), "wrap");
        add(TextField.getTextField(textSrc.getString("mk_intro"), "width: 600px", null), "gapleft 10, wrap");
        add(getLabel(textSrc.getString("mk_title_ini"), 20f), "wrap");
        add(TextField.getTextField(textSrc.getString("mk_ini"), "width: 600px", null), "gapleft 10, wrap");
    }
}
