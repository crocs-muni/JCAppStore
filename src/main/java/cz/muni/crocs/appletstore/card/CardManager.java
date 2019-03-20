package cz.muni.crocs.appletstore.card;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.command.Delete;
import cz.muni.crocs.appletstore.card.command.GPCommand;
import cz.muni.crocs.appletstore.card.command.Install;
import cz.muni.crocs.appletstore.util.AppletInfo;
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
import java.util.Set;
/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CardManager {

    private static final Logger logger = LoggerFactory.getLogger(CardManager.class);

    private CardManager() {}
    private static CardManager instance;

    public static CardManager getInstance() {
        if (instance == null) instance = new CardManager();
        return instance;
    }

    public CardInstance.CardState getCardState() {
        return card.getState();
    }

    private Terminals terminals = new Terminals();
    //our card representation
    private /*volatile*/ CardInstance card = new CardInstance();

    private AID selectedAID = null;

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

    public boolean isSelected() {
        return selectedAID != null;
    }

    /**
     * Get state of the terminal instance
     * @return Terminals.TerminalState value (NO_CARD / NO_READER / OK)
     */
    public Terminals.TerminalState getTerminalState() {
        return terminals.getState();
    }
    /**
     * Return set of connected terminal names
     * @return
     */
    public Set<String> getTerminals() {
        return terminals.getTerminals().keySet();
    }
    public CardTerminal getSelectedTerminal() {
        return terminals.getTerminal();
    }
    public String getSelectedTerminalName() {
        return terminals.getSelectedReaderName();
    }

    public void setSelectedTerminal(String name) {
        terminals.selectTerminal(name);
    }

    public CardInstance getCard() {
        return card;
    }

    public String getErrorCauseTitle() {
        return Config.translation.get(card.getErrorTitleId());
    }

    public String getErrorCause() {
        return SW.getErrorCause(card.getErrorByte(),
                card.getErrorBody() == null ? Config.translation.get(182) : card.getErrorBody());
    }

    public int needsCardRefresh() {
        if (card.getState() == CardInstance.CardState.WORKING)
            return 0;
        return terminals.checkTerminals();
    }
    /**
     * Look into terminals for a card. If state changed, e.g. terminals / cards switched,
     * makes necessarry steps to be ready to work with
     * @return @link Terminals::checkTerminals()
     */
    public void refreshCard() {
        if (card.getState() == CardInstance.CardState.WORKING)
            return;

        if (terminals.getState() == Terminals.TerminalState.OK) {
            card.update(card.getCardInfo(terminals.getTerminal()), terminals.getTerminal(), false);
        } else {
            card.update(null, null, false);
        }
        //todo update
    }

    public Integer getCardLifeCycle() {
        java.util.List<AppletInfo> infoList = card.getApplets();
        if (infoList == null)
            return 0;

        for (AppletInfo info : infoList) {
            if (info.getKind() == GPRegistryEntry.Kind.IssuerSecurityDomain) {
                return info.getLifecycle();
            }
        }
        return null;
    }

    public void install(File file, String[] data) throws CardException, IOException {
        if (!file.exists())
            throw new CardException(
                    Config.translation.get(150) + file.getAbsolutePath() + Config.translation.get(151));

        CAPFile capFile;
        try (FileInputStream fin = new FileInputStream(file)) {
            capFile = CAPFile.fromStream(fin);
        }
        install(capFile, data);
    }

    public void install(final CAPFile file, String[] data) throws CardException {

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

            // todo INMPORTANT note whether applet stores keys - uninstalling will destroy them


//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        //}).start();

    }

    public void uninstall(AppletInfo nfo, boolean force) throws CardException {
        if (card.getState() == CardInstance.CardState.WORKING) return;

        GPCommand<Void> delete = new Delete(nfo, force);
        card.executeCommand(delete);
        //todo remove applet data into ini
        // todo search for failures during install and notify user
        card.setRefresh();
        if (card.getState() == CardInstance.CardState.OK)
            terminals.setState(Terminals.TerminalState.NO_CARD);
    }


    public void sendApdu(String AID) {

    }
}
