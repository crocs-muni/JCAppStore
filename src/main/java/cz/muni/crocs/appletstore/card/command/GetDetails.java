package cz.muni.crocs.appletstore.card.command;

import apdu4j.APDUBIBO;
import cz.muni.crocs.appletstore.card.CardDetails;

import javax.smartcardio.CardException;

import java.io.IOException;

import static pro.javacard.gp.GPData.fetchCPLC;
import static pro.javacard.gp.GPData.fetchKeyInfoTemplate;
import static pro.javacard.gp.GPData.getData;

/**
 * @author Jiří Horák
 */
public class GetDetails implements CardCommand {
    private APDUBIBO channel;
    private CardDetails details;

    public GetDetails(APDUBIBO channel) {
        this.channel = channel;
    }

    public CardDetails getOuput() {
        return details;
    }

    @Override
    public boolean execute() throws CardException, IOException {
        details = new CardDetails();
        details.setCplc(fetchCPLC(channel));
        details.setIin(getData(channel, 0x00, 0x42, "IIN", false));
        details.setCin(getData(channel, 0x00, 0x45, "CIN", false));
        details.setCardData(getData(channel, 0x00, 0x66, "Card Data", false));
        details.setCardCapabilities(getData(channel, 0x00, 0x67, "Card Capabilities", false));
        details.setKeyInfo(fetchKeyInfoTemplate(channel));
        return true;
    }
}
