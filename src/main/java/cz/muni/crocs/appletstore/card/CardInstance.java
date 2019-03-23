package cz.muni.crocs.appletstore.card;

import apdu4j.HexUtils;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.command.GPCommand;
import cz.muni.crocs.appletstore.card.command.GetDetails;
import cz.muni.crocs.appletstore.card.command.List;
import cz.muni.crocs.appletstore.util.IniParser;
import cz.muni.crocs.appletstore.util.Sources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(CardInstance.class);

    private String masterKey;
    private String keyType;
    private String divesifier;
    private boolean auth = true;

    public static final String NO_CARD = "";
    private String id = NO_CARD;
    public static final int CUSTOM_ERROR_BYTE = 0;

    private CardDetails details;
    private CardTerminal terminal;
    private ArrayList<AppletInfo> applets;

    //CARD STATE
    public enum CardState {
        OK, WORKING, FAILED
    }
    private CardState state = CardState.OK;
    public CardState getState() {
        return state;
    }

    //ERROR DETECTION
    private int errorByte;
    private String errorTitle;
    private String error;
    public void setError(int errorByte, String errorTitle, String errorBody) {
        this.errorByte = errorByte;
        this.errorTitle = errorTitle;
        this.error = errorBody;
    }
    public String getErrorBody() {
        return error;
    }
    public String getErrorTitle() {
        return errorTitle;
    }
    public int getErrorByte() {
        return errorByte;
    }

    /**
     * Force to reload card by deleting card id
     */
    public void setRefresh() {
        this.id = NO_CARD;
    }
    private void cleanWith(CardState state) {
        this.details = null;
        this.masterKey = null;
        this.state = state;
        this.applets = null;
    }
    private void setTestPasword404f() {
        masterKey = "404142434445464748494A4B4C4D4E4F";
        keyType = "DES3";
        divesifier = "";
    }

    public ArrayList<AppletInfo> getApplets() {
        return applets;
    }

    /**
     * Performs the only insecure channel use
     * to get data from inserted card
     */
    public CardDetails getCardInfo(CardTerminal terminal) {
        try {
            //todo use logging card terminal?
            Card card = terminal.connect("*");

            card.beginExclusive();
            GetDetails command = new GetDetails(card.getBasicChannel());
            command.execute();
            card.endExclusive();

            CardDetails details = command.getOuput();
            details.setAtr(card.getATR());

            card.disconnect(false);
            return details;

        } catch (CardException e) {
            state = CardState.FAILED;

            String errormsg = e.getMessage().substring(36);
            System.out.println("PODIVEJ SE" + errormsg);

            //todo ugly, but no code management
            switch (errormsg) {
                case "SCardConnect got response 0x80100066":
                    setError(CUSTOM_ERROR_BYTE, Sources.language.get("E_no_reponse"), Sources.language.get("E_card_no_response"));
                case "SCardConnect got response 0x80100068":
                    //card ejected ignore this error
                    cleanWith(CardState.OK);
                    setRefresh();
                default:
                    setError(CUSTOM_ERROR_BYTE, Sources.language.get("E_unkown"), Sources.language.get("W_no_translation") + e.getMessage());
            }
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Compares the card id and updates card data if needed
     * e.g. swaps the card instance
     *
     * @param newDetails of the card: ATR is a must, other optional
     */
    public void update(CardDetails newDetails, CardTerminal terminal, boolean force) {
        this.terminal = terminal;

        if (newDetails == null || terminal == null) {
            cleanWith(state);
            return;
        }

        String newId = CardDetails.getId(newDetails);
        if (this.id.equals(newId) && !force) {
            return;
        }
        details = newDetails;
        id = newId;
        this.masterKey = null; //possible present key from previous cards

        try {
            if (saveDetailsAndCheckMasterKey())
                getCardListWithSavedPassword();
            else
                getCardListWithDefaultPassword();

            updateCardAuth(true);
        } catch (IOException | CardException ex) {
            updateCardAuth(false);
            logger.warn("Secure channel failed: ", ex);
            cleanWith(CardState.FAILED);
        }
    }

    private void updateCardAuth(boolean authenticated) {
        try {
            IniParser parser = new IniParser(Config.INI_CARD_LIST, id);
            parser.addValue(Config.INI_KEY, masterKey)
                    .addValue(Config.INI_KEY_TYPE, keyType)
                    .addValue(Config.INI_DIVERSIFIER, divesifier)
                    .addValue(Config.INI_AUTHENTICATED, authenticated ? "true" : "false")
                    .store();
        } catch (IOException e) {
            e.printStackTrace();
            //todo make some unified notification system
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
            masterKey = parser.getValue(Config.INI_KEY);
            keyType = parser.getValue(Config.INI_KEY_TYPE).toUpperCase();
            divesifier = parser.getValue(Config.INI_DIVERSIFIER).toUpperCase();
            auth = parser.getValue(Config.INI_AUTHENTICATED).toLowerCase().equals("true");
            return !(masterKey == null || masterKey.isEmpty());
        }

        logger.info("Card " + id + " saved into card list database.");
        parser.addValue(Config.INI_KEY, "")
                //one of: RAW, DES, DES3, AES, RSAPUB, PSK
                .addValue(Config.INI_KEY_TYPE, "")
                //one of: <no_value>, EMV, KDF3, VISA2
                .addValue(Config.INI_DIVERSIFIER, "")
                .addValue(Config.INI_AUTHENTICATED, "")
                .addValue(Config.INI_ATR, CardDetails.byteArrayToHexSpaces(details.getAtr().getBytes()))
                .addValue(Config.INI_CIN, details.getCin())
                .addValue(Config.INI_IIN, details.getIin())
                .addValue(Config.INI_CPLC, (details.getCplc() == null) ? null : details.getCplc().toString())
                .addValue(Config.INI_DATA, details.getCardData())
                .addValue(Config.INI_CAPABILITIES, details.getCardCapabilities())
                .addValue(Config.INI_KEY_INFO, details.getKeyInfo())
                .addValue(Config.INI_INSTALLED, "[]")
                .store();
        return false;
    }

    /**
     * Open card types INI and searches by ATR for default password
     * extract functionality into one connection process
     */
    private void getCardListWithDefaultPassword() throws CardException {

        try {
            IniParser parser = new IniParser(Config.INI_CARD_TYPES,
                    CardDetails.byteArrayToHexSpaces(details.getAtr().getBytes()).toLowerCase());
            if (parser.isHeaderPresent()) {
                masterKey = parser.getValue(Config.INI_KEY);
                keyType = parser.getValue(Config.INI_KEY_TYPE).toUpperCase();
                divesifier = parser.getValue(Config.INI_DIVERSIFIER).toUpperCase();
            } else {
                throw new CardException("Could not auto-detect the card master key.");
            }
            if (masterKey == null || masterKey.isEmpty()) {
                setTestPasword404f();
            }
            getCardListWithSavedPassword();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Open card types INI and searches by ATR for default password
     */
    private void getCardListWithSavedPassword() throws CardException {
        if (! auth) {
            cleanWith(CardState.FAILED);
            setError(CUSTOM_ERROR_BYTE, Sources.language.get("E_communication"), Sources.language.get("H_authentication"));
            return;
        }

        List list = new List();
        executeCommand(list);
        applets = list.getResult();
    }

    /**
     * Executes any desired command using secure channel
     * TODO: (CENC+CMAC) security levels check if meets
     *
     * @param command command instance to execute
     * @throws CardException unable to perform command
     */
    public void executeCommand(GPCommand command) throws CardException {
        state = CardState.WORKING;

        Card card = null;
        GlobalPlatform context = null;

        try {
            card = terminal.connect("*");
            context = GlobalPlatform.discover(card.getBasicChannel());

        } catch (GPException e) {
            error = e.getMessage();
            errorByte = e.sw;
            state = CardState.FAILED;

            if (card != null) {
                card.disconnect(true);
            }
            return;
        }

        try {
            secureConnect(context);
            command.setCardId(id);
            command.setGP(context);
            command.execute();
            this.state = CardState.OK;
        } catch (GPException e) {
            logger.warn("Secure channel failed: ", e);
            e.printStackTrace();
            error = e.getMessage();
            errorByte = e.sw;

            cleanWith(CardState.FAILED);
            updateCardAuth(false);
        } finally {
            card.disconnect(true);
        }
    }

    /**
     * Assumes that masterKey variable is set. Tries to secure connect with it
     * If successful, loads the card contents
     * This method may brick the card if bad masterKey set
     */
    private void secureConnect(GlobalPlatform context) throws CardException, GPException {
        PlaintextKeys key = PlaintextKeys.fromMasterKey(new GPKey(HexUtils.hex2bin(masterKey), getType(keyType)));
        key.setDiversifier(getDivesifier(divesifier));
        context.openSecureChannel(key, null, 0, GlobalPlatform.defaultMode.clone());
    }

    /**
     * Convert string type to actual object
     * @param diversifier diversification name
     * @return PlaintextKeys.Diversification diversification object
     */
    private static PlaintextKeys.Diversification getDivesifier(String diversifier) {
        switch (diversifier) {
            case "EMV":
                return PlaintextKeys.Diversification.EMV;
            case "KDF3":
                return PlaintextKeys.Diversification.KDF3;
            case "VISA2":
                return PlaintextKeys.Diversification.VISA2;
            default:
                return null;
        }
    }

    /**
     * Convert string type to actual object
     * @param type type of key
     * @return GPKey.Type key type
     */
    private static GPKey.Type getType(String type) {
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
