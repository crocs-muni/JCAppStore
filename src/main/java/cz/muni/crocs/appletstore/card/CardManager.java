package cz.muni.crocs.appletstore.card;

import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.card.CardInstance;
import cz.muni.crocs.appletstore.card.Terminals;
import pro.javacard.AID;
import pro.javacard.CAPFile;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface CardManager {

    CardInstance.CardState getCardState();

    void select(AID aid);

    boolean isSelected();

    /**
     * Get state of the terminal instance
     * @return Terminals.TerminalState value (NO_CARD / NO_READER / OK)
     */
    Terminals.TerminalState getTerminalState();
    /**
     * Return set of connected terminal names
     * @return
     */
    Set<String> getTerminals();
    CardTerminal getSelectedTerminal();
    String getSelectedTerminalName();

    void setSelectedTerminal(String name);

    CardInstance getCard();

    String getErrorCauseTitle();

    String getErrorCause();

    int needsCardRefresh();
    /**
     * Look into terminals for a card. If state changed, e.g. terminals / cards switched,
     * makes necessarry steps to be ready to work with
     * @return @link Terminals::checkTerminals()
     */
    void refreshCard();

    Integer getCardLifeCycle();

    void install(File file, String[] data) throws CardException, IOException;

    void install(final CAPFile file, String[] data) throws CardException;

    void uninstall(AppletInfo nfo, boolean force) throws CardException;

    void sendApdu(String AID) throws CardException;
}
