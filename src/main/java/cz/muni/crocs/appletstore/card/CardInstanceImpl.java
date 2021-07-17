package cz.muni.crocs.appletstore.card;

import apdu4j.APDUBIBO;
import apdu4j.HexUtils;
import apdu4j.*;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.action.CardDetectionAction;
import cz.muni.crocs.appletstore.card.command.GPCommand;
import cz.muni.crocs.appletstore.card.command.GetDefaultSelected;
import cz.muni.crocs.appletstore.card.command.ListContents;
import cz.muni.crocs.appletstore.iface.CallableParam;
import cz.muni.crocs.appletstore.iface.ProcessTrackable;
import cz.muni.crocs.appletstore.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.gp.*;
import pro.javacard.gp.PlaintextKeys.Diversification;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Card instance of card inserted in selected terminal
 * provides all functionality over card communication
 * todo: introduce base class that implements functionality of both authorized and unauthorized card instances
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardInstanceImpl implements CardInstanceManagerExtension {
    private static final Logger logger = LoggerFactory.getLogger(CardInstanceImpl.class);
    private static final ResourceBundle textSrc = ResourceBundle.getBundle("Lang",
            OptionsFactory.getOptions().getLanguageLocale());

    private String masterKey;
    private String kcv;
    private String diversifier;
    private boolean doAuth = true;
    private boolean doJCAlgTestFinder = true;

    private final String id;
    private String name = "";
    private final CardDetails details;
    private final CardTerminal terminal;
    private CardInstanceMetaData metadata;
    private AID defaultSelected;
    private boolean authenticated = false;

    private ProcessTrackable task;

    /**
     * Compares the card id and updates card data if needed
     * e.g. swaps the card instance
     *
     * @param newDetails of the card: ATR is a must, other optional. Data from GET_CATA APDU command.
     */
    CardInstanceImpl(CardDetails newDetails, CardTerminal terminal)
            throws LocalizedCardException, CardException, UnknownKeyException, CardNotAuthenticatedException {
        this(newDetails, terminal, false);
    }

    /**
     * Creates new card instance
     * @param newDetails of the card: ATR is a must, other optional. Data from GET_CATA APDU command.
     * @param terminal terminal used to talk to this card
     * @param defaultTestKey default test key value
     */
    CardInstanceImpl(CardDetails newDetails, CardTerminal terminal, boolean defaultTestKey)
            throws LocalizedCardException, CardException, UnknownKeyException, CardNotAuthenticatedException {
        if (newDetails == null || terminal == null) {
            logger.warn("NewDetails loaded " + (newDetails != null) + ", terminal: " + (terminal != null));
            throw new LocalizedCardException("Invalid arguments.", "E_load_card", "plug-in-out.jpg", ErrDisplay.FULL_SCREEN);
        }

        this.terminal = terminal;
        this.details = newDetails;
        id = CardDetails.getId(newDetails);
        logger.info("Card plugged in:" + id);

        reload(defaultTestKey);
    }

    @Override
    public CardInstanceMetaData getCardMetadata() {
        return metadata;
    }

    @Override
    public AppletInfo getInfoOf(AID aid) {
        if (aid == null) return null;
        Optional<AppletInfo> info = metadata.getApplets().stream().filter(a -> aid.equals(a.getAid())).findFirst();
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
    public ATR getCardATR() {
        return details == null ? null : details.getAtr();
    }

    @Override
    public Integer getLifeCycle() {
        if (metadata == null)
            return 0;

        AppletInfo sd = null;
        for (AppletInfo info : metadata.getApplets()) {
            if (info.getKind() == GPRegistryEntry.Kind.IssuerSecurityDomain) {
                return info.getLifecycle();
            } else if (info.getKind() == GPRegistryEntry.Kind.SecurityDomain) {
                sd = info;
            }
        }
        if (sd != null) return sd.getLifecycle();
        return -1;
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
        updateIniValue(IniCardTypesParser.TAG_NAME, newName);
    }

    @Override
    public void foreachAppletOf(GPRegistryEntry.Kind kind, CallableParam<Boolean, AppletInfo> call) {
        Set<AppletInfo> applets = getCardMetadata().getApplets();
        if (applets == null) return;
        for (AppletInfo nfo : getCardMetadata().getApplets()) {
            if (kind.equals(nfo.getKind())) {
                if (!call.callBack(nfo)) break;
            }
        }
    }

    @Override
    public boolean addTask(ProcessTrackable task) {
        if (task == null || isTask()) return false;
        this.task = task;
        this.task.run();
        return true;
    }

    @Override
    public boolean isTask() {
        return task != null && !task.finished();
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    ////////////////////////////////////////////////////////////////////////////
    ///////////////////  CardInstanceManagerExtension        ///////////////////
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public CardDetails getDetails() {
        return details;
    }

    @Override
    public void setDefaultSelected(AID defaultSelected) {
        this.defaultSelected = defaultSelected;
    }

    @Override
    public void saveInfoData() throws LocalizedCardException {
        AppletSerializer<CardInstanceMetaData> serializer = new AppletSerializerImpl();
        serializer.serialize(metadata, new File(Config.APP_DATA_DIR + Config.S + getId()));
    }

    @Override
    public void saveInfoData(List<AppletInfo> toSave) throws LocalizedCardException {
        metadata.removeInvalidApplets();
        for (AppletInfo info : toSave) {
            metadata.insertOrRewriteApplet(info);
        }
        saveInfoData();
    }

    @Override
    public void deletePackageData(final AppletInfo pkg) throws LocalizedCardException {
        deleteAppletData(pkg, false);
    }

    @Override
    public void deleteAppletData(final AppletInfo applet, boolean force) throws LocalizedCardException {
        logger.info("Delete applet metadata: " + applet.toString());
        metadata.deleteAppletInfo(applet.getAid());
        if (force && applet.getKind().equals(GPRegistryEntry.Kind.ExecutableLoadFile)) {
            for (AID aid : applet.getModules()) {
                metadata.deleteAppletInfo(aid);
            }
        }
        saveInfoData();
    }

    @Override
    public void executeCommands(GPCommand<?>... commands) throws LocalizedCardException, CardException {
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
                    TerminalManager.getExceptionMessage(e), "E_connect_fail", ErrDisplay.FULL_SCREEN);
        }

        try {
            for (GPCommand<?> command : commands) {
//                if (Thread.interrupted()) {
//                    throw new LocalizedCardException("Run out of time.", textSrc.getString("E_timeout"), "timer.png");
//                }
                logger.info("EXECUTING: " + command.getDescription());
                command.setChannel(channel);
                command.execute();
            }
        } catch (GPException e) {
            Tuple<String, ErrDisplay> swDesc = SW.getErrorCauseKey(e.sw, "E_unknown_error");
            throw new LocalizedCardException(e.getMessage(), swDesc.first, "error.png", e, swDesc.second);
        } catch (IOException e) {
            throw new LocalizedCardException(e.getMessage(), "E_unknown_error", "plug-in-out.jpg", e, ErrDisplay.FULL_SCREEN);
        } finally {
            if (OptionsFactory.getOptions().is(Options.KEY_EXCLUSIVE_CARD_CONNECT))
                card.endExclusive();
            card.disconnect(true);
        }
    }

    @Override
    public void secureExecuteCommands(GPCommand<?>... commands) throws LocalizedCardException, CardException {
        executeCommands(new GPCommand<Void>() {
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
                    throw new LocalizedCardException(il.getMessage(), "no_channel", "plug-in-out.jpg", il, ErrDisplay.FULL_SCREEN);
                } catch (GPException ex) {
                    Tuple<String, ErrDisplay> swDesc = SW.getErrorCauseKey(ex.sw, "E_fail_to_detect_sd");
                    throw new LocalizedCardException(ex.getMessage(), swDesc.first, "plug-in-out.png", ex, swDesc.second);
                } catch (IOException e) {
                    throw new LocalizedCardException(e.getMessage(), "E_fail_to_detect_sd", "plug-in-out.jpg", e, ErrDisplay.FULL_SCREEN);
                }

                try {
                    logger.info("Establishing secure channel.");
                    GPCardKeys key = PlaintextKeys.derivedFromMasterKey(HexUtils.hex2bin(masterKey), HexUtils.hex2bin(kcv), getDiversifier(diversifier));
                    context.openSecureChannel(key, null, null, GPSession.defaultMode.clone());
                    logger.info("Secure channel established.");
                } catch (IllegalArgumentException e) {
                    throw new LocalizedCardException(e.getMessage(), textSrc.getString("wrong_kcv"), e, ErrDisplay.FULL_SCREEN);
                } catch (GPException e) {
                    //ugly, but the GP is designed in a way it does not allow me to do otherwise
                    if (e.getMessage().startsWith("STRICT WARNING: ")) {
                        updateCardAuth(false);
                        throw new LocalizedCardException(e.getMessage(), "H_authentication", e, "lock_black.png",
                               CardDetectionAction::detectUnsafe, textSrc.getString("E_nokey_retry"), ErrDisplay.FULL_SCREEN);
                    }
                    Tuple<String, ErrDisplay> swDesc = SW.getErrorCauseKey(e.sw, "E_unknown_error");
                    throw new LocalizedCardException(e.getMessage(), swDesc.first, e, swDesc.second);
                }

                for (GPCommand<?> command : commands) {
//                    if (Thread.interrupted()) {
//                        throw new LocalizedCardException("Run out of time.", textSrc.getString("E_timeout"), "timer.png");
//                    }
                    logger.info("SECURE EXECUTING: " + command.getDescription());
                    command.setGP(context);
                    command.setChannel(channel);
                    command.execute();
                }
                return true;
            }
        });
    }

    @Override
    public void setMetaData(CardInstanceMetaData metadata) {
        this.metadata = metadata;
    }

    @Override
    public void disableTemporarilyJCAlgTestFinder() {
        doJCAlgTestFinder = false;
    }

    @Override
    public boolean shouldJCAlgTestFinderRun() {
        return doJCAlgTestFinder;
    }

    ////////////////////////////////////////////////////////////////////////////
    ///////////////////    PRIVATE ONLY (INTERNAL LOGIC)     ///////////////////
    ////////////////////////////////////////////////////////////////////////////

    private void reload(boolean useDefaultTestKey)
            throws LocalizedCardException, CardException, UnknownKeyException, CardNotAuthenticatedException {
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
            IniCardTypesParserImpl parser = new IniCardTypesParserImpl(Config.CARD_LIST_FILE, id, textSrc.getString("ini_commentary"));
            parser.addValue(IniCardTypesParser.TAG_NAME, name)
                    .addValue(IniCardTypesParser.TAG_KEY, masterKey)
                    .addValue(IniCardTypesParser.TAG_KEY_CHECK_VALUE, kcv)
                    .addValue(IniCardTypesParser.TAG_DIVERSIFIER, diversifier)
                    .addValue(IniCardTypesParser.TAG_AUTHENTICATED, authenticated ? "true" : "false")
                    .store();

            this.authenticated = authenticated;
        } catch (IOException e) {
            throw new LocalizedCardException("Failed to save card info.", "E_card_details_failed", e, ErrDisplay.BANNER);
        }
    }

    private void updateIniValue(String name, String value) throws LocalizedCardException {
        try {
            new IniCardTypesParserImpl(Config.CARD_LIST_FILE, id, textSrc.getString("ini_commentary"))
                    .addValue(name, value).store();
        } catch (IOException e) {
            throw new LocalizedCardException("Failed to save card info.", "E_card_details_failed", e, ErrDisplay.BANNER);
        }
    }

    /**
     * Open the ini file and try to find our card,
     * possibly save the card info
     *
     * @return true if card info present and custom master key is set
     */
    private boolean saveDetailsAndCheckMasterKey() throws LocalizedCardException {
        IniCardTypesParserImpl parser;
        try {
            parser = new IniCardTypesParserImpl(Config.CARD_LIST_FILE, id, textSrc.getString("ini_commentary"));
            if (parser.isHeaderPresent()) {
                logger.info("Card " + id + " metadata found.");
                name = parser.getValue(IniCardTypesParser.TAG_NAME);
                masterKey = parser.getValue(IniCardTypesParser.TAG_KEY);
                kcv = parser.getValue(IniCardTypesParser.TAG_KEY_CHECK_VALUE).toUpperCase();
                diversifier = parser.getValue(IniCardTypesParser.TAG_DIVERSIFIER).toUpperCase();
                doAuth = parser.getValue(IniCardTypesParser.TAG_AUTHENTICATED).toLowerCase().equals("true");

                boolean valid = validMasterKey(masterKey);
                logger.info("With valid master key: " + valid);
                return valid;
            }

            logger.info("Card " + id + " saved into card list database.");
            parser.addValue(IniCardTypesParser.TAG_NAME, name)
                    .addValue(IniCardTypesParser.TAG_KEY, "")
                    //key check value from provider, default none
                    .addValue(IniCardTypesParser.TAG_KEY_CHECK_VALUE, "")
                    //one of: <no_value>, EMV, KDF3, VISA2
                    .addValue(IniCardTypesParser.TAG_DIVERSIFIER, "")
                    .addValue(IniCardTypesParser.TAG_AUTHENTICATED, "true")
                    .addValue(IniCardTypesParser.TAG_ATR, CardDetails.byteArrayToHexSpaces(details.getAtr().getBytes()))
                    .addValue(IniCardTypesParser.TAG_CIN, details.getCin())
                    .addValue(IniCardTypesParser.TAG_IIN, details.getIin())
                    .addValue(IniCardTypesParser.TAG_CPLC, (details.getCplc() == null) ? null : details.getCplc().toString())
                    .addValue(IniCardTypesParser.TAG_DATA, details.getCardData())
                    .addValue(IniCardTypesParser.TAG_CAPABILITIES, details.getCardCapabilities())
                    .addValue(IniCardTypesParser.TAG_KEY_INFO, details.getKeyInfo())
                    .store();
            return false;
        } catch (IOException e) {
            throw new LocalizedCardException("Unable to save new card details.", "E_card_details_failed", ErrDisplay.BANNER);
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
    private void getCardListWithDefaultPassword(boolean useGeneric) throws LocalizedCardException, UnknownKeyException, CardException, CardNotAuthenticatedException {
        if (!(new File(Config.CARD_TYPES_FILE).exists())) {
            logger.error("Cad types file not found");
            //todo add image file not found
            throw new LocalizedCardException("No types present.", "E_missing_types", ErrDisplay.BANNER);
        }

        if (!useGeneric) {
            try {
                IniCardTypesParserImpl parser = new IniCardTypesParserImpl(Config.CARD_TYPES_FILE,
                        CardDetails.byteArrayToHexSpaces(details.getAtr().getBytes()).toLowerCase());
                if (parser.isHeaderPresent()) {
                    name = parser.getValue(IniCardTypesParser.TAG_NAME);
                    masterKey = parser.getValue(IniCardTypesParser.TAG_KEY);
                    kcv = parser.getValue(IniCardTypesParser.TAG_KEY_CHECK_VALUE).toUpperCase();
                    diversifier = parser.getValue(IniCardTypesParser.TAG_DIVERSIFIER).toUpperCase();
                    logger.info("Found test key by card type.");
                } else {
                    logger.info("No header present for card in card.ini file");
                    throw new UnknownKeyException(CardDetails.getId(details));
                }

                if (masterKey == null || masterKey.isEmpty()) {
                    logger.info("Ini file contains empty master key.");
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
    private void getCardListWithSavedPassword() throws LocalizedCardException, CardException, CardNotAuthenticatedException {
        //todo image that represents not trying to auth
        if (!doAuth)  throw new CardNotAuthenticatedException(id);

        GPCommand<CardInstanceMetaData> listContents = new ListContents(id);
        GPCommand<Optional<AID>> selected = new GetDefaultSelected();
        secureExecuteCommands(listContents, selected);
        metadata = listContents.getResult();
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
