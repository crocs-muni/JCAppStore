package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.util.*;

import javax.annotation.Nullable;
import java.awt.event.MouseEvent;

/**
 * Class to add to button as listener target to perform applet installation
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class ReloadAction extends CardAbstractAction {

    public ReloadAction(OnEventCallBack<Void, Void> call) {
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
