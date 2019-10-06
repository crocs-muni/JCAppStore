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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Parses all installed applets to display them in app
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class ListContents extends GPCommand<ArrayList<AppletInfo>> {

    @Override
    public boolean execute() throws CardException, GPException, IOException {
        result = new ArrayList<>();
        GPRegistry registry = context.getRegistry();
        if (registry == null || cardId == null) return false;

        AppletSerializer<List<AppletInfo>> savedData = new AppletSerializerImpl();
        File file = new File(Config.APP_DATA_DIR + Config.S + cardId);

        List<AppletInfo> saved;
        if (file.exists()) {
            try {
                saved = savedData.deserialize(file);
            } catch (LocalizedCardException e) {
                e.printStackTrace();
                saved = Collections.emptyList();
            }
        } else {
            saved = Collections.emptyList();
        }

        saved.forEach(item -> {
            System.out.println(item.getAid().toString() + ", " + item.getKind());
        });

        for (GPRegistryEntry entry : registry) {
            //global platform lists all packages two times skip the one with no modules in it
            if (entry.getType().equals(GPRegistryEntry.Kind.ExecutableLoadFile) && entry.getModules().size() == 0)
                continue;
            result.add(new AppletInfo(entry, saved));
        }
        return true;
    }
}
