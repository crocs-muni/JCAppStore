package cz.muni.crocs.appletstore.card.command;

import cz.muni.crocs.appletstore.card.InstallOpts;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.CAPFile;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GPRegistry;

import java.io.IOException;

/**
 * Modified install process from GPPro
 *
 * @author Jiří Horák & Martin Paljak
 * @version 1.0
 */
public class Load extends GPCommand<Void> {
    private static final Logger logger = LoggerFactory.getLogger(Load.class);

    private final CAPFile file;
    private InstallOpts data;

    public Load(CAPFile f, InstallOpts data) {
        this.file = f;
        this.data = data;
    }

    @Override
    public boolean execute() throws LocalizedCardException, GPException {
        if (data == null) {
            logger.info("Installing params are missing");
            throw new LocalizedCardException("No install data.", "E_notify_us");
        }
        logger.info("Installing params: " + data.toString());

        GPRegistry registry;
        try {
            registry = context.getRegistry();
        } catch (IOException e) {
            //todo message?
            throw new LocalizedCardException("", e);
        }

        // Remove existing default app
        if (data.isForce() && registry.allPackageAIDs().contains(file.getPackageAID())) {
            try {
                context.deleteAID(file.getPackageAID(), true);
            } catch (IOException e) {
                e.printStackTrace();
                logger.warn("Failed to remove existing package before an install.", e);
            }
        }
        //todo whether to uninstall even when package differs but applet AID is the same (e.g. PIV applet / open FIPS)

        try {
            context.loadCapFile(file, null);
            logger.info("CAP file loaded.");
        } catch (GPException e) {
            if (e.sw == 0x00) {
                throw new LocalizedCardException("Package already present", "E_pkg_present", e);
            }
            throw new LocalizedCardException("Package load failed", "E_load_failed", e);
        } catch (IOException e) {
            throw new LocalizedCardException("Failed to load cap file onto card", "E_install_load_failed", e);
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "Load procedure [install for load].";
    }
}
