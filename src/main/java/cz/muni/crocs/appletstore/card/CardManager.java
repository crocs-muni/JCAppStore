package cz.muni.crocs.appletstore.card;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.LocalWindowPane;
import cz.muni.crocs.appletstore.card.command.GPCommand;
import cz.muni.crocs.appletstore.card.command.Install;
import cz.muni.crocs.appletstore.util.AppletInfo;
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
    private CardInstance card = new CardInstance();

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

    /**
     * Look into terminals for a card. If state changed, e.g. terminals / cards switched,
     * makes necessarry steps to be ready to work with
     * @return @link Terminals::checkTerminals()
     */
    public int refresh(LocalWindowPane parent) {
        int result = terminals.checkTerminals();
        if (result != 2) return result;

        if (parent != null) {
            parent.updatePanes(Terminals.TerminalState.LOADING);
        }
        if (terminals.getState() == Terminals.TerminalState.OK) {
            try {
                card.update(CardInstance.getCardInfo(terminals.getTerminal()), terminals.getTerminal(), false);
            } catch (CardException e) {
                card.error = e.getMessage();
                e.printStackTrace();
                //todo 80100068 error - card ejected ignore this error
            }
        } else {
            card.update(null, null, false);
        }
        //todo update
        return 2;
    }

    public Integer getCardLifeCycle() {
        for (AppletInfo info : card.getApplets()) {
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
        //todo dump into log:   instcap.dump( ... logger or other stream ... )

        GPCommand<Void> install = new Install(file, data);
        card.executeCommand(install);

        //todo save applet data into ini

        // todo search for failures during install and notify user

        // todo INMPORTANT note whether applet stores keys - uninstalling will destroy them
        if (card.getState() == CardInstance.CardState.OK)
            terminals.setState(Terminals.TerminalState.NO_CARD); //card has been modyfied - reload
    }

    public void uninstall(File file) {

    }

    public void uninstall(String AID) {

    }

    public void sendApdu(String AID) {

    }
}
