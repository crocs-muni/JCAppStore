package cz.muni.crocs.appletstore.card.command;

import cz.muni.crocs.appletstore.iface.CardCommand;
import cz.muni.crocs.appletstore.util.AppletInfo;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GPRegistry;
import pro.javacard.gp.GPRegistryEntry;

import javax.smartcardio.CardException;
import java.util.ArrayList;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class List extends GPCommand {

    private ArrayList<AppletInfo> applets = new ArrayList<>();
    private String status = "OP_READY";

    public ArrayList<AppletInfo> getApplets() {
        return applets;
    }

    @Override
    public boolean execute() throws CardException, GPException {
        GPRegistry registry = context.getRegistry();
        if (registry == null || cardId == null) return false;

        for (GPRegistryEntry entry : registry) {
            applets.add(new AppletInfo(entry, cardId));
        }
        return true;
    }
}
