package cz.muni.crocs.appletstore.card.action;

import apdu4j.ResponseAPDU;
import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import java.io.IOException;

public class CardCommands {

    public static boolean setDefaultSelected(String aid) throws LocalizedCardException {
        final CardManager manager = CardManagerFactory.getManager();
        ResponseAPDU response = manager.sendApdu(aid, "");
        return response.getSW() == 0x9000;
    }

    public static byte[] getSystemInfo() throws LocalizedCardException {
        byte[] result;
        final CardManager manager = CardManagerFactory.getManager();

        ResponseAPDU response = null;
        try {
            response = manager.sendApdu(JCMemory.getAID(), JCMemory.getAPDU());
        } catch (LocalizedCardException e) {
            //ignore
            e.printStackTrace();
        }
        result = getData(response);
        if (result != null) {
            uninstallIfNotKeep(manager, false);
            return result;
        }

        try {
            manager.install(JCMemory.getSource(), JCMemory.getInstallOptions());
        } catch (IOException e) {
            return null;
        }

        try {
            response = manager.sendApdu(JCMemory.getAID(), JCMemory.getAPDU());
        } catch (LocalizedCardException e) {
            //ignore
            e.printStackTrace();
        }
        uninstallIfNotKeep(manager, true);
        return getData(response);
    }

    private static byte[] getData(ResponseAPDU responseAPDU) throws LocalizedCardException {
        if (responseAPDU != null && responseAPDU.getSW() == 0x9000) {
            return responseAPDU.getBytes();
        }
        return null;
    }

    private static void uninstallIfNotKeep(CardManager manager, boolean refresh) throws LocalizedCardException {
        if (!OptionsFactory.getOptions().is(Options.KEY_KEEP_JCMEMORY)) {
            manager.uninstall(JCMemory.getPackageInfo(), true);
            //todo somehow force the localpanel to refresh, manager is (maybe) refreshed...

        } else if (refresh) {
            manager.loadCard();
            //todo somehow force the localpanel to refresh, manager is (maybe) refreshed...

        }
    }
}
