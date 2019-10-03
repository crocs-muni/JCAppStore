package cz.muni.crocs.appletstore.card.action;

import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Class to add to button as listener target to perform applet installation
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class ReloadAction extends CardAction {
    private static final Logger logger = LoggerFactory.getLogger(ReloadAction.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    public ReloadAction(OnEventCallBack<Void, Void, Void> call) {
        super(call);
    }

    @Override
    public void mouseClicked(@Nullable MouseEvent e) {
        execute(() -> {
            CardManager manager = CardManagerFactory.getManager();
            manager.loadCard();
        }, "Failed to reload card.", textSrc.getString("failed_to_reload"));
    }
}
