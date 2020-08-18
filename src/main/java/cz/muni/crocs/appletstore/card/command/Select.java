package cz.muni.crocs.appletstore.card.command;

import apdu4j.CommandAPDU;
import apdu4j.HexUtils;
import apdu4j.ResponseAPDU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.gp.GPException;
import pro.javacard.gp.ISO7816;

import java.io.IOException;

/**
 * Custom APDU transmission
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class Select extends GPCommand<Boolean> {
    private static final Logger logger = LoggerFactory.getLogger(Select.class);

    private final AID targetAid;

    public Select(String targetAid) {
        this.targetAid = AID.fromString(targetAid);
    }

    public Select(AID targetAid) {
        this.targetAid = targetAid;
    }

    @Override
    public boolean execute() throws GPException, IOException {
        logger.info("Transmit command for applet: " + targetAid);
        logger.debug(">> 00A40400 [len byte missing]" + HexUtils.bin2hex(targetAid.getBytes()));
        ResponseAPDU response = channel.transmit(
                new CommandAPDU(0x00, ISO7816.INS_SELECT, 0x04, 0x00, targetAid.getBytes()));
        logger.debug("<< " + HexUtils.bin2hex(response.getBytes()));
        result = response.getSW() == 0x9000;
        return true;
    }

    @Override
    public String getDescription() {
        return "Select command " + targetAid;
    }
}
