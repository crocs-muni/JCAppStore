package cz.muni.crocs.appletstore.card;

import cz.muni.crocs.appletstore.card.command.Delete;
import cz.muni.crocs.appletstore.card.command.GPCommand;
import cz.muni.crocs.appletstore.card.command.Install;
import cz.muni.crocs.appletstore.iface.CardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.CAPFile;
import pro.javacard.gp.GPRegistryEntry;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CardManagerImpl implements CardManager {

    private static final Logger logger = LoggerFactory.getLogger(CardManagerImpl.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private Terminals terminals = new Terminals();
    //our card representation
    private /*volatile*/ CardInstance card = new CardInstance();
    private AID selectedAID = null;
    private boolean busy = false;

    @Override
    public CardInstance.CardState getCardState() {
        return card.getState();
    }

    @Override
    public void select(AID aid) {
        if (aid == selectedAID) {
            selectedAID = null;
            aid = null;
        }
        for (AppletInfo info : card.getApplets()) {
            info.setSelected(info.getAid() == aid);
        }
        this.selectedAID = aid;
    }

    @Override
    public boolean isSelected() {
        return selectedAID != null;
    }

    /**
     * Get state of the terminal instance
     * @return Terminals.TerminalState value (NO_CARD / NO_READER / OK)
     */
    @Override
    public Terminals.TerminalState getTerminalState() {
        return terminals.getState();
    }
    /**
     * Return set of connected terminal names
     * @return
     */
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
    public CardInstance getCard() {
        return card;
    }

    @Override
    public String getErrorCauseTitle() {
        return card.getErrorTitle();
    }

    @Override
    public String getErrorCause() {
        return SW.getErrorCause(card.getErrorByte(),
                card.getErrorBody() == null ? textSrc.getString("E_communication") : card.getErrorBody());
    }

    @Override
    public synchronized int needsCardRefresh() {
        while (busy || card.getState() == CardInstance.CardState.WORKING) {
            try {
                wait();
            } catch (InterruptedException e)  {
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
    /**
     * Look into terminals for a card. If state changed, e.g. terminals / cards switched,
     * makes necessarry steps to be ready to work with
     * @return @link Terminals::checkTerminals()
     */
    @Override
    public synchronized void refreshCard() {
        while (busy || card.getState() == CardInstance.CardState.WORKING) {
            try {
                wait();
            } catch (InterruptedException e)  {
                logger.info("The card was busy when refreshCard() called, waiting interrupted.");
                Thread.currentThread().interrupt();
            }
        }
        busy = true;

        try {
            if (terminals.getState() == Terminals.TerminalState.OK) {
                card.update(card.getCardInfo(terminals.getTerminal()), terminals.getTerminal(), false);
            } else {
                card.update(null, null, false);
            }
            //todo update
        } finally {
            busy = false;
            notifyAll();
        }
    }

    @Override
    public Integer getCardLifeCycle() {
        java.util.List<AppletInfo> infoList = card.getApplets();
        if (infoList == null || card.getState() == CardInstance.CardState.WORKING)
            return 0;

        for (AppletInfo info : infoList) {
            if (info.getKind() == GPRegistryEntry.Kind.IssuerSecurityDomain) {
                return info.getLifecycle();
            }
        }
        return null;
    }

    @Override
    public void install(File file, String[] data) throws CardException, IOException {
        if (!file.exists()) throw new CardException(textSrc.getString("E_install_no_file_1") +
                file.getAbsolutePath() + textSrc.getString("E_install_no_file_2"));

        CAPFile capFile;
        try (FileInputStream fin = new FileInputStream(file)) {
            capFile = CAPFile.fromStream(fin);
        }
        install(capFile, data);
    }

    @Override
    public synchronized void install(final CAPFile file, String[] data) throws CardException {
        while (busy || card.getState() == CardInstance.CardState.WORKING) {
            try {
                wait();
            } catch (InterruptedException e)  {
                logger.info("The card was busy when install() called, waiting interrupted.");
                Thread.currentThread().interrupt();
            }
        }
        busy = true;

        try {
            if (card.getState() == CardInstance.CardState.WORKING) return;
            file.dump(System.out);
            //todo dump into log:   instcap.dump( ... logger or other stream ... )

            //new Thread(() -> {
            GPCommand<Void> install = new Install(file, data);
            card.executeCommand(install);

            card.setRefresh();
            if (card.getState() == CardInstance.CardState.OK)
                terminals.setState(Terminals.TerminalState.NO_CARD);

            //todo save applet data into ini

            // todo search for failures during install and notify user

            // todo IMPORTANT note whether applet stores keys - uninstalling will destroy them


//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
            //}).start();
        } finally {
            busy = false;
            notifyAll();
        }
    }

    @Override
    public synchronized void uninstall(AppletInfo nfo, boolean force) throws CardException {
        while (busy || card.getState() == CardInstance.CardState.WORKING) {
            try {
                wait();
            } catch (InterruptedException e)  {
                logger.info("The card was busy when uninstall() called, waiting interrupted.");
                Thread.currentThread().interrupt();
            }
        }
        busy = true;

        try {
            GPCommand<Void> delete = new Delete(nfo, force);
            card.executeCommand(delete);
            //todo remove applet data from ini
            // todo search for failures during install and notify user
            card.setRefresh();
            if (card.getState() == CardInstance.CardState.OK)
                terminals.setState(Terminals.TerminalState.NO_CARD);
        } finally {
            busy = false;
            notifyAll();
        }
    }

    @Override
    public synchronized void sendApdu(String AID) {

    }
}
