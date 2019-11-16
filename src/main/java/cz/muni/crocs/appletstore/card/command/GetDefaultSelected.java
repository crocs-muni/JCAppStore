package cz.muni.crocs.appletstore.card.command;

import cz.muni.crocs.appletstore.card.LocalizedCardException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GPRegistry;

import java.io.IOException;
import java.util.Optional;


/**
 * Modified install process from GPPro
 *
 * @author Jiří Horák & Martin Paljak
 * @version 1.0
 */
public class GetDefaultSelected extends GPCommand<Optional<AID>> {

    private static final Logger logger = LoggerFactory.getLogger(GetDefaultSelected.class);
    @Override
    public boolean execute() throws GPException {
        try {
            GPRegistry registry = context.getRegistry();
            if (registry == null)
                result = Optional.empty();
            else
                result = registry.getDefaultSelectedAID();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Failed to get registry from session.", e);
            return false;
        }
        return true;
    }

    @Override
    public String getDescription() {
        return "Get default selected method.";
    }
}
