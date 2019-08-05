package cz.muni.crocs.appletstore.card;

import apdu4j.HexUtils;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.command.GPCommand;
import cz.muni.crocs.appletstore.card.command.List;
import cz.muni.crocs.appletstore.util.IniParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.gp.GPException;

import pro.javacard.gp.GPKey;
import pro.javacard.gp.GlobalPlatform;
import pro.javacard.gp.PlaintextKeys;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Card instance of card inserted in selected terminal
 * provides all functionality over card communication
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardInstance {

    private static final Logger logger = LoggerFactory.getLogger(CardInstance.class);

    private String masterKey;
    private String keyType;
    private String diversifier;
    private boolean auth = true;

    public static final int CUSTOM_ERROR_BYTE = 0;

    private final String id;
    private String name = "";
    private final CardDetails details;
    private final CardTerminal terminal;
    private ArrayList<AppletInfo> applets;

    /**
     * Compares the card id and updates card data if needed
     * e.g. swaps the card instance
     *
     * @param newDetails of the card: ATR is a must, other optional
     */
    CardInstance(CardDetails newDetails, CardTerminal terminal) throws LocalizedCardException, CardException {
        if (newDetails == null || terminal == null) {
            logger.warn("NewDetails loaded " + (newDetails != null) + ", terminal: " + (terminal != null));
            throw new LocalizedCardException("Invalid arguments.", "E_load_card");
        }
        this.terminal = terminal;
        this.details = newDetails;
        id = CardDetails.getId(newDetails);

        try {
            if (saveDetailsAndCheckMasterKey())
                getCardListWithSavedPassword();
            else
                getCardListWithDefaultPassword();

            updateCardAuth(true);
        } catch (LocalizedCardException | CardException e) {
            updateCardAuth(false);
            logger.warn("Secure channel failed: ", e);
            throw e;
        }
    }

    public String getId() {
        return id;
    }

    private void setTestPassword404f() {
        masterKey = "404142434445464748494A4B4C4D4E4F";
        keyType = "DES3";
        diversifier = "";
    }

    /**
     * Modifiable access for local classes
     * @return modifiable applet list
     */
    java.util.List<AppletInfo> getApplets() {
        return applets;
    }

    void removeAppletInfo(AppletInfo info) {
        for (AppletInfo appletInfo : applets) {
            if (appletInfo.getAid().equals(info.getAid())) {
                applets.remove(appletInfo);
                return;
            }
        }
    }

    public String getName() {
        return name;
    }

    private void updateCardAuth(boolean authenticated) throws LocalizedCardException {
        try {
            IniParserImpl parser = new IniParserImpl(Config.INI_CARD_LIST, id);
            parser.addValue(Config.INI_NAME, name)
                    .addValue(Config.INI_KEY, masterKey)
                    .addValue(Config.INI_KEY_TYPE, keyType)
                    .addValue(Config.INI_DIVERSIFIER, diversifier)
                    .addValue(Config.INI_AUTHENTICATED, authenticated ? "true" : "false")
                    .store();
        } catch (IOException e) {
            throw new LocalizedCardException("Failed to save card info.", "E_card_details_failed", e);
        }
    }

    /**
     * Open the ini file and try to find our card,
     * possibly save the card info
     *
     * @return true if card info present and custom master key is set
     */
    private boolean saveDetailsAndCheckMasterKey() throws LocalizedCardException {
        IniParserImpl parser;
        try {
            parser = new IniParserImpl(Config.INI_CARD_LIST, id);
            if (parser.isHeaderPresent()) {
                name = parser.getValue(Config.INI_NAME);
                masterKey = parser.getValue(Config.INI_KEY);
                keyType = parser.getValue(Config.INI_KEY_TYPE).toUpperCase();
                diversifier = parser.getValue(Config.INI_DIVERSIFIER).toUpperCase();
                auth = parser.getValue(Config.INI_AUTHENTICATED).toLowerCase().equals("true");
                return !(masterKey == null || masterKey.isEmpty());
            }

            logger.info("Card " + id + " saved into card list database.");
            parser.addValue(Config.INI_NAME, name)
                    .addValue(Config.INI_KEY, "")
                    //one of: RAW, DES, DES3, AES, RSAPUB, PSK
                    .addValue(Config.INI_KEY_TYPE, "")
                    //one of: <no_value>, EMV, KDF3, VISA2
                    .addValue(Config.INI_DIVERSIFIER, "")
                    .addValue(Config.INI_AUTHENTICATED, "true")
                    .addValue(Config.INI_ATR, CardDetails.byteArrayToHexSpaces(details.getAtr().getBytes()))
                    .addValue(Config.INI_CIN, details.getCin())
                    .addValue(Config.INI_IIN, details.getIin())
                    .addValue(Config.INI_CPLC, (details.getCplc() == null) ? null : details.getCplc().toString())
                    .addValue(Config.INI_DATA, details.getCardData())
                    .addValue(Config.INI_CAPABILITIES, details.getCardCapabilities())
                    .addValue(Config.INI_KEY_INFO, details.getKeyInfo())
                    .store();
            return false;
        } catch (IOException e) {
            throw new LocalizedCardException("Unable to save new card details.", "E_card_details_failed");
        }
    }

    /**
     * Open card types INI and searches by ATR for default password
     * extract functionality into one connection process
     */
    private void getCardListWithDefaultPassword() throws LocalizedCardException, CardException {
        if (! (new File(Config.INI_CARD_TYPES).exists())) {
            throw new LocalizedCardException("No types present.", "E_missing_types");
        }

        try {
            IniParserImpl parser = new IniParserImpl(Config.INI_CARD_TYPES,
                    CardDetails.byteArrayToHexSpaces(details.getAtr().getBytes()).toLowerCase());
            if (parser.isHeaderPresent()) {
                name = parser.getValue(Config.INI_NAME);
                masterKey = parser.getValue(Config.INI_KEY);
                keyType = parser.getValue(Config.INI_KEY_TYPE).toUpperCase();
                diversifier = parser.getValue(Config.INI_DIVERSIFIER).toUpperCase();
            } else {
                logger.warn("Card type not found: " + CardDetails.byteArrayToHexSpaces(details.getAtr().getBytes()).toLowerCase());
                throw new LocalizedCardException("Could not auto-detect the card master key.", "E_master_key_not_found");
            }
            if (masterKey == null || masterKey.isEmpty()) {
                setTestPassword404f();
            }
            getCardListWithSavedPassword();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Open card types INI and searches by ATR for default password
     */
    private void getCardListWithSavedPassword() throws LocalizedCardException, CardException {
        if (!auth) throw new LocalizedCardException("Card not authenticated.", "H_not_authenticated");

        List list = new List();
        executeCommand(list);
        applets = list.getResult();
    }

    /**
     * Executes any desired command using secure channel
     *
     * @param command command instance to execute
     * @throws CardException unable to perform command
     */
    void executeCommand(GPCommand command) throws LocalizedCardException, CardException {

        Card card = null;
        GlobalPlatform context = null;

        try {
            card = terminal.connect("*");
            context = GlobalPlatform.discover(card.getBasicChannel());

        } catch (GPException e) {
            card.disconnect(true);
            //todo look into the discover() method for possibility of translation
            throw new LocalizedCardException("Failed to indetify card.", e);
        }

        try {
            secureConnect(context);
            command.setCardId(id);
            command.setGP(context);
            command.execute();
        } catch (GPException e) {
            throw new LocalizedCardException("Card command failed: " + e.getMessage(), e);
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
        key.setDiversifier(getDivesifier(diversifier));
        context.openSecureChannel(key, null, 0, GlobalPlatform.defaultMode.clone());
    }

    /**
     * Convert string type to actual object
     *
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
     *
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
