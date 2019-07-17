package cz.muni.crocs.appletstore.card.command;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.util.IniParserImpl;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GPRegistry;
import pro.javacard.gp.GPRegistryEntry;

import javax.smartcardio.CardException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class List extends GPCommand<ArrayList<AppletInfo>> {

    @Override
    public boolean execute() throws CardException, GPException {
        result = new ArrayList<>();
        GPRegistry registry = context.getRegistry();
        if (registry == null || cardId == null) return false;

        IniParserImpl cardDetails = null;
        try {
            cardDetails = new IniParserImpl(Config.INI_CARD_LIST, cardId);

            for (GPRegistryEntry entry : registry) {
                //if (entry.getAID().toString())
                result.add(new AppletInfo(entry, cardId));
            }
        } catch (IOException e) {
            for (GPRegistryEntry entry : registry) {
                result.add(new AppletInfo(entry, cardId));
            }
        }

        return true;
    }

//    java.util.List<String> get

//    private boolean isInstalledWithStore(GPRegistryEntry entry, )
}
