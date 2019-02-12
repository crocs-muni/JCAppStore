package cz.muni.crocs.appletstore.card;

import apdu4j.ISO7816;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.command.GPCommand;
import cz.muni.crocs.appletstore.card.command.GetDetails;
import cz.muni.crocs.appletstore.card.command.List;
import cz.muni.crocs.appletstore.iface.CardCommand;
import cz.muni.crocs.appletstore.util.AppletInfo;
import cz.muni.crocs.appletstore.util.IniParser;
import pro.javacard.gp.GPData;
import pro.javacard.gp.GPException;

import pro.javacard.gp.GlobalPlatform;
import pro.javacard.gp.PlaintextKeys;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CardInstance {

    public static final String DEFAULT_TEST_KEY = "404142434445464748494A4B4C4D4E4F";
    private String masterKey = DEFAULT_TEST_KEY;

    public static final String NO_CARD = "";
    private String id = NO_CARD;

    private CardDetails details;
    private CardTerminal terminal;

    private ArrayList<AppletInfo> applets;


    public enum CardState {
        OP_READY, INITIALIZED, SECURED, LOCKED, TERMINATED, UNAUTHORIZED
    }
    private CardState state;

    public CardState getState() {
        return state;
    }

    public ArrayList<AppletInfo> getApplets() {
        return applets;
    }

    /**
     * Performs the only insecure channel use
     * to get data from inserted card
     */
    public static CardDetails getCardInfo(CardTerminal terminal) throws CardException {
        Card card = terminal.connect("*");

        card.beginExclusive();
        GetDetails command = new GetDetails(card.getBasicChannel());
        command.execute();
        card.endExclusive();

        CardDetails details = command.getOuput();
        details.setAtr(card.getATR());

        card.disconnect(false);
        return details;
    }

    /**
     * Compares the card id and updates card data if needed
     * e.g. swaps the card instance
     *
     * @param newDetails of the card: ATR is a must, other optional
     */
    public void update(CardDetails newDetails, CardTerminal terminal) {
        this.terminal = terminal;

        if (newDetails == null || terminal == null) {
            this.id = NO_CARD;
            this.details = null;
            this.masterKey = null;
            return;
        }

        String newId = getId(newDetails);
        if (this.id.equals(newId)) {
            return;
        }
        details = newDetails;
        id = newId;
        this.masterKey = null; //possible present key from previous cards

        try {
            if (saveDetailsAndCheckMasterKey())
                getCardListWithMasterPassword();
            else
                getCardListWithDefaultPassword(false);
        } catch (IOException | CardException ex) {
            state = CardState.UNAUTHORIZED;
        }
    }

    /**
     * Open the ini file and try to find our card,
     * possibly save the card info
     *
     * @return true if card info present and custom master key is set
     */
    private boolean saveDetailsAndCheckMasterKey() throws IOException {
        IniParser parser = new IniParser(Config.INI_CARD_LIST, id);
        if (parser.isHeaderPresent()) {
            masterKey = parser.getValue("MasterKey");
            return !(masterKey == null || masterKey.isEmpty());
        }
        System.out.println("Saving card specific data into ini file: " + id);
        parser.addValue("MasterKey", "")
                .addValue("ATR", byteArrayToUnitString(details.getAtr().getBytes()))
                .addValue("CIN", details.getCin())
                .addValue("IIN", details.getIin())
                .addValue("CPLC", (details.getCplc() == null) ? null : details.getCplc().toString())
                .addValue("CardData", details.getCardData())
                .addValue("CardCapabilities", details.getCardCapabilities())
                .addValue("KeyInfo", details.getKeyInfo())
                .store();
        return false;
    }

    /**
     * Executes any desired command using secure channel
     * @param command command instance to execute
     * @param emv true if emv diversification used
     * @throws CardException unable to perform command
     */
    private void executeCommand(GPCommand command, boolean emv) throws CardException {
        Card card = null;
        GlobalPlatform context = null;

        try {
            card = terminal.connect("*");
            card.beginExclusive();
            //card.beginExclusive();  todo ask causes error CARD HAS BEEN RESET
            context = GlobalPlatform.discover(card.getBasicChannel());
            secureConnect(context, (emv) ? PlaintextKeys.Diversification.EMV : null);

            command.setCardId(id);
            command.setGP(context);
            command.execute();

        } catch (GPException e) {
            if (emv) {
                switch (e.sw) {
                    case ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED:
                    case ISO7816.SW_AUTHENTICATION_METHOD_BLOCKED:

                        state = CardState.LOCKED;
                        break;
                    default:
                        //todo unable to connect
                }
            } else {
                System.out.println("trying -emv");
                getCardListWithDefaultPassword(true);
            }
        } finally {
            if (card != null) {
                card.endExclusive();
                card.disconnect(true);
            }
        }
    }

    /**
     * Open card types INI and searches by ATR for default password
     * todo: separate list from getting default key
     * extract functionality into one connection process
     */
    private void getCardListWithDefaultPassword(boolean diversification) throws CardException {

        try {
            IniParser parser = new IniParser(Config.INI_CARD_TYPES, byteArrayToUnitString(details.getAtr().getBytes()));
            if (parser.isHeaderPresent())
                masterKey = parser.getValue("MasterKey");
            if (masterKey == null || masterKey.isEmpty()) masterKey = DEFAULT_TEST_KEY;

            List list = new List();
            executeCommand(list, false);
            applets = list.getApplets();

        } catch (IOException e) {
            //todo handle
            e.printStackTrace();
        }
    }

    /**
     * Open card types INI and searches by ATR for default password
     */
    private void getCardListWithMasterPassword() {
        //todo
    }


    /**
     * Assumes that masterKey variable is set. Tries to secure connect with it
     * If successful, loads the card contents
     * This method may brick the card if bad masterKey set
     */
    private void secureConnect(GlobalPlatform context, PlaintextKeys.Diversification diversification)
            throws CardException, GPException {

//        PlaintextKeys key = PlaintextKeys.fromMasterKey(new GPKey(masterKey.getBytes()));
        PlaintextKeys key = PlaintextKeys.fromMasterKey(GPData.getDefaultKey());
        //todo masterKey ignored
        //todo uses DES-3 how do i know which to use for new GPKey()

        if (diversification != null) key.setDiversifier(diversification);
        //todo mode - now default only
        context.openSecureChannel(key, null, 0,  GlobalPlatform.defaultMode.clone());
    }

    /**
     * Convert array to single unit
     *
     * @param array to convert
     * @return items without delimiters in a string
     */
    private String byteArrayToUnitString(byte[] array) {
        StringBuilder builder = new StringBuilder();
        for (byte b : array) {
            builder.append(b);
        }
        return builder.toString();
    }

    /**
     * Computes the card ID
     *
     * @param details details to get data for id
     * @return card id
     */
    private String getId(CardDetails details) {
        return "ATR:" + byteArrayToUnitString(details.getAtr().getBytes()) + ":ICSN:" +
                ((details.getCplc() == null) ?
                        "null" : byteArrayToUnitString(details.getCplc().get(GPData.CPLC.Field.ICSerialNumber)));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CardInstance)) return false;
        return ((CardInstance) obj).id.equals(this.id);
    }
}
