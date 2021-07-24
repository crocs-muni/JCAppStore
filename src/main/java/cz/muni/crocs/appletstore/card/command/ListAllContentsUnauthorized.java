package cz.muni.crocs.appletstore.card.command;

import apdu4j.CommandAPDU;
import apdu4j.ResponseAPDU;
import cz.muni.crocs.appletstore.card.CardInstanceMetaData;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.gp.GPException;
import pro.javacard.gp.ISO7816;

import java.io.File;
import java.util.*;

/**
 * Try to detect **ALL** installed applets without authenticating to ISD.
 * Note: this bruteforce method might not work on all applets, also it will take some time.
 * Note: what happens to the card after this many SELECT request is not guaranteed.
 *
 * TODO: delete this class, not usable (SELECT must already have a certain prefix length, too many
 *  to try
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class ListAllContentsUnauthorized extends GPCommand<CardInstanceMetaData> {
    private static final Logger logger = LoggerFactory.getLogger(ListAllContentsUnauthorized.class);
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private final File aidList;

    public ListAllContentsUnauthorized(File aidList) {
        this.aidList = aidList;
    }

    @Override
    public boolean execute() throws GPException {
        SortedSet<String> res = new TreeSet<>();
        detectRecursiveAt("", res);

        //TODO 'res' remove all prefixes in sorted order
        result = new CardInstanceMetaData(new HashSet<>(), null);
        return true;
    }

    //todo test
    private void detectRecursiveAt(String prefix, SortedSet<String> result) {
        if (prefix.length() > 9) { //4 bytes are not accepted, 5+ bytes for select to work
            try {
                System.out.println("Detect AID at prefix: " + prefix);
                ResponseAPDU response = channel.transmit(
                        new CommandAPDU(0x00, ISO7816.INS_SELECT, 0x04, 0x00, Hex.decode(prefix)));

                if (response.getSW() == 0x9000) {
                    System.out.println(prefix);
                    result.add(prefix);
                } else {
                    return;
                }
            } catch (Exception e) {
                logger.warn("Card threw an error: " + e.getMessage());
                logger.warn("The search on this AID prefix was aborted: " + prefix);
                return;
            }
        }

        for (int i = 0; i < 256; i++) {
            String hex = String.format("%02X", i);
            detectRecursiveAt(prefix + hex.substring(Math.max(hex.length() - 2, 0)), result);
        }
    }

    @Override
    public String getDescription() {
        return "Applets presence detection, based on " + aidList.getName();
    }

}
