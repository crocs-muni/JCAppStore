package cz.muni.crocs.appletstore.card;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.util.AppletInfo;
import pro.javacard.AID;


import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import java.io.File;
import java.util.Set;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CardManager {

    boolean ready = false;
    private Terminals terminals = new Terminals();
    //our card representation
    private CardInstance card = new CardInstance();

    private AID selectedAID = null;

    public void select(AID aid) {
        for (AppletInfo info : card.getApplets()) {
            info.setSelected(info.getAid() == aid);
        }
        this.selectedAID = aid;
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
     * @return true if any state changed
     */
    public boolean refresh() {
        if (! terminals.checkTerminals()) {
            ready = true;
            return false;
        }
        System.out.println("state changed");
        //changes occurred, now make necessary authentication
        //todo on different thread, update screen after loading done
        if (terminals.getState() == Terminals.TerminalState.OK) {
            try {
                card.update(CardInstance.getCardInfo(terminals.getTerminal()), terminals.getTerminal());
            } catch (CardException e) {
                //todo handle
                e.printStackTrace();
            }
        } else {
            card.update(null, null);
        }

        //todo update
        return true;
    }











    public void install(String filePath) throws CardException {
        install(new File(filePath));
    }

    public void install(File file) throws CardException {
        if (!file.exists()) {
            throw new CardException(
                    Config.translation.get(150) + file.getAbsolutePath() + Config.translation.get(151));
        }
    }

    public void uninstall(File file) {

    }

    public void uninstall(String AID) {

    }




}
