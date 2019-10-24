package cz.muni.crocs.appletstore.help;

import cz.muni.crocs.appletstore.ui.TextField;

import java.util.Locale;
import java.util.ResourceBundle;

public class AppletUsage extends HelpPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    public AppletUsage() {
        add(getLabel(textSrc.getString("au_title"), 35f), "wrap");
        add(TextField.getTextField(textSrc.getString("au_introduction"), "width: 600px", null), "gapleft 10, wrap");
        add(getLabel(textSrc.getString("au_host_title"), 20f), "wrap");
        add(TextField.getTextField(textSrc.getString("au_host"), "width: 600px", null), "gapleft 10, wrap");
        add(getLabel(textSrc.getString("au_no_host_title"), 20f), "wrap");
        add(TextField.getTextField(textSrc.getString("au_no_host"), "width: 600px", null), "gapleft 10, wrap");
    }
}
