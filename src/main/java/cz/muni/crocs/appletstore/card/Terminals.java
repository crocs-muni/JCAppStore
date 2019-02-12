package cz.muni.crocs.appletstore.card;

import apdu4j.TerminalManager;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Terminals {

    public enum TerminalState {
        NO_READER, NO_CARD, OK
    }

    private TreeMap<String, CardTerminal> cardReaderMap = new TreeMap<>();
    private String selectedReader = null;
    private String toSelectReader = null;

    private TerminalState state = TerminalState.NO_READER;

    public TerminalState getState() {
        return state;
    }

    private boolean checkCardInTerminal(CardTerminal terminal) throws CardException {
        return terminal.isCardPresent();
    }

    public String getSelectedReaderName() {
        return selectedReader;
    }

    /**
     * Look into terminal list to get the card
     *
     * @param terminal terminal to look into
     */
    public void checkCardPresence(CardTerminal terminal) {
        if (terminal == null) {
            state = TerminalState.NO_READER;
            return;
        }
        try {
            state = (checkCardInTerminal(terminal)) ? TerminalState.OK : TerminalState.NO_CARD;
        } catch (CardException e) {
            //TODO report
            state = TerminalState.NO_CARD;
        }
    }

    /**
     * Search all terminals for a presence of any card
     *
     * @return terminal of a card inserted, null otherwise
     */
    public CardTerminal checkCardPresence() {
        try {
            for (CardTerminal term : cardReaderMap.values()) {
                if (checkCardInTerminal(term)) {
                    return term;
                }
            }
        } catch (CardException e) {
            //TODO report
            return null;
        }
        return null;
    }

    /**
     * Check for card presence in a specific terminal name
     *
     * @param terminal name of terminal to llok into
     * @return true if card found, false otherwise
     */
    public boolean checkCardPresence(String terminal) {
        checkCardPresence(cardReaderMap.get(terminal));
        return state == TerminalState.OK;
    }

    /**
     * Check for terminal and card presence
     * readers are assumed to have only one card at time
     * @return true if state changed (e.g. different reader / card) as default selected
     */
    public boolean checkTerminals() {
        cardReaderMap.clear();

        TerminalState old = state;
        boolean needToRefresh;

        try {
            final TerminalFactory tf;
            tf = TerminalManager.getTerminalFactory(null);
            CardTerminals terminals = tf.terminals();

            int number = 0;
            for (CardTerminal term : terminals.list()) {
                number++;
                cardReaderMap.put(term.getName(), term);
            }

            if (number == 0) {
                state = TerminalState.NO_READER;
                return old != state;
            }
            needToRefresh = toSelectReader != null || selectedReader == null || selectedReader.isEmpty() ||
                    !cardReaderMap.containsKey(selectedReader);
            if (needToRefresh) {
                selectedReader = (toSelectReader != null && cardReaderMap.containsKey(toSelectReader))
                        ? toSelectReader : cardReaderMap.firstKey();
                toSelectReader = null;
            }
            checkCardPresence(selectedReader);

        } catch (CardException | NoSuchAlgorithmException ex) {
            //TODO ask changed to getdefault and on exception return false
//            JOptionPane.showMessageDialog(null, ex.getMessage(), "Start", JOptionPane.INFORMATION_MESSAGE);
//            Logger.getLogger(AppletStore.class.getName()).log(Level.SEVERE, null, ex);
            state = TerminalState.NO_READER;
            return old != state;
        }
        return old != state || needToRefresh;
    }

    public CardTerminal getTerminal(String name) {
        return cardReaderMap.get(name);
    }

    public CardTerminal getTerminal() {
        System.out.println(selectedReader);
        return cardReaderMap.get(selectedReader);
    }

    /**
     * Select terminal as a main one, check if contains a card
     * @param name terminal to set as a main terminal
     * @return true if terminal found or card presence detecting didn't fail, false otherwise
     */
    public void selectTerminal(String name) {
        toSelectReader = name;
    }

    public TreeMap<String, CardTerminal> getTerminals() {
        return cardReaderMap;
    }
}
