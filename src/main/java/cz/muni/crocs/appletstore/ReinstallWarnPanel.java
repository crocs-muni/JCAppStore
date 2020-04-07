package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.HtmlText;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * Panel to display with popup when store attempts to reinstall applet
 *
 * @author Jiří Horák
 * @version 1.0
 */
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

    /**
     * Check if user blocked this message
     * @return true if blocked next time - dont show anymore
     */
    public boolean userSelectedDontShowAgain() {
        return dontShowAgain.isSelected();
    }
}
