package cz.muni.crocs.appletstore.card;

import apdu4j.APDUBIBO;
import apdu4j.CardChannelBIBO;
import apdu4j.TerminalManager;
import apdu4j.ResponseAPDU;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.Store;
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
import java.util.concurrent.*;

/**
 * Manager providing all functionality over card
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardManagerImpl implements CardManager {

    private static final Logger logger = LoggerFactory.getLogger(CardManagerImpl.class);
    private static final LogOutputStream loggerStream = new LogOutputStream(logger, Level.INFO);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private Terminals terminals = new Terminals();
    private CardInstanceImpl card;
    private String lastCardId = textSrc.getString("no_last_card");
    private AID selectedAID = null;
    private String[] lastInstalledAIDs = null;
    private boolean tryGeneric = false;

    private ArrayList<AppletInfo> toSave = new ArrayList<>();
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
            logger.info("Card successfully refreshed.");

            getJCAlgTestDependencies();
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
        if (card != null && card.getCardMetadata().getJCData() == null) {
            new Thread(new JCAlgTestResultsFinder(card)).start();
            return true;
        }
        return card != null;
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

            GPCommand<Boolean> send = new Select(AID);
            try {
                card.executeCommands(send);
            } catch (CardException e) {
                throw new LocalizedCardException(e.getMessage(), "unable_to_translate", e);
            }
            return send.getResult();
        }
    }

    @Override
    public ResponseAPDU sendApdu(String AID, String APDU) throws LocalizedCardException {
        synchronized(lock) {
            if (card == null) {
                throw new LocalizedCardException("No card recognized.", "no_card");
            }

            GPCommand<ResponseAPDU> send = new Transmit(AID, APDU);
            try {
                card.executeCommands(send);
            } catch (CardException e) {
                throw new LocalizedCardException(e.getMessage(), "unable_to_translate", e);
            }
            return send.getResult();
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
                    card.deleteData(nfo, force);
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

            toSave.clear();
            try (PrintStream print = new PrintStream(loggerStream)) {
                file.dump(print);

                GPCommand<?>[] commands = new GPCommand[data.getOriginalAIDs().length * 2 + 2]; //load, save data, n * install and save data
                commands[0] = new Load(file, data);
                commands[1] = new GPCommand<Void>() {
                    @Override
                    public String getDescription() {
                        return "Register package info for save.";
                    }

                    @Override
                    public boolean execute() throws GPException {
                        toSave.add(getPackageInfo(data.getInfo(), file));
                        return true;
                    }
                };

                AID defaultSelected = data.getDefalutSelected() == null || data.getDefalutSelected().isEmpty() ?
                        null : AID.fromString(data.getDefalutSelected());
                int appletIdx = 0;
                int i = 2;
                for (; i < data.getOriginalAIDs().length * 2 + 2; ) {
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
                            toSave.add(installed);
                            return false;
                        }
                    };
                    appletIdx++;
                }
                card.secureExecuteCommands(commands);
                lastInstalledAIDs = data.getAppletAIDsAsInstalled();
            } finally {
                try {
                    for (AppletInfo info : toSave) {
                        card.getCardMetadata().addApplet(info);
                    }
                    card.saveInfoData();
                    ListContents cmd = new ListContents(card.getId());
                    card.secureExecuteCommands(cmd);
                    card.setMetaData(cmd.getResult());
                } finally {
                    selectedAID = null;
                }
            }
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
