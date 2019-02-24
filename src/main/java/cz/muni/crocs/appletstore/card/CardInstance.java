package cz.muni.crocs.appletstore.card;

import apdu4j.HexUtils;
import apdu4j.ISO7816;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.command.GPCommand;
import cz.muni.crocs.appletstore.card.command.GetDetails;
import cz.muni.crocs.appletstore.card.command.List;
import cz.muni.crocs.appletstore.util.AppletInfo;
import cz.muni.crocs.appletstore.util.IniParser;
import pro.javacard.gp.GPData;
import pro.javacard.gp.GPException;

import pro.javacard.gp.GPKey;
import pro.javacard.gp.GlobalPlatform;
import pro.javacard.gp.PlaintextKeys;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CardInstance {

    public static final String DEFAULT_TEST_KEY = "404142434445464748494A4B4C4D4E4F";
    private String masterKey = DEFAULT_TEST_KEY;        //default test key with no emv and DES3 alg
    private GPKey.Type keyType = GPKey.Type.DES3;
    private boolean emv = false;

    public static final String NO_CARD = "";
    private String id = NO_CARD;

    private CardDetails details;
    private CardTerminal terminal;

    private ArrayList<AppletInfo> applets;

    public enum CardState {
        OK, LOCKED, UNAUTHORIZED, FAILED
    }

    private CardState state = CardState.OK;
    public String error;

    public CardState getState() {
        return state;
    }

    public ArrayList<AppletInfo> getApplets() {
        if (applets == null) return new ArrayList<>();
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
            this.state = CardState.OK;
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
                getCardListWithDefaultPassword();
        } catch (IOException | CardException ex) {
            this.details = null;
            this.masterKey = null;
            this.state = CardState.FAILED;
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
            keyType = getType(parser.getValue("KeyType").toUpperCase());
            emv = parser.getValue("EMV").toUpperCase().equals("EMV");
            return !(masterKey == null || masterKey.isEmpty());
        }
        System.out.println("Saving card specific data into ini file: " + id);
        parser.addValue("MasterKey", "") //todo update if supports custom keys
                //one of: RAW, DES, DES3, AES, RSAPUB, PSK
                .addValue("KeyType", "")
                // uses EMV - YES / NO
                .addValue("EMV", "")
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
     * Open card types INI and searches by ATR for default password
     * todo: separate list from getting default key
     * extract functionality into one connection process
     */
    private void getCardListWithDefaultPassword() throws CardException {

        try {
            IniParser parser = new IniParser(Config.INI_CARD_TYPES, byteArrayToUnitString(details.getAtr().getBytes()));
            if (parser.isHeaderPresent()) {
                masterKey = parser.getValue("MasterKey");
                keyType = getType(parser.getValue("KeyType").toUpperCase());
                emv = parser.getValue("EMV").toUpperCase().equals("EMV");
            }
            if (masterKey == null || masterKey.isEmpty()) {
                masterKey = DEFAULT_TEST_KEY;
                keyType = GPKey.Type.DES3;
                emv = false;
            }

            List list = new List();
            executeCommand(list);
            applets = list.getResult();

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
     * Executes any desired command using secure channel
     *
     * @param command command instance to execute
     * @throws CardException unable to perform command
     */
    public void executeCommand(GPCommand command) throws CardException {
        Card card = null;
        GlobalPlatform context = null;

        try {
            card = terminal.connect("*");
            context = GlobalPlatform.discover(card.getBasicChannel());
        } catch (GPException e) {
            error = e.getMessage();
            switch (e.sw) {
                case ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED:
                case ISO7816.SW_AUTHENTICATION_METHOD_BLOCKED:
                case 0x6283:
                    state = CardState.LOCKED;
                    break;
                default:
                    state = CardState.FAILED;
                    break;
            }
            if (card != null) {
                card.disconnect(true);
            }
            return;
        }

        try {
            secureConnect(context, (emv) ? PlaintextKeys.Diversification.EMV : null);
            command.setCardId(id);
            command.setGP(context);
            command.execute();
        } catch (GPException e) {
            error = e.getMessage();
            this.state = CardState.UNAUTHORIZED;
        } finally {
            card.disconnect(true);
        }
    }

    /**
     * Assumes that masterKey variable is set. Tries to secure connect with it
     * If successful, loads the card contents
     * This method may brick the card if bad masterKey set
     */
    private void secureConnect(GlobalPlatform context, PlaintextKeys.Diversification diversification)
            throws CardException, GPException {

        PlaintextKeys key = PlaintextKeys.fromMasterKey(new GPKey(HexUtils.hex2bin(masterKey), keyType));
        //todo SCP 01/02 not considered!!
        if (diversification != null) key.setDiversifier(diversification);
        //todo mode - now default only
        context.openSecureChannel(key, null, 0, GlobalPlatform.defaultMode.clone());
    }

    private GPKey.Type getType(String type) {
        switch (type) {
            case "RAW":
                return GPKey.Type.RAW;
            case "DES":
                return GPKey.Type.DES;
            case "DES3":
                return GPKey.Type.DES3;
            case "AES":
                return GPKey.Type.AES;
            case "RSAPUB":
                return GPKey.Type.RSAPUB;
            case "PSK":
                return GPKey.Type.PSK;
            default:
                return GPKey.Type.RAW;
        }
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
