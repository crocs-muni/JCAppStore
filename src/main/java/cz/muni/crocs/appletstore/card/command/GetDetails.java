package cz.muni.crocs.appletstore.card.command;

import cz.muni.crocs.appletstore.card.CardDetails;
import cz.muni.crocs.appletstore.iface.CardCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

import static pro.javacard.gp.GPData.fetchCPLC;
import static pro.javacard.gp.GPData.fetchKeyInfoTemplate;
import static pro.javacard.gp.GPData.getData;

/**
 * @author Jiří Horák
 */
public class GetDetails implements CardCommand {
    private static final Logger logger = LoggerFactory.getLogger(GetDetails.class);

    private CardChannel channel;
    private CardDetails details;

    public GetDetails(CardChannel channel) {
        this.channel = channel;
    }

    public CardDetails getOuput() {
        return details;
    }

    @Override
    public boolean execute() throws CardException {
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
