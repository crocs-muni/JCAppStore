package cz.muni.crocs.appletstore.card.command;

import apdu4j.CommandAPDU;
import apdu4j.HexUtils;
import apdu4j.ResponseAPDU;
import cz.muni.crocs.appletstore.card.AppletInfo;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.gp.GPException;
import pro.javacard.gp.ISO7816;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

/**
 * Try to detect installed applets without authenticating to ISD.
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class DetectUnauthorized extends GPCommand<ResponseAPDU> {
    private static final Logger logger = LoggerFactory.getLogger(DetectUnauthorized.class);

    private final File aidList;
    private final HashSet<AppletInfo> found = new HashSet<>();

    public DetectUnauthorized(File aidList) {
        this.aidList = aidList;
    }

    @Override
    public boolean execute() throws GPException, IOException {
        //TRY GET_DATA '2F00'
        logger.info("Use GET_DATA command: '2F00' that lists applets if supported.");
        logger.debug(">> 80CA2F00 00"); //todo possibly 00CBF200 00
        result = channel.transmit(new CommandAPDU(0x80, ISO7816.INS_GET_DATA, 0xF2, 0x00, 256));
        logger.debug("<< " + HexUtils.bin2hex(result.getBytes()));
        //if (result.getSW() == 0x9000) {
            //todo fill in data-- found.add(...);
            //todo possible need for repeated commands (send data part 2,3,4 .. too big)
            //todo compare with AID list to get more info? author etc.
            //return true;
        //}

        //OR BRUTEFORCE aid list
        if (aidList == null || !aidList.exists()) return false;
        for (Map.Entry<String, Profile.Section> entry : new Ini(aidList).entrySet()) {
            //todo first 5 bytes should suffice?
            result = channel.transmit(new CommandAPDU(0x00, ISO7816.INS_SELECT, 0x04, 0x00, entry.getKey().getBytes()));
            if (result.getSW() == 0x9000) {
                AppletInfo nfo = new AppletInfo(entry.getValue().get("name"), "unknown", "unknown",
                        entry.getValue().get("author"), "");
                if (found.contains(nfo)) continue;
                found.add(nfo);
                logger.debug("+Select " + entry.getKey() + "successful.");
            } else logger.debug("-Select " + entry.getKey());
        }
        return true;
    }

    public HashSet<AppletInfo> getResults() {
        return found;
    }

    @Override
    public String getDescription() {
        return "Applets presence detection, based on " + aidList.getName();
    }
}
