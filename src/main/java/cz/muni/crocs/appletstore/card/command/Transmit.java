package cz.muni.crocs.appletstore.card.command;

import apdu4j.CommandAPDU;
import apdu4j.HexUtils;
import apdu4j.ResponseAPDU;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import pro.javacard.AID;
import pro.javacard.gp.GPException;
import pro.javacard.gp.ISO7816;

import javax.smartcardio.CardException;
import java.io.IOException;

public class Transmit extends GPCommand<byte[]> {

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
        ResponseAPDU response = channel.transmit(new CommandAPDU(0x00, ISO7816.INS_SELECT, 0x04, 0x00, targetAid.getBytes()));
        if (response.getSW() != 0x9000) return false;
        CommandAPDU c = new CommandAPDU(HexUtils.stringToBin(APDU));
        response = channel.transmit(c);
        if (response.getSW() != 0x9000) return false;
        result = response.getData();
        return true;
    }
}
