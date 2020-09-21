package cz.muni.crocs.appletstore.card;

import apdu4j.APDUBIBO;
import apdu4j.CardChannelBIBO;
import apdu4j.TerminalManager;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.command.ListContentsUnauthorized;
import cz.muni.crocs.appletstore.card.command.GPCommand;
import cz.muni.crocs.appletstore.iface.CallableParam;
import cz.muni.crocs.appletstore.iface.ProcessTrackable;
import cz.muni.crocs.appletstore.util.IniCardTypesParser;
import cz.muni.crocs.appletstore.util.IniCardTypesParserImpl;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.gp.*;

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
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardInstanceUnauthorizedImpl implements CardInstanceManagerExtension {
    private static final Logger logger = LoggerFactory.getLogger(CardInstanceUnauthorizedImpl.class);
    private static final ResourceBundle textSrc = ResourceBundle.getBundle("Lang",
            OptionsFactory.getOptions().getLanguageLocale());


    private final String id;
    private String name = "";
    private final CardDetails details;
    private final CardTerminal terminal;
    private CardInstanceMetaData metadata;
    private AID defaultSelected;

    private ProcessTrackable task;

    /**
     * Compares the card id and updates card data if needed
     * e.g. swaps the card instance
     *
     * @param newDetails of the card: ATR is a must, other optional. Data from GET_CATA APDU command.
     */
    CardInstanceUnauthorizedImpl(CardDetails newDetails, CardTerminal terminal)
            throws LocalizedCardException, CardException {
        if (newDetails == null || terminal == null) {
            logger.warn("NewDetails loaded " + (newDetails != null) + ", terminal: " + (terminal != null));
            throw new LocalizedCardException("Invalid arguments.", "E_load_card", "plug-in-out.jpg");
        }

        this.terminal = terminal;
        this.details = newDetails;
        id = "Unauthorized::" + CardDetails.getId(newDetails);
        logger.info("Card plugged in:" + id);

        reload();
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
        updateCardName(newName);
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
        return false;
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
                    TerminalManager.getExceptionMessage(e), "E_connect_fail");
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
            throw new LocalizedCardException(e.getMessage(), SW.getErrorCauseKey(e.sw, "E_unknown_error"), "error.png", e);
        } catch (IOException e) {
            throw new LocalizedCardException(e.getMessage(), "E_unknown_error", "plug-in-out.jpg", e);
        } finally {
            if (OptionsFactory.getOptions().is(Options.KEY_EXCLUSIVE_CARD_CONNECT))
                card.endExclusive();
            card.disconnect(true);
        }
    }

    @Override
    public void secureExecuteCommands(GPCommand<?>... commands) throws LocalizedCardException {
        throw new LocalizedCardException("secureExecuteCommands() call not allowed.", "E_unauthorized");
    }

    @Override
    public void setMetaData(CardInstanceMetaData metadata) {
        this.metadata = metadata;
    }

    @Override
    public void disableTemporarilyJCAlgTestFinder() {
        //do nothing
    }

    @Override
    public boolean shouldJCAlgTestFinderRun() {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    ///////////////////    PRIVATE ONLY (INTERNAL LOGIC)     ///////////////////
    ////////////////////////////////////////////////////////////////////////////

    private void reload() throws LocalizedCardException, CardException {
        ListContentsUnauthorized cmd =
                new ListContentsUnauthorized(new File(Config.DATA_DIR + "well_known_aids.ini"));
        executeCommands(cmd);
        metadata = cmd.getResult();
    }

    private void updateCardName(String name) throws LocalizedCardException {
        try {
            new IniCardTypesParserImpl(Config.CARD_LIST_FILE, id, textSrc.getString("ini_commentary"))
                    .addValue(IniCardTypesParser.TAG_NAME, name).store();
        } catch (IOException e) {
            throw new LocalizedCardException("Failed to save card info.", "E_card_details_failed", e);
        }
    }

    //todo save this card to cards.ini?
    private boolean saveDetails() throws LocalizedCardException {
//        IniCardTypesParserImpl parser;
//        try {
//            parser = new IniCardTypesParserImpl(Config.CARD_LIST_FILE, id, textSrc.getString("ini_commentary"));
//            if (parser.isHeaderPresent()) {
//                logger.info("Card " + id + " metadata found.");
//                name = parser.getValue(IniCardTypesParser.TAG_NAME);
//                return true;
//            }
//
//            logger.info("Card " + id + " saved into card list database.");
//            parser.addValue(IniCardTypesParser.TAG_NAME, name)
//                    .addValue(IniCardTypesParser.TAG_ATR, CardDetails.byteArrayToHexSpaces(details.getAtr().getBytes()))
//                    .addValue(IniCardTypesParser.TAG_CIN, details.getCin())
//                    .addValue(IniCardTypesParser.TAG_IIN, details.getIin())
//                    .addValue(IniCardTypesParser.TAG_CPLC, (details.getCplc() == null) ? null : details.getCplc().toString())
//                    .addValue(IniCardTypesParser.TAG_DATA, details.getCardData())
//                    .addValue(IniCardTypesParser.TAG_CAPABILITIES, details.getCardCapabilities())
//                    .addValue(IniCardTypesParser.TAG_KEY_INFO, details.getKeyInfo())
//                    .store();
//            return false;
//        } catch (IOException e) {
//            throw new LocalizedCardException("Unable to save new card details.", "E_card_details_failed");
//        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CardInstanceUnauthorizedImpl)) return false;
        return ((CardInstanceUnauthorizedImpl) obj).id.equals(this.id);
    }
}
