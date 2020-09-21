package cz.muni.crocs.appletstore.card;

import apdu4j.APDUBIBO;
import apdu4j.CardChannelBIBO;
import apdu4j.TerminalManager;
import apdu4j.ResponseAPDU;

import cz.muni.crocs.appletstore.card.command.*;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import pro.javacard.AID;
import pro.javacard.CAPFile;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GPRegistryEntry;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * Manager providing all functionality over card
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardManagerImpl implements CardManager {

    private static final Logger logger = LoggerFactory.getLogger(CardManagerImpl.class);
    private static final LogOutputStream loggerStream = new LogOutputStream(logger, Level.INFO);
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private final Terminals terminals = new Terminals();
    private CardInstanceManagerExtension card;
    private String lastCardId = textSrc.getString("no_last_card");
    private AID selectedAID = null;
    private String[] lastInstalledAIDs = null;
    private boolean tryGeneric = false;

    private CallBack<Void> notifier;

    private static final Object lock = new Object();

    @Override
    public boolean isCard() {
        return terminals.getState() == Terminals.TerminalState.OK && card != null;
    }

    @Override
    public CardInstance getCard() {
        return isCard() ? card : null;
    }

    @Override
    public void switchAppletStoreSelected(AID aid) {
        if (card == null) {
            selectedAID = null;
            return;
        }

        if (aid == null || aid.equals(selectedAID)) {
            selectedAID = null;
            aid = null;
        }

        if (card.getCardMetadata() == null)
            return;
        this.selectedAID = aid;
    }

    @Override
    public boolean isAppletStoreSelected() {
        return selectedAID != null;
    }

    @Override
    public boolean isAppletStoreSelected(AID applet) {
        return applet != null && applet.equals(selectedAID);
    }

    @Override
    public Terminals.TerminalState getTerminalState() {
        return terminals.getState();
    }

    @Override
    public Set<String> getTerminals() {
        return terminals.getTerminals().keySet();
    }

    @Override
    public CardTerminal getSelectedTerminal() {
        return terminals.getTerminal();
    }

    @Override
    public String getSelectedTerminalName() {
        return terminals.getSelectedReaderName();
    }

    @Override
    public void setSelectedTerminal(String name) {
        terminals.selectTerminal(name);
    }

    @Override
    public String getLastCardDescriptor() {
        return lastCardId;
    }

    @Override
    public synchronized int needsCardRefresh() {
         return terminals.checkTerminals();
    }

    @Override
    public void loadCard() throws LocalizedCardException, UnknownKeyException {
        synchronized(lock) {
            lastInstalledAIDs = null;
            selectedAID = null;
            try {
                if (terminals.getState() == Terminals.TerminalState.OK) {
                    CardDetails details = getCardDetails(terminals.getTerminal());
                    lastCardId = CardDetails.getId(details);
                    card = new CardInstanceImpl(details, terminals.getTerminal(), tryGeneric);
                    logger.info("Card successfully refreshed.");
                } else {
                    card = null;
                }

            } catch (UnknownKeyException | LocalizedCardException ex) {
                card = null;
                throw ex;
            } catch (Exception e) {
                card = null;
                throw new LocalizedCardException(e.getMessage(), "E_card_default", e);
            } finally {
                tryGeneric = false;
            }

            if (card != null && card.shouldJCAlgTestFinderRun()) getJCAlgTestDependencies();
        }
    }

    @Override
    public void loadCardUnauthorized() throws LocalizedCardException {
        synchronized(lock) {
            lastInstalledAIDs = null;
            selectedAID = null;
            try {
                if (terminals.getState() == Terminals.TerminalState.OK) {
                    CardDetails details = getCardDetails(terminals.getTerminal());
                    lastCardId = CardDetails.getId(details);
                    card = new CardInstanceUnauthorizedImpl(details, terminals.getTerminal());
                    logger.info("Card - unauthorized - loaded.");
                } else {
                    card = null;
                }
            } catch (Exception e) {
                card = null;
                throw new LocalizedCardException(e.getMessage(), "E_card_default", e);
            } finally {
                tryGeneric = false;
            }
        }
    }

    @Override
    public String[] getLastAppletInstalledAids() {
        return lastInstalledAIDs;
    }

    @Override
    public void setReloadCard() {
        this.terminals.setNeedsRefresh();
    }

    @Override
    public boolean getJCAlgTestDependencies() {
        if (card != null) {
            if (card.getCardMetadata().getJCData() == null) new Thread(new JCAlgTestResultsFinder(card)).start();
            else card.disableTemporarilyJCAlgTestFinder();
            return true;
        }
        return false;
    }

    @Override
    public boolean loadJCAlgTestDependencies(File from, boolean rewrite) throws LocalizedCardException {
        if (card == null) return false;
        if (card.getCardMetadata().getJCData() != null && !rewrite) return false;
        return JCAlgTestResultsFinder.parseFile(from);
    }

    @Override
    public void setTryGenericTestKey() {
        this.tryGeneric = true;
    }

    @Override
    public void setCallbackOnFailure(CallBack<Void> call) {
        this.notifier = call;
    }

    @Override
    public void install(File file, InstallOpts data) throws LocalizedCardException, IOException, UnknownKeyException {
        install(toCapFile(file), data);
    }

    @Override
    public void install(final CAPFile file, InstallOpts data) throws LocalizedCardException, UnknownKeyException {
        try {
            installImpl(file, data);
        } catch (CardException e) {
            loadCard();
            if (notifier != null) notifier.callBack();
            throw new LocalizedCardException(e.getMessage(), "unable_to_translate", e);
        } catch (LocalizedCardException e) {
            loadCard();
            if (notifier != null) notifier.callBack();
            throw e;
        }
    }

    @Override
    public void uninstall(AppletInfo nfo, boolean force) throws LocalizedCardException, UnknownKeyException {
        if (card == null) {
            throw new LocalizedCardException("No card recognized.", "no_card");
        }
        try {
            uninstallImpl(nfo, force);
        } catch (CardException e) {
            loadCard();
            if (notifier != null) notifier.callBack();
            throw new LocalizedCardException(e.getMessage(), "unable_to_translate", e);
        } catch (LocalizedCardException ex) {
            loadCard();
            if (notifier != null) notifier.callBack();
            throw ex;
        }
    }

    @Override
    public boolean select(String AID) throws LocalizedCardException {
        synchronized(lock) {
            if (card == null) {
                throw new LocalizedCardException("No card recognized.", "no_card");
            }

            try {
                GPCommand<Boolean> select = new Select(AID);
                card.executeCommands(select);
                return select.getResult();
            } catch (CardException e) {
                throw new LocalizedCardException(e.getMessage(), "unable_to_translate", e);
            }
        }
    }

    @Override
    public ResponseAPDU sendApdu(String AID, String APDU) throws LocalizedCardException {
        synchronized(lock) {
            if (card == null) {
                throw new LocalizedCardException("No card recognized.", "no_card");
            }

            try {
                GPCommand<ResponseAPDU> send = new Transmit(AID, APDU);
                card.executeCommands(send);
                return send.getResult();
            } catch (CardException e) {
                throw new LocalizedCardException(e.getMessage(), "unable_to_translate", e);
            }
        }
    }

    private CAPFile toCapFile(File f) throws IOException, LocalizedCardException {
        if (!f.exists()) throw new LocalizedCardException(textSrc.getString("E_install_no_file_1") +
                f.getCanonicalPath() + " " + textSrc.getString("E_install_no_file_2"));

        try (FileInputStream fin = new FileInputStream(f)) {
            return CAPFile.fromStream(fin);
        }
    }

    /**
     * Performs GET_DATA command
     * to get data from inserted card and create new CardInstance instance
     */
    private CardDetails getCardDetails(CardTerminal terminal) throws CardException, LocalizedCardException, IOException {
        logger.info("Manager: GET_DATA command");
        Card card;
        APDUBIBO channel;
        boolean exclusive = OptionsFactory.getOptions().is(Options.KEY_EXCLUSIVE_CARD_CONNECT);

        try {
            card = terminal.connect("*");
            if (exclusive) card.beginExclusive();
            channel = CardChannelBIBO.getBIBO(card.getBasicChannel());
            logger.info("Manager: GET_DATA channel created.");
        } catch (CardException e) {
            //if (card != null) card.endExclusive();
            throw new LocalizedCardException("Could not connect to selected reader: " +
                    TerminalManager.getExceptionMessage(e), "E_connect_fail");
        }

        GPCommand<CardDetails> command = new GetDetails(channel);
        command.setChannel(channel);
        command.execute();
        logger.info("Manager: GET_DATA data returned.");
        if (exclusive) card.endExclusive();
        card.disconnect(false);

        CardDetails details = command.getResult();
        details.setAtr(card.getATR());
        return details;
    }

    //applet deletion implementation
    private void uninstallImpl(AppletInfo nfo, boolean force) throws CardException, LocalizedCardException{
        synchronized(lock) {
            GPCommand<?> delete = new Delete(nfo, force);
            GPCommand<CardInstanceMetaData> contents = new ListContents(card.getId());
            card.secureExecuteCommands(delete, new GPCommand<Void>() {
                @Override
                public String getDescription() {
                    return "Delete applet metadata inside secure loop.";
                }

                @Override
                public boolean execute() throws LocalizedCardException {
                    card.deleteAppletData(nfo, force);
                    return true;
                }
            }, contents);
            card.setMetaData(contents.getResult());
            selectedAID = null;
        }
    }

    //applet installation implementation
    private void installImpl(final CAPFile file, InstallOpts data) throws CardException, LocalizedCardException {
        synchronized(lock) {
            if (card == null) {
                throw new LocalizedCardException("No card recognized.", "no_card");
            }

            if (!data.isForce() && OptionsFactory.getOptions().is(Options.KEY_SIMPLE_USE)) tryDeletePackageByAID(file);

            try (PrintStream print = new PrintStream(loggerStream)) {
                file.dump(print);

                GPCommand<?>[] commands = new GPCommand[data.getOriginalAIDs().length * 2 + 2 + 1]; //load, save data, n * install and save data, list
                commands[0] = new Load(file, data);
                commands[1] = new GPCommand<Void>() {
                    @Override
                    public String getDescription() {
                        return "Register package info for save.";
                    }

                    @Override
                    public boolean execute() throws GPException {
                        card.getCardMetadata().addAppletIgnoreModulesIfPkg(getPackageInfo(data.getInfo(), file));
                        try {
                            card.saveInfoData();
                        } catch (LocalizedCardException e) {
                            //todo
                        }
                        return false;
                    }
                };

                AID defaultSelected = data.getDefalutSelected() == null || data.getDefalutSelected().isEmpty() ?
                        null : AID.fromString(data.getDefalutSelected());
                int appletIdx = 0;
                int i = 2;
                while (i < data.getOriginalAIDs().length * 2 + 2) {
                    final int appidx = appletIdx;
                    Install command = new Install(file, data, appidx,
                            AID.fromString(data.getOriginalAIDs()[appidx]).equals(defaultSelected));
                    commands[i++] = command;
                    commands[i++] = new GPCommand<Void>() {
                        @Override
                        public String getDescription() {
                            return "Register applet info for save.";
                        }

                        @Override
                        public boolean execute() throws GPException {
                            AppletInfo installed = getAppletInfo(data.getInfo(), command.getResult(),
                                    data.getAppletNames() != null ? data.getAppletNames()[appidx] : "");
                            card.getCardMetadata().addAppletIgnoreModulesIfPkg(installed);
                            try {
                                card.saveInfoData();
                            } catch (LocalizedCardException e) {
                                //todo
                            }
                            return false;
                        }
                    };
                    appletIdx++;
                }
                ListContents cmd = new ListContents(card.getId());
                commands[commands.length - 1] = cmd;

                card.secureExecuteCommands(commands);
                lastInstalledAIDs = data.getAppletAIDsAsInstalled();
                card.setMetaData(cmd.getResult());
            } finally {
                selectedAID = null;
            }
        }
    }

    //deleting without synchronization, card re-listing and other stuff - use with caution
    private void tryDeletePackageByAID(final CAPFile file) throws CardException, LocalizedCardException {
        if (!card.getCardMetadata().isPackagePresent(file.getPackageAID())) return;
        logger.info("Package present - try to uninstall in simple mode.");
        try {
            AppletInfo nfo = new AppletInfo("", "", "", "", "", file.getPackageAID().toString());
            card.secureExecuteCommands(
                    new Delete(nfo, false),
                    new GPCommand<Void>() {
                        @Override
                        public String getDescription() {
                            return "Delete applet metadata inside secure loop.";
                        }

                        @Override
                        public boolean execute() throws LocalizedCardException {
                            card.deletePackageData(nfo);
                            return true;
                        }
                    }
            );
        } catch (Exception e) {
            logger.warn("Failed to remove package (ALREADY PRESENT) before installation.");
        }
    }

    private AppletInfo getPackageInfo(AppletInfo of, CAPFile file) {
       return new AppletInfo(of.getName(), null, of.getVersion(), of.getAuthor(),
                of.getSdk(), file.getPackageAID().toString(), KeysPresence.NO_KEYS, GPRegistryEntry.Kind.ExecutableLoadFile);
    }

    private AppletInfo getAppletInfo(AppletInfo from, AID realInstalledAID, String appletName) {
        AppletInfo clone = null;
        try {
            clone = (AppletInfo)from.clone();
            clone.setAID(realInstalledAID.toString());
            if (!appletName.isEmpty()) clone.setAppletInstanceName(appletName);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            logger.warn("Unable to save applet info data: " + realInstalledAID, e);
        }
        return clone;
    }
}
