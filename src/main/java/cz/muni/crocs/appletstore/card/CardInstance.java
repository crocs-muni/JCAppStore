package cz.muni.crocs.appletstore.card;

import apdu4j.HexUtils;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.command.GPCommand;
import cz.muni.crocs.appletstore.card.command.List;
import cz.muni.crocs.appletstore.util.HtmlLabel;
import cz.muni.crocs.appletstore.util.IniParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.gp.*;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

/**
 * Card instance of card inserted in selected terminal
 * provides all functionality over card communication
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardInstance {
    private static final Logger logger = LoggerFactory.getLogger(CardInstance.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private String masterKey;
    private String keyType;
    private String diversifier;
    private boolean doAuth = true;

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
        logger.info("Card plugged in:" + id);

        if (saveDetailsAndCheckMasterKey())
            getCardListWithSavedPassword();
        else
            getCardListWithDefaultPassword();

        updateCardAuth(true);
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
     *
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
                doAuth = parser.getValue(Config.INI_AUTHENTICATED).toLowerCase().equals("true");
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
        if (!(new File(Config.INI_CARD_TYPES).exists())) {
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
                if (!askDefault()) {
                    logger.warn("Card type not found: " + CardDetails.byteArrayToHexSpaces(details.getAtr().getBytes()).toLowerCase());
                    throw new LocalizedCardException("Could not auto-detect the card master key.", "E_master_key_not_found");
                }
                setTestPassword404f();
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
        if (!doAuth) throw new LocalizedCardException("Card not authenticated.", "H_not_authenticated");

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
        } catch (IllegalArgumentException il) {
            throw new LocalizedCardException(il.getMessage(), "no_channel", il);
        } catch (GPDataException ex) {
            throw new LocalizedCardException(ex.getMessage(), SW.getErrorCauseKey(ex.sw, "E_unknown_error"), ex);
        } catch (GPException e) {
            card.disconnect(true);
            throw new LocalizedCardException(e.getMessage(), SW.getErrorCauseKey(e.sw, "E_unknown_error"),e);
        }

        try {
            secureConnect(context);
        } catch (GPException e) {
            //ugly, but the GP is designed in a way it does not allow me to do otherwise
            if (e.getMessage().startsWith("STRICT WARNING: ")) {
                updateCardAuth(false);
                throw new LocalizedCardException(e.getMessage(), SW.getErrorCauseKey(e.sw, "E_secure_channel_error"), e);
            }
            throw new LocalizedCardException(e.getMessage(), SW.getErrorCauseKey(e.sw, "E_unknown_error"),e);
        }

        try {
            command.setCardId(id);
            command.setGP(context);
            command.execute();
        } catch (GPException e) {
            throw new LocalizedCardException(e.getMessage(), SW.getErrorCauseKey(e.sw, "E_unknown_error"),e);
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
        key.setDiversifier(getDiversifier(diversifier));
        context.openSecureChannel(key, null, 0, GlobalPlatform.defaultMode.clone());
    }

    /**
     * Convert string type to actual object
     *
     * @param diversifier diversification name
     * @return PlaintextKeys.Diversification diversification object
     */
    private static PlaintextKeys.Diversification getDiversifier(String diversifier) {
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

    private boolean askDefault() {
        RunnableFuture<Boolean> task = new FutureTask<>(() -> JOptionPane.showConfirmDialog(
                null,
                new HtmlLabel(textSrc.getString("I_use_default_keys_1") +
                        "<br>" + textSrc.getString("master_key") + ": <b>404142434445464748494A4B4C4D4E4F</b>" +
                        "<br>" + textSrc.getString("key_type") + ": <b>DES3</b>" +
                        textSrc.getString("I_use_default_keys_2")),
                textSrc.getString("key_not_found"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(Config.IMAGE_DIR + "")) == JOptionPane.YES_OPTION);
        SwingUtilities.invokeLater(task);
        try {
            return task.get();
        } catch (InterruptedException| ExecutionException ex) {
            ex.printStackTrace();
            return false;
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
