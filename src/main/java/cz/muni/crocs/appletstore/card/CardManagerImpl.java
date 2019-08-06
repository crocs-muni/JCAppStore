package cz.muni.crocs.appletstore.card;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.command.*;
import cz.muni.crocs.appletstore.util.LogOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import pro.javacard.AID;
import pro.javacard.CAPFile;
import pro.javacard.gp.GPRegistryEntry;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/**
 * Manager providing all functionality over card
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardManagerImpl implements CardManager {

    private static final Logger logger = LoggerFactory.getLogger(CardManagerImpl.class);
    private static final LogOutputStream loggerStream = new LogOutputStream(logger, Level.INFO);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private Terminals terminals = new Terminals();

    private CardInstance card;
    private String lastCardId = textSrc.getString("no_last_card");
    private AID selectedAID = null;
    private volatile boolean busy = false;

    @Override
    public void select(AID aid) {
        if (aid == selectedAID) {
            selectedAID = null;
            aid = null;
        }

        if (card.getApplets() == null)
            return;

        for (AppletInfo info : card.getApplets()) {
            info.setSelected(info.getAid() == aid);
        }
        this.selectedAID = aid;
    }

    @Override
    public boolean isSelected() {
        return selectedAID != null;
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
    public List<AppletInfo> getInstalledApplets() {
        return card == null ? null : Collections.unmodifiableList(card.getApplets());
    }

    @Override
    public String getCardId() {
        return card == null ? "" : card.getId();
    }

    @Override
    public String getCardDescriptor() {
        return card == null ? "" : card.getName() + "  " + card.getId();
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
                logger.info("The card was busy when needsCardRefresh() called, waiting interrupted.");
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
    public synchronized void refreshCard() throws LocalizedCardException {
        while (busy) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.info("The card was busy when refreshCard() called, waiting interrupted.");
                Thread.currentThread().interrupt();
            }
        }
        busy = true;

        try {
            if (terminals.getState() == Terminals.TerminalState.OK) {
                CardDetails details = getCardDetails(terminals.getTerminal());
                lastCardId = CardDetails.getId(details);
                card = new CardInstance(details, terminals.getTerminal());
            } else {
                card = null;
            }
        } catch (LocalizedCardException ex) {
            card = null;
            throw ex;
        } catch (Exception e) {
            card = null;
            throw new LocalizedCardException(e.getMessage(), "unable_to_translate", e);
        } finally {
            busy = false;
            notifyAll();
        }
    }

    @Override
    public Integer getCardLifeCycle() {
        if (card == null)
            return 0;
        java.util.List<AppletInfo> infoList = card.getApplets();
        if (infoList == null)
            return 0;

        for (AppletInfo info : infoList) {
            if (info.getKind() == GPRegistryEntry.Kind.IssuerSecurityDomain) {
                return info.getLifecycle();
            }
        }
        throw new Error("Should not end here.");
    }

    @Override
    public void install(File file, String[] data) throws LocalizedCardException, IOException {
        if (!file.exists()) throw new LocalizedCardException(textSrc.getString("E_install_no_file_1") +
                file.getAbsolutePath() + textSrc.getString("E_install_no_file_2"));

        CAPFile capFile;
        try (FileInputStream fin = new FileInputStream(file)) {
            capFile = CAPFile.fromStream(fin);
        }

        try {
            installImpl(capFile, data);
        } catch (CardException e) {
            e.printStackTrace();
            throw new LocalizedCardException(e.getMessage(), "unable_to_translate", e);
        }
        refreshCard();
    }

    @Override
    public synchronized void install(final CAPFile file, String[] data) throws LocalizedCardException {
        try {
            installImpl(file, data);
        } catch (CardException e) {
            e.printStackTrace();
            throw new LocalizedCardException(e.getMessage(), "unable_to_translate", e);
        } finally {
            refreshCard();
        }
    }

    @Override
    public synchronized void install(final CAPFile file, String[] data, AppletInfo info) throws LocalizedCardException {
        try {
            String aid = installImpl(file, data);
            info.setAID(aid);
        } catch (CardException e) {
            e.printStackTrace();
            throw new LocalizedCardException(e.getMessage(), "unable_to_translate", e);
        }

        java.util.List<AppletInfo> appletInfoList = card.getApplets();
        appletInfoList.add(info);
        AppletSerializer<java.util.List<AppletInfo>> toSave = new AppletSerializerImpl();
        try {
            toSave.serialize(appletInfoList, new File(Config.APP_DATA_DIR + Config.SEP + card.getId()));
        } finally {
            refreshCard();
        }
    }

    @Override
    public synchronized void uninstall(AppletInfo nfo, boolean force) throws LocalizedCardException {
        if (card == null) {
            throw new LocalizedCardException("No card recognized.", "no_card");
        }

        while (busy) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.info("The card was busy when uninstall() called, waiting interrupted.");
                Thread.currentThread().interrupt();
            }
        }
        busy = true;

        try {
            GPCommand<Void> delete = new Delete(nfo, force);
            card.executeCommand(delete);
            card.removeAppletInfo(nfo);

        } catch (CardException e) {
            throw new LocalizedCardException(e.getMessage(), "unable_to_translate", e);
        } finally {
            busy = false;
            notifyAll();
            refreshCard();
        }
    }

    @Override
    public synchronized void sendApdu(String AID) throws LocalizedCardException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Performs the only card insecure-channel use (e.g. GET)
     * to get data from inserted card
     */
    private CardDetails getCardDetails(CardTerminal terminal) throws CardException {
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

    private synchronized String installImpl(final CAPFile file, String[] data) throws CardException, LocalizedCardException {
        if (card == null) {
            throw new LocalizedCardException("No card recognized.", "no_card");
        }

        while (busy) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.info("The card was busy when install() called, waiting interrupted.");
                Thread.currentThread().interrupt();
            }
        }
        busy = true;

        try (PrintStream print = new PrintStream(loggerStream)) {
            file.dump(print);
            Install install = new Install(file, data);
            card.executeCommand(install);
            return install.getAppletAID().toString();
        } finally {
            busy = false;
            notifyAll();
        }
    }
}
