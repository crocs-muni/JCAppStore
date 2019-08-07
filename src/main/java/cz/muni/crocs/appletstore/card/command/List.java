package cz.muni.crocs.appletstore.card.command;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.card.AppletSerializer;
import cz.muni.crocs.appletstore.card.AppletSerializerImpl;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GPRegistry;
import pro.javacard.gp.GPRegistryEntry;

import javax.smartcardio.CardException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Parses all installed applets to display them in app
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class List extends GPCommand<ArrayList<AppletInfo>> {

    @Override
    public boolean execute() throws CardException, GPException, IOException {
        result = new ArrayList<>();
        GPRegistry registry = context.getRegistry();
        if (registry == null || cardId == null) return false;

        AppletSerializer<java.util.List<AppletInfo>> savedData = new AppletSerializerImpl();
        File file = new File(Config.APP_DATA_DIR + Config.SEP + cardId);

        java.util.List<AppletInfo> saved = null;
        if (file.exists()) {
            try {
                saved = savedData.deserialize(file);
            } catch (LocalizedCardException e) {
                e.printStackTrace();
            }
        } else {
            saved = Collections.emptyList();
        }

        for (GPRegistryEntry entry : registry) {
            result.add(new AppletInfo(entry, saved));
        }

        return true;
    }
}
