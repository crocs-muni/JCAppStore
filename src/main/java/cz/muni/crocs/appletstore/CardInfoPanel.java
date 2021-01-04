package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.action.FreeMemoryAction;
import cz.muni.crocs.appletstore.action.applet.JCMemory;
import cz.muni.crocs.appletstore.ui.HtmlText;
import cz.muni.crocs.appletstore.ui.Notice;
import cz.muni.crocs.appletstore.ui.Text;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.sql.Ref;
import java.util.ResourceBundle;

/**
 * Panel to show card memory info
 * used as popup
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardInfoPanel extends JPanel {
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    /**
     * Crate a card info panel
     */
    public CardInfoPanel() {
        setLayout(new MigLayout());
        add(new JLabel(textSrc.getString("working")));
        final CardInfoPanel self = this;

        if (CardManagerFactory.getManager().isCard()) {
            BackgroundChangeable changeable = GUIFactory.Components().getBackgroundChangeable();
            Refreshable refreshable = GUIFactory.Components().getRefreshable();

            new FreeMemoryAction(new OnEventCallBack<>() {
                @Override
                public void onStart() {
                    changeable.switchEnabled(false);
                }

                @Override
                public void onFail() {
                    changeable.switchEnabled(true);
                    refreshable.refresh();
                }

                @Override
                public Void onFinish() {
                    changeable.switchEnabled(true);
                    refreshable.refresh();
                    return null;
                }

                @Override
                public Void onFinish(byte[] apduData) {
                    changeable.switchEnabled(true);
                    self.removeAll();
                    if (apduData == null) {
                        self.add(new JLabel(new ImageIcon(Config.IMAGE_DIR + "announcement.png")),
                                "align center, wrap");
                        self.add(new Text(textSrc.getString("memory_could_not_obtain")));
                    } else {
                        int memory = JCMemory.getPersistentMemory(apduData);
                        if (memory == 32767) {
                            self.add(new HtmlText("<p style='width:350px;'>" +
                                    textSrc.getString("card_free_memory_over32kb") + "</div>"), "wrap");
                        } else {
                            self.add(new Text(textSrc.getString("card_free_memory")), "");
                            self.add(new Text(memory + " bytes."), "align right, wrap");
                        }
                    }
                    Window origin = SwingUtilities.getWindowAncestor(self);
                    origin.pack();
                    origin.setLocationRelativeTo(null);
                    refreshable.refresh();
                    return null;
                }
            }).start();
        } else {
            removeAll();
            add(new JLabel(textSrc.getString("no_card")));
        }
    }
}
