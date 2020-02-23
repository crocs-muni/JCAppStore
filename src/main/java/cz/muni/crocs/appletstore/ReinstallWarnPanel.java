package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.action.FreeMemoryAction;
import cz.muni.crocs.appletstore.action.JCMemory;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.ui.HtmlText;
import cz.muni.crocs.appletstore.ui.Text;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class ReinstallWarnPanel extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private JCheckBox dontShowAgain;
    public ReinstallWarnPanel() {
        setLayout(new MigLayout());

        dontShowAgain = new JCheckBox(textSrc.getString("reinstall_warn_dont_show_again"));
        final ReinstallWarnPanel self = this;

        add(new HtmlText("<div width=\"600px\">" + textSrc.getString("reinstall_warn") + "</div>"), "wrap");
        add(dontShowAgain, "wrap");
    }

    public boolean userSelectedDontShowAgain() {
        return dontShowAgain.isSelected();
    }
}
