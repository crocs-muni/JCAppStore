package cz.muni.crocs.appletstore.card;

import apdu4j.APDUBIBO;
import apdu4j.HexUtils;
import apdu4j.*;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.command.GPCommand;
import cz.muni.crocs.appletstore.card.command.GetDefaultSelected;
import cz.muni.crocs.appletstore.card.command.ListContents;
import cz.muni.crocs.appletstore.util.IniParser;
import cz.muni.crocs.appletstore.util.IniParserImpl;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.gp.*;
import pro.javacard.gp.PlaintextKeys.Diversification;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Card instance of card inserted in selected terminal
 * provides all functionality over card communication
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardInstanceImpl implements CardInstance {
    private static final Logger logger = LoggerFactory.getLogger(CardInstanceImpl.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private String masterKey;
    private String kcv;
    private String diversifier;
    private boolean doAuth = true;

    private final String id;
    private String name = "";
    private final CardDetails details;
    private final CardTerminal terminal;
    private Set<AppletInfo> applets;
    private AID defaultSelected;

    /**
     * Compares the card id and updates card data if needed
     * e.g. swaps the card instance
     *
     * @param newDetails of the card: ATR is a must, other optional
     */
    CardInstanceImpl(CardDetails newDetails, CardTerminal terminal)
            throws LocalizedCardException, CardException, UnknownKeyException {
        this(newDetails, terminal, false);
    }

    CardInstanceImpl(CardDetails newDetails, CardTerminal terminal, boolean defaultTestKey)
            throws LocalizedCardException, CardException, UnknownKeyException {
        if (newDetails == null || terminal == null) {
            logger.warn("NewDetails loaded " + (newDetails != null) + ", terminal: " + (terminal != null));
            throw new LocalizedCardException("Invalid arguments.", "E_load_card");
        }

        this.terminal = terminal;
        this.details = newDetails;
        id = CardDetails.getId(newDetails);
        logger.info("Card plugged in:" + id);

        reload(defaultTestKey);
    }

    @Override
    public Set<AppletInfo> getInstalledApplets() {
        return Collections.unmodifiableSet(getApplets());
    }

    @Override
    public AppletInfo getInfoOf(AID aid) {
        if (aid == null) return null;
        Optional<AppletInfo> info = getApplets().stream().filter(a -> aid.equals(a.getAid())).findFirst();
        return info.orElse(null);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescriptor() {
        return name == null || name.isEmpty() ? getId() : name + "  " + getId();
    }

    @Override
    public Integer getLifeCycle() {
        if (applets == null)
            return 0;

        for (AppletInfo info : applets) {
            if (info.getKind() == GPRegistryEntry.Kind.IssuerSecurityDomain) {
                return info.getLifecycle();
            }
        }
        throw new Error("Should not end here.");
    }

    @Override
    public AID getDefaultSelected() {
        return defaultSelected;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String newName) throws LocalizedCardException {
        name = newName;
        updateCardName(newName);
    }

    ////////////////////////////////////////////////////////////////////////////
    ///////////////////  PACKAGE VISIBLE ONLY (FOR MANAGER)  ///////////////////
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Set default selected applet of the card
     * @param defaultSelected applet that is default selected on card
     */
    void setDefaultSelected(AID defaultSelected) {
        this.defaultSelected = defaultSelected;
    }

    /**
     * Executes any desired command without establishing secure channel
     * @param commands commands to execute
     * @throws LocalizedCardException unable to perform command
     * @throws CardException unable to perform command
     */
    void executeCommands(GPCommand... commands) throws LocalizedCardException, CardException {
        Card card;
        APDUBIBO channel;

        try {
            card = terminal.connect("*");
            if (OptionsFactory.getOptions().is(Options.KEY_EXCLUSIVE_CARD_CONNECT))
                card.beginExclusive();
            logger.info("Connected to the terminal: " + terminal + ", card: " + card);
            channel = CardChannelBIBO.getBIBO(card.getBasicChannel());
            logger.info("Card BIBO channel obtained: " + channel);

        } catch (CardException e) {
            throw new LocalizedCardException("Could not connect to selected reader: " +
                    TerminalManager.getExceptionMessage(e), "E_connect_fail");
        }

        try {
            for (GPCommand command : commands) {
                logger.info("EXECUTING: " + command.getDescription());
                command.setChannel(channel);
                command.execute();
            }
            if (OptionsFactory.getOptions().is(Options.KEY_EXCLUSIVE_CARD_CONNECT))
                card.endExclusive();
        } catch (GPException e) {
            if (OptionsFactory.getOptions().is(Options.KEY_EXCLUSIVE_CARD_CONNECT))
                card.endExclusive();
            throw new LocalizedCardException(e.getMessage(), SW.getErrorCauseKey(e.sw, "E_unknown_error"), e);
        } catch (IOException e) {
            if (OptionsFactory.getOptions().is(Options.KEY_EXCLUSIVE_CARD_CONNECT))
                card.endExclusive();
            throw new LocalizedCardException(e.getMessage(), "E_unknown_error", e);
        } finally {
            card.disconnect(true);
        }
    }

    /**
     * Executes any desired command using secure channel
     * @param commands commands to execute
     * @throws CardException unable to perform command
     */
    void secureExecuteCommands(GPCommand... commands) throws LocalizedCardException, CardException {
        executeCommands(new GPCommand() {
            @Override
            public String getDescription() {
                return "Secure channel.";
            }

            @Override
            public boolean execute() throws CardException, GPException, LocalizedCardException, IOException {
                try {
                    logger.info("Discovering channel.");
                    context = GPSession.discover(channel);
                } catch (IllegalArgumentException il) {
                    throw new LocalizedCardException(il.getMessage(), "no_channel", il);
                } catch (GPException ex) {
                    throw new LocalizedCardException(ex.getMessage(), SW.getErrorCauseKey(ex.sw, "E_fail_to_detect_sd"), ex);
                } catch (IOException e) {
                    throw new LocalizedCardException(e.getMessage(), "E_fail_to_detect_sd", e);
                }

                try {
                    logger.info("Establishing secure channel.");
                    GPCardKeys key = PlaintextKeys.derivedFromMasterKey(HexUtils.hex2bin(masterKey), HexUtils.hex2bin(kcv), getDiversifier(diversifier));
                    context.openSecureChannel(key, null, null, GPSession.defaultMode.clone());
                    logger.info("Secure channel established.");
                } catch (IllegalArgumentException e) {
                    throw new LocalizedCardException(e.getMessage(), textSrc.getString("wrong_kcv"), e);
                } catch (GPException e) {
                    //ugly, but the GP is designed in a way it does not allow me to do otherwise
                    if (e.getMessage().startsWith("STRICT WARNING: ")) {
                        updateCardAuth(false);
                        throw new LocalizedCardException(e.getMessage(), SW.getErrorCauseKey(e.sw, "H_authentication"), e);
                    }
                    throw new LocalizedCardException(e.getMessage(), SW.getErrorCauseKey(e.sw, "E_unknown_error"), e);
                }

                for (GPCommand command : commands) {
                    logger.info("SECURE EXECUTING: " + command.getDescription());
                    command.setGP(context);
                    command.setChannel(channel);
                    command.execute();
                }
                return true;
            }
        });
    }

    /**
     * Modifiable access for local classes
     *
     * @return modifiable applet list
     */
    Set<AppletInfo> getApplets() {
        return applets;
    }

    /**
     * Used to update applet list on install
     * @param applets applet info list to set
     */
    void setApplets(Set<AppletInfo> applets) {
        this.applets = applets;
    }

    ////////////////////////////////////////////////////////////////////////////
    ///////////////////    PRIVATE ONLY (INTERNAL LOGIC)     ///////////////////
    ////////////////////////////////////////////////////////////////////////////

    private void reload(boolean useDefaultTestKey)
            throws LocalizedCardException, CardException, UnknownKeyException {
        if (useDefaultTestKey) {
            setTestPassword404f();
        }

        if (!useDefaultTestKey && saveDetailsAndCheckMasterKey())
            getCardListWithSavedPassword();
        else
            getCardListWithDefaultPassword(useDefaultTestKey);

        updateCardAuth(true);
    }

    private void setTestPassword404f() {
        masterKey = "404142434445464748494A4B4C4D4E4F";
        kcv = "";
        diversifier = "";
    }

    private void updateCardAuth(boolean authenticated) throws LocalizedCardException {
        try {
            IniParserImpl parser = new IniParserImpl(Config.CARD_LIST_FILE, id, textSrc.getString("ini_commentary"));
            parser.addValue(IniParser.TAG_NAME, name)
                    .addValue(IniParser.TAG_KEY, masterKey)
                    .addValue(IniParser.TAG_KEY_CHECK_VALUE, kcv)
                    .addValue(IniParser.TAG_DIVERSIFIER, diversifier)
                    .addValue(IniParser.TAG_AUTHENTICATED, authenticated ? "true" : "false")
                    .store();
        } catch (IOException e) {
            throw new LocalizedCardException("Failed to save card info.", "E_card_details_failed", e);
        }
    }

    private void updateCardName(String name) throws LocalizedCardException {
        try {
            new IniParserImpl(Config.CARD_LIST_FILE, id, textSrc.getString("ini_commentary"))
                    .addValue(IniParser.TAG_NAME, name).store();
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
            parser = new IniParserImpl(Config.CARD_LIST_FILE, id, textSrc.getString("ini_commentary"));
            if (parser.isHeaderPresent()) {
                logger.info("Card " + id + " metadata found.");
                name = parser.getValue(IniParser.TAG_NAME);
                masterKey = parser.getValue(IniParser.TAG_KEY);
                kcv = parser.getValue(IniParser.TAG_KEY_CHECK_VALUE).toUpperCase();
                diversifier = parser.getValue(IniParser.TAG_DIVERSIFIER).toUpperCase();
                doAuth = parser.getValue(IniParser.TAG_AUTHENTICATED).toLowerCase().equals("true");

                boolean valid = validMasterKey(masterKey);
                logger.info("With valid master key: " + valid);
                return valid;
            }

            logger.info("Card " + id + " saved into card list database.");
            parser.addValue(IniParser.TAG_NAME, name)
                    .addValue(IniParser.TAG_KEY, "")
                    //key check value from provider, default none
                    .addValue(IniParser.TAG_KEY_CHECK_VALUE, "")
                    //one of: <no_value>, EMV, KDF3, VISA2
                    .addValue(IniParser.TAG_DIVERSIFIER, "")
                    .addValue(IniParser.TAG_AUTHENTICATED, "true")
                    .addValue(IniParser.TAG_ATR, CardDetails.byteArrayToHexSpaces(details.getAtr().getBytes()))
                    .addValue(IniParser.TAG_CIN, details.getCin())
                    .addValue(IniParser.TAG_IIN, details.getIin())
                    .addValue(IniParser.TAG_CPLC, (details.getCplc() == null) ? null : details.getCplc().toString())
                    .addValue(IniParser.TAG_DATA, details.getCardData())
                    .addValue(IniParser.TAG_CAPABILITIES, details.getCardCapabilities())
                    .addValue(IniParser.TAG_KEY_INFO, details.getKeyInfo())
                    .store();
            return false;
        } catch (IOException e) {
            throw new LocalizedCardException("Unable to save new card details.", "E_card_details_failed");
        }
    }

    private static boolean validMasterKey(String key) {
        //key is either 3DES or AES with the length of 128/256 bit = 8/16 bytes = 16/32 chars
        //todo find out which key version the gppro uses aes or 3des
        return key != null && !key.isEmpty() && key.length()% 2 == 0 && (key.length() == 32 || key.length() == 16);
    }

    /**
     * Open card types INI and searches by ATR for default password
     * extract functionality into one connection process
     *
     * @param useGeneric true if 40..4F key should be used
     */
    private void getCardListWithDefaultPassword(boolean useGeneric) throws LocalizedCardException, UnknownKeyException, CardException {
        if (!(new File(Config.CARD_TYPES_FILE).exists())) {
            logger.error("Cad types file not found");
            throw new LocalizedCardException("No types present.", "E_missing_types");
        }

        if (!useGeneric) {
            try {
                IniParserImpl parser = new IniParserImpl(Config.CARD_TYPES_FILE,
                        CardDetails.byteArrayToHexSpaces(details.getAtr().getBytes()).toLowerCase());
                if (parser.isHeaderPresent()) {
                    name = parser.getValue(IniParser.TAG_NAME);
                    masterKey = parser.getValue(IniParser.TAG_KEY);
                    kcv = parser.getValue(IniParser.TAG_KEY_CHECK_VALUE).toUpperCase();
                    diversifier = parser.getValue(IniParser.TAG_DIVERSIFIER).toUpperCase();
                    logger.info("Found test key by card type.");
                } else {
                    throw new UnknownKeyException(CardDetails.getId(details));
                }

                if (masterKey == null || masterKey.isEmpty()) {
                    throw new UnknownKeyException(CardDetails.getId(details));
                }
            } catch (IOException e) {
                logger.error("Could not open card types ini file", e);
                e.printStackTrace();
            }
        }
        logger.info("Using default test key found by card type for authentication.");
        getCardListWithSavedPassword();
    }

    /**
     * Open card types INI and searches by ATR for default password
     */
    private void getCardListWithSavedPassword() throws LocalizedCardException, CardException {
        if (!doAuth) throw new LocalizedCardException("Card not authenticated.", "H_not_authenticated");

        GPCommand<Set<AppletInfo>> listContents = new ListContents(id);
        GPCommand<Optional<AID>> selected = new GetDefaultSelected();
        secureExecuteCommands(listContents, selected);
        applets = listContents.getResult();
        defaultSelected = selected.getResult().orElse(null);
    }

    /**
     * Convert string type to actual object
     *
     * @param diversifier diversification name
     * @return PlaintextKeys.Diversification diversification object
     */
    private static Diversification getDiversifier(String diversifier) {
        switch (diversifier) {
            case "EMV":
                return Diversification.EMV;
            case "KDF3":
                return Diversification.KDF3;
            case "VISA2":
                return Diversification.VISA2;
            default:
                return Diversification.NONE;
        }
    }

//    private boolean askDefault() {
//        RunnableFuture<Boolean> task = new FutureTask<>(() -> JOptionPane.showConfirmDialog(
//                null,
//                new HtmlText(textSrc.getString("I_use_default_keys_1") +
//                        "<br>" + textSrc.getString("master_key") + ": <b>404142434445464748494A4B4C4D4E4F</b>" +
//                        textSrc.getString("I_use_default_keys_2")),
//                textSrc.getString("key_not_found"),
//                JOptionPane.OK_CANCEL_OPTION,
//                JOptionPane.INFORMATION_MESSAGE,
//                new ImageIcon(Config.IMAGE_DIR + "")) == JOptionPane.YES_OPTION);
//        SwingUtilities.invokeLater(task);
//        try {
//            return task.get();
//        } catch (InterruptedException | ExecutionException ex) {
//            ex.printStackTrace();
//            return false;
//        }
//    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CardInstanceImpl)) return false;
        return ((CardInstanceImpl) obj).id.equals(this.id);
    }
}
