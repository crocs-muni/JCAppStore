package cz.muni.crocs.appletstore.help;

import cz.muni.crocs.appletstore.ui.TextField;

import java.util.Locale;
import java.util.ResourceBundle;

public class Cmd extends HelpPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    public Cmd() {
        add(getLabel(textSrc.getString("cmd_title"), 35f), "wrap");
        add(TextField.getTextField(textSrc.getString("cmd_introduction"), "width: 600px", null), "gapleft 10, wrap");

        add(getLabel(textSrc.getString("cmd_browse_title"), 20f), "wrap");
        add(TextField.getTextField(textSrc.getString("cmd_browse"), "width: 600px", null), "gapleft 10, wrap");

        add(getLabel(textSrc.getString("cmd_launch_linux_title"), 20f), "wrap");
        add(TextField.getTextField(textSrc.getString("cmd_launch_linux"), "width: 600px", null), "gapleft 10, wrap");

        add(getLabel(textSrc.getString("cmd_launch_win_title"), 20f), "wrap");
        add(TextField.getTextField(textSrc.getString("cmd_launch_win"), "width: 600px", null), "gapleft 10, wrap");

        add(getLabel(textSrc.getString("cmd_lanuch_Java_title"), 20f), "wrap");
        add(TextField.getTextField(textSrc.getString("cmd_lanuch_Java"), "width: 600px", null), "gapleft 10, wrap");
    }
}