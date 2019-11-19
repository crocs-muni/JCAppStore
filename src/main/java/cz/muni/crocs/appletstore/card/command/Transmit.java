package cz.muni.crocs.appletstore.card.command;

import apdu4j.CommandAPDU;
import apdu4j.HexUtils;
import apdu4j.ResponseAPDU;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.gp.GPException;
import pro.javacard.gp.ISO7816;

import javax.smartcardio.CardException;
import java.io.IOException;

public class Transmit extends GPCommand<ResponseAPDU> {
    private static final Logger logger = LoggerFactory.getLogger(Transmit.class);

    private AID targetAid;
    private String APDU;

    public Transmit(String targetAid, String APDU) {
        this.targetAid = AID.fromString(targetAid);
        this.APDU = APDU;
    }

    public Transmit(AID targetAid, String APDU) {
        this.targetAid = targetAid;
        this.APDU = APDU;
    }

    @Override
    public boolean execute() throws CardException, GPException, LocalizedCardException, IOException {
        logger.debug(">> 00A40400 " + HexUtils.bin2hex(targetAid.getBytes()));
        ResponseAPDU response = channel.transmit(new CommandAPDU(0x00, ISO7816.INS_SELECT, 0x04, 0x00, targetAid.getBytes()));
        logger.debug("<< " + HexUtils.bin2hex(response.getBytes()));
        if (response.getSW() != 0x9000) return false;
        logger.debug(">> " + APDU);
        CommandAPDU c = new CommandAPDU(HexUtils.stringToBin(APDU));
        response = channel.transmit(c);
        logger.debug("<< " + HexUtils.bin2hex(response.getBytes()));
        if (response.getSW() != 0x9000) return false;
        result = response;
        return true;
    }

    @Override
    public String getDescription() {
        return "Transmit method with command of " + APDU + ", sent to AID: " + targetAid;
    }
}
