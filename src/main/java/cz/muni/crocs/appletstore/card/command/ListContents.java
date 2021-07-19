package cz.muni.crocs.appletstore.card.command;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GPRegistry;
import pro.javacard.gp.GPRegistryEntry;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Parses all installed applets to display them in app
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class ListContents extends GPCommand<CardInstanceMetaData> {
    private static final Logger logger = LoggerFactory.getLogger(ListContents.class);
    private final String cardId;

    public ListContents(String cardId) {
        this.cardId = cardId;
    }

    @Override
    public boolean execute() throws GPException, IOException {
        result = CardInstanceMetaData.empty();
        GPRegistry registry = context.getRegistry();
        if (registry == null || cardId == null) return false;

        logger.info("List contents of card: " + cardId);
        AppletSerializer<CardInstanceMetaData> savedData = new AppletSerializerImpl();
        File file = new File(Config.APP_DATA_DIR + Config.S + cardId);

        CardInstanceMetaData saved;
        if (file.exists()) {
            try {
                saved = savedData.deserialize(file);
            } catch (LocalizedCardException e) {
                //todo show user info that card metadata was unable to parse the old data?
                // TODO: any change to the metadata class will cause this issue and users will lose
                // all the metadata
                e.printStackTrace();
                logger.warn("Failed to obtain card cache file", e);
                saved = CardInstanceMetaData.empty();
            }
        } else {
            saved = CardInstanceMetaData.empty();
        }

        ArrayList<AppletInfo> packages = new ArrayList<>();
        for (GPRegistryEntry entry : registry) {
            AppletInfo info = new AppletInfo(entry, saved.getApplets());
            if (info.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile) {
                packages.add(info);
            } else {
                result.addAppletInstance(info);
            }
        }

        for (AppletInfo pkg : packages) {
            result.addAppletPackage(pkg);
        }

        result.setJCData(saved.getJCData());
        return true;
    }

    @Override
    public String getDescription() {
        return "List contents method for: " + cardId;
    }
}
