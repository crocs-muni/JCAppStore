package cz.muni.crocs.appletstore.card.command;

import apdu4j.APDUBIBO;
import apdu4j.CommandAPDU;
import cz.muni.crocs.appletstore.card.CardDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.gp.ISO7816;

import javax.smartcardio.CardException;

import java.io.IOException;

import static pro.javacard.gp.GPData.fetchCPLC;
import static pro.javacard.gp.GPData.fetchKeyInfoTemplate;
import static pro.javacard.gp.GPData.getData;

/**
 * @author Jiří Horák
 */
public class GetDetails extends GPCommand<CardDetails> {
    private static final Logger logger = LoggerFactory.getLogger(GetDetails.class);

    public GetDetails(APDUBIBO channel) {
        this.channel = channel;
    }

    @Override
    public boolean execute() throws CardException, IOException {
        result = new CardDetails();

        //select security domain in case default selected applet present on the card
        logger.info("Selecting security domain..");
        channel.transmit(new CommandAPDU(0x00, ISO7816.INS_SELECT, 0x04, 0x00, 0x00)); // lc and data omitted
        logger.info("Domain selected.");

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
