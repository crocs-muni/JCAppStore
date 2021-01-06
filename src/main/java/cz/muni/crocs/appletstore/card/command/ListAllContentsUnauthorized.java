package cz.muni.crocs.appletstore.card.command;

import apdu4j.CommandAPDU;
import apdu4j.ResponseAPDU;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.gp.GPException;
import pro.javacard.gp.ISO7816;

import java.io.File;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Try to detect **ALL** installed applets without authenticating to ISD.
 * Note: this bruteforce method might not work on all applets, also it will take some time.
 * Note: what happens to the card after this many SELECT request is not guaranteed.
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class ListAllContentsUnauthorized extends GPCommand<Set<String>> {
    private static final Logger logger = LoggerFactory.getLogger(ListAllContentsUnauthorized.class);
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private static final int DETECTION_FROM_AID_PREFIX_BYTES_LEN = 3;

    private final File aidList;

    public ListAllContentsUnauthorized(File aidList) {
        this.aidList = aidList;
    }

    @Override
    public boolean execute() throws GPException {
        result = new HashSet<>();
        detectRecursiveAt("");
        return true;
    }

    //todo test
    private void detectRecursiveAt(String prefix) {
        if (prefix.length() >= DETECTION_FROM_AID_PREFIX_BYTES_LEN * 2) {
            try {
                logger.info("Detect AID at prefix: " + prefix);
                ResponseAPDU response = channel.transmit(
                        new CommandAPDU(0x00, ISO7816.INS_SELECT, 0x04, 0x00, Hex.decode(prefix)));

                if (response.getSW() == 0x9000) {
                    //todo this will add all prefixes of found AIDs - remove all AIDs that have prefix to some other in this list (sort it)
                    result.add(prefix);
                } else return; //cut walking, no applet with such prefix exists
            } catch (Exception e) {
                logger.warn("Card threw an error: " + e.getMessage());
                logger.warn("The search on this AID prefix was aborted: " + prefix);
                return;
            }
        }

        for (int i = 0; i <= 255; i++) {
            String hex = Integer.toHexString(i);
            detectRecursiveAt(prefix + hex.substring(Math.max(hex.length() - 2, 0)));
        }
    }

    @Override
    public String getDescription() {
        return "Applets presence detection, based on " + aidList.getName();
    }

}
