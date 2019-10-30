package cz.muni.crocs.appletstore.card.action;

import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import java.io.File;
import java.io.IOException;

public class JCSystemInfo {

    byte[] getSystemInfo() throws LocalizedCardException {
        final CardManager manager = CardManagerFactory.getManager();

        byte[] size = null;
        try {
            size = manager.sendApdu(JCMemory.getAID(), JCMemory.getAPDU());
        } catch (LocalizedCardException e) {
            //ignore
            e.printStackTrace();
        }
        if (size != null)
            return size;

        try {
            File f = JCMemory.getSource();
            System.out.println(f.exists());
            manager.install(JCMemory.getSource(), JCMemory.getInstallOptions());
        } catch (IOException e) {
            return null;
        }

        try {
            size = manager.sendApdu(JCMemory.getAID(), JCMemory.getAPDU());
        } catch (LocalizedCardException e) {
            //ignore
            e.printStackTrace();
        }

        if (!OptionsFactory.getOptions().keepJCMemory()) {
            //todo somehow not uninstalled
            manager.uninstall(JCMemory.getPackageInfo(), true);
        } else {
            //todo somehow force the localpanel to refresh, manager is (maybe) refreshed...
            //manager.loadCard();
        }
        return size;
    }
}
