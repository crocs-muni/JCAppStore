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
public class GetDetails extends GPCommand<CardDetails> {

    public GetDetails(APDUBIBO channel) {
        this.channel = channel;
    }

    @Override
    public boolean execute() throws CardException, IOException {
        result = new CardDetails();
        result.setCplc(fetchCPLC(channel));
        result.setIin(getData(channel, 0x00, 0x42, "IIN", false));
        result.setCin(getData(channel, 0x00, 0x45, "CIN", false));
        result.setCardData(getData(channel, 0x00, 0x66, "Card Data", false));
        result.setCardCapabilities(getData(channel, 0x00, 0x67, "Card Capabilities", false));
        result.setKeyInfo(fetchKeyInfoTemplate(channel));
        return true;
    }

    @Override
    public String getDescription() {
        return "Getting card detail information before authentication.";
    }
}
