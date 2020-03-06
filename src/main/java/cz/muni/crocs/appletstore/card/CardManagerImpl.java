package cz.muni.crocs.appletstore.card;

import apdu4j.APDUBIBO;
import apdu4j.CardChannelBIBO;
import apdu4j.TerminalManager;
import apdu4j.ResponseAPDU;

import cz.muni.crocs.appletstore.CardInfoPanel;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.command.*;
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
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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

    private volatile Terminals terminals = new Terminals();
    private volatile CardInstanceImpl card;
    private volatile String lastCardId = textSrc.getString("no_last_card");
    private volatile AID selectedAID = null;
    private volatile String[] lastInstalledAIDs = null;
    private volatile boolean tryGeneric = false;
    private volatile boolean busy = false;

    private volatile ArrayList<AppletInfo> toSave = new ArrayList<>();

    private volatile CallBack<Void> notifier;

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

        if (card.getApplets() == null)
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
        while (busy) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.warn("The card was busy when needsCardRefresh() called, waiting interrupted.", e);
                Thread.currentThread().interrupt();
            }
        }
        busy = true;

        try {
            return terminals.checkTerminals();
        } finally {
            busy = false;
            notifyAll();
        }
    }

    @Override
    public synchronized void loadCard() throws LocalizedCardException, UnknownKeyException {
        while (busy) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.warn("The card was busy when loadCard() called, waiting interrupted.", e);
                Thread.currentThread().interrupt();
            }
        }
        busy = true;
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
            busy = false;
            notifyAll();
        }
        logger.info("Card successfully refreshed.");
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
    public void setTryGenericTestKey() {
        this.tryGeneric = true;
    }

    @Override
    public void setCallbackOnFailure(CallBack<Void> call) {
        this.notifier = call;
    }

    @Override
    public synchronized void install(File file, InstallOpts data) throws LocalizedCardException, IOException, UnknownKeyException {
        install(toCapFile(file), data);
    }

    @Override
    public synchronized void install(final CAPFile file, InstallOpts data) throws LocalizedCardException, UnknownKeyException {
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
    public synchronized void uninstall(AppletInfo nfo, boolean force) throws LocalizedCardException, UnknownKeyException {
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
    public synchronized ResponseAPDU sendApdu(String AID, String APDU) throws LocalizedCardException {
        if (card == null) {
            throw new LocalizedCardException("No card recognized.", "no_card");
        }

        while (busy) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.warn("The card was busy when sendApdu() called, waiting interrupted.", e);
                Thread.currentThread().interrupt();
            }
        }
        busy = true;

        GPCommand<ResponseAPDU> send = new Transmit(AID, APDU);
        try {
            card.executeCommands(send);
        } catch (CardException e) {
            throw new LocalizedCardException(e.getMessage(), "unable_to_translate", e);
        } finally {
            busy = false;
            notifyAll();
        }
        return send.getResult();
    }

    private CAPFile toCapFile(File f) throws IOException, LocalizedCardException {
        if (!f.exists()) throw new LocalizedCardException(textSrc.getString("E_install_no_file_1") +
                f.getCanonicalPath() + " " + textSrc.getString("E_install_no_file_2"));

        try (FileInputStream fin = new FileInputStream(f)) {
            return CAPFile.fromStream(fin);
        }
    }

    private void deleteData(final AppletInfo applet, boolean force) throws LocalizedCardException {
        Set<AppletInfo> appletInfoList = card.getApplets();
        deleteInfo(appletInfoList, applet.getAid());
        if (force && applet.getKind().equals(GPRegistryEntry.Kind.ExecutableLoadFile)) {
            for (AID aid : applet.getModules()) {
                deleteInfo(appletInfoList, aid);
            }
        }
        AppletSerializer<Set<AppletInfo>> toSave = new AppletSerializerImpl();
        toSave.serialize(appletInfoList, new File(Config.APP_DATA_DIR + Config.S + card.getId()));
    }

    private void deleteInfo(Set<AppletInfo> list, AID toDelete) {
        Iterator<AppletInfo> info = list.iterator();
        while(info.hasNext()) {
            AppletInfo nfo = info.next();
            if (toDelete.equals(nfo.getAid())) {
                info.remove();
                return;
            }
        }
    }

    /**
     * Performs card insecure-channel use (e.g. GET)
     * to get data from inserted card
     */
    private CardDetails getCardDetails(CardTerminal terminal) throws CardException, LocalizedCardException, IOException {
        //todo timeout
        Card card = null;
        APDUBIBO channel = null;
        boolean exclusive = OptionsFactory.getOptions().is(Options.KEY_EXCLUSIVE_CARD_CONNECT);

        try {
            card = terminal.connect("*");
            if (exclusive) card.beginExclusive();
            channel = CardChannelBIBO.getBIBO(card.getBasicChannel());
        } catch (CardException e) {
            //if (card != null) card.endExclusive();
            throw new LocalizedCardException("Could not connect to selected reader: " +
                    TerminalManager.getExceptionMessage(e), "E_connect_fail");
        }

        GPCommand<CardDetails> command = new GetDetails(channel);
        command.setChannel(channel);
        command.execute();
        if (exclusive) card.endExclusive();
        card.disconnect(false);

        CardDetails details = command.getResult();
        details.setAtr(card.getATR());
        return details;
    }

    private synchronized void uninstallImpl(AppletInfo nfo, boolean force) throws CardException, LocalizedCardException{
        while (busy) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.warn("The card was busy when uninstall() called, waiting interrupted.", e);
                Thread.currentThread().interrupt();
            }
        }
        busy = true;

        try {
            GPCommand delete = new Delete(nfo, force);
            GPCommand<Set<AppletInfo>> contents = new ListContents(card.getId());
            card.secureExecuteCommands(delete, new GPCommand() {
                @Override
                public String getDescription() {
                    return "Delete applet metadata inside secure loop.";
                }

                @Override
                public boolean execute() throws LocalizedCardException {
                    deleteData(nfo, force);
                    return true;
                }
            }, contents);
            card.setApplets(contents.getResult());
            selectedAID = null;
        } finally {
            busy = false;
            notifyAll();
        }
    }

//    private void uninstallAppletsBeforeInstallation(InstallOpts data) throws CardException, LocalizedCardException {
//        if (data.getAppletsForDeletion() == null) return;
//        GPCommand[] commands = new GPCommand[data.getAppletsForDeletion().size() * 2];
//        //delete first collision applets
//        int i = 0;
//        for (AppletInfo nfo : data.getAppletsForDeletion()) {
//            if (nfo.getKind() == GPRegistryEntry.Kind.Application) {
//                commands[i++] = new Delete(nfo, true);
//                commands[i++] = new GPCommand() {
//                    @Override
//                    public String getDescription() {
//                        return "Deletion for installation.";
//                    }
//
//                    @Override
//                    public boolean execute() throws LocalizedCardException {
//                        deleteData(nfo, true);
//                        return true;
//                    }
//                };
//            }
//        }
//
//        //delete packages
//        for (AppletInfo nfo : data.getAppletsForDeletion()) {
//            if (nfo.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile) {
//                commands[i++] = new Delete(nfo, true);
//                commands[i++] = new GPCommand() {
//                    @Override
//                    public String getDescription() {
//                        return "Deletion for installation.";
//                    }
//
//                    @Override
//                    public boolean execute() throws LocalizedCardException {
//                        deleteData(nfo, true);
//                        return true;
//                    }
//                };
//            }
//        }
//        card.secureExecuteCommands(commands);
//    }

    private synchronized void installImpl(final CAPFile file, InstallOpts data) throws CardException, LocalizedCardException {
        if (card == null) {
            throw new LocalizedCardException("No card recognized.", "no_card");
        }

        while (busy) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.warn("The card was busy when install() called, waiting interrupted.", e);
                Thread.currentThread().interrupt();
            }
        }
        busy = true;

        toSave.clear();
        try (PrintStream print = new PrintStream(loggerStream)) {
            file.dump(print);

//            uninstallAppletsBeforeInstallation(data);

            GPCommand[] commands = new GPCommand[data.getOriginalAIDs().length * 2 + 2]; //load, save data, n * install and save data
            commands[0] = new Load(file, data);
            commands[1] = new GPCommand() {
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
            for ( ; i < data.getOriginalAIDs().length * 2 + 2; ) {
                final int appidx = appletIdx;
                Install command = new Install(file, data, appidx,
                        AID.fromString(data.getOriginalAIDs()[appidx]).equals(defaultSelected));
                commands[i++] = command;
                commands[i++] = new GPCommand() {
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
                saveInfoData();
                ListContents cmd = new ListContents(card.getId());
                card.secureExecuteCommands(cmd);
                card.setApplets(cmd.getResult());
            } finally {
                selectedAID = null;
                busy = false;
                notifyAll();
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

    private void saveInfoData() throws LocalizedCardException {
        Set<AppletInfo> appletInfoList = getAppletsToSave(card.getApplets());
        for (AppletInfo info : toSave) {
            insertOrRewrite(info, appletInfoList);
        }

        AppletSerializer<Set<AppletInfo>> serializer = new AppletSerializerImpl();
        serializer.serialize(appletInfoList, new File(Config.APP_DATA_DIR + Config.S + card.getId()));
    }

    private void insertOrRewrite(AppletInfo item, Set<AppletInfo> to) {
        if(!to.add(item)) {
            to.remove(item);
            to.add(item);
        }
    }

    private Set<AppletInfo> getAppletsToSave(Set<AppletInfo> all) {
        return all.stream().filter(a -> a.getAuthor() != null ||
                a.getVersion() != null ||
                a.getSdk() != null ||
                a.getName() != null)
                .collect(Collectors.toSet());
    }
}
