package cz.muni.crocs.appletstore;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.action.FreeMemoryAction;
import cz.muni.crocs.appletstore.action.JCMemory;
import cz.muni.crocs.appletstore.ui.Text;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class CardInfoPanel extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    public CardInfoPanel(BackgroundChangeable changeable, Refreshable refreshable) {
        setLayout(new MigLayout());
        final CardInfoPanel self = this;

        if (CardManagerFactory.getManager().isCard()) {
            new FreeMemoryAction(new OnEventCallBack<Void, byte[]>() {
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
                    if (apduData == null) {
                        self.add(new JLabel(new ImageIcon(Config.IMAGE_DIR + "announcement.png")), "align center, wrap");
                        self.add(new Text(textSrc.getString("memory_could_not_obtain")));
                    } else {
                        self.add(new Text(textSrc.getString("card_free_memory")), "");
                        self.add(new Text(String.valueOf(JCMemory.getPersistentMemory(apduData))), "align right, wrap");
                    }
                    self.revalidate();
                    self.repaint();
                    refreshable.refresh();
                    return null;
                }
            }).start();
        } else {
            add(new JLabel(new ImageIcon(Config.IMAGE_DIR + "no-card-black.png")), "align center, wrap");
            add(new Text(textSrc.getString("no_card")));
        }
    }
}
