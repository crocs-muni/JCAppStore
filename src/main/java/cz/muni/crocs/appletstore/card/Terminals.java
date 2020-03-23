package cz.muni.crocs.appletstore.card;

import apdu4j.TerminalManager;
import jnasmartcardio.Smartcardio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Terminal class that manages all card & terminal detection and selection
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class Terminals {

    private static final Logger logger = LoggerFactory.getLogger(Terminals.class);

    public enum TerminalState {
        NO_READER, NO_CARD, LOADING, OK, NO_SERVICE
    }

    private TreeMap<String, CardTerminal> cardReaderMap = new TreeMap<>();
    private String selectedReader = null;
    private String toSelectReader = null;
    private volatile TerminalState state = TerminalState.LOADING;
    private volatile Boolean needsRefresh = false;

    /**
     * Get state of the current selected terminal
     * @return state of selected terminal
     */
    public TerminalState getState() {
        return state;
    }

    /**
     * Get card terminal
     * @return by default selected terminal
     */
    public CardTerminal getTerminal() {
        return cardReaderMap.get(selectedReader);
    }

    /**
     * Get card terminal
     * @param name name of the terminal
     * @return terminal specified by name
     */
    public CardTerminal getTerminal(String name) {
        return cardReaderMap.get(name);
    }

    /**
     * Get the current reader name
     * @return selected reader name
     */
    public String getSelectedReaderName() {
        return selectedReader;
    }

    /**
     * Forces the terminal app to reload
     * package-private: can be used through manager only
     */
    void setNeedsRefresh() {
        this.needsRefresh = true;
    }

    /**
     * Check for terminal and card presence
     * readers are assumed to have only one card at time
     * @return 0 if no change occured, 1 if terminals changed only, but the current card is still present,
     * 2 if everything needs refresh
     * package-private: can be used through manager only
     */
    int checkTerminals() {
        if (needsRefresh) {
            needsRefresh = false;
            return 2;
        }

        int oldHash = cardReaderMap.keySet().hashCode();
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
                return (old != state) ? 2 : 0;
            }

            needToRefresh = toSelectReader != null || selectedReader == null || selectedReader.isEmpty() ||
                    !cardReaderMap.containsKey(selectedReader);
            if (needToRefresh) {
                selectedReader = (toSelectReader != null && cardReaderMap.containsKey(toSelectReader))
                        ? toSelectReader : cardReaderMap.firstKey();
                logger.info("Selected reader: " + selectedReader);
                toSelectReader = null;
            }

            if (!checkCardPresence(selectedReader)) {
                selectedReader = checkAnyCardPresence();
            }

        } catch (Smartcardio.EstablishContextException | Smartcardio.JnaPCSCException | Smartcardio.JnaCardException e) {
            logger.error("Failed to reach the card reader", e);
            state = TerminalState.NO_SERVICE;
            return (old != state) ? 2 : 0;
        } catch (CardException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            logger.warn("Failed to check terminal.", e);
            state = TerminalState.NO_READER;
            return (old != state) ? 2 : 0;
        }
        if (needToRefresh || old != state) return 2;
        if (cardReaderMap.keySet().hashCode() != oldHash) return 1;
        return 0;
    }

    /**
     * Select terminal as a main one, check if contains a card
     * package-private: can be used through manager only
     * @param name terminal to set as a main terminal
     */
    void selectTerminal(String name) {
        toSelectReader = name;
    }

    /**
     * Get the mapping of CardReader name -> instance
     * package-private: can be used through manager only
     * @return map of connected card readers
     */
    Map<String, CardTerminal> getTerminals() {
        return Collections.unmodifiableMap(cardReaderMap);
    }

    /**
     * Look into terminal list to get the card
     *
     * @param terminal terminal to look into
     */
    private void checkCardPresence(CardTerminal terminal) {
        if (terminal == null) {
            state = TerminalState.NO_READER;
            return;
        }
        try {
            state = (checkCardInTerminal(terminal)) ? TerminalState.OK : TerminalState.NO_CARD;
        } catch (CardException e) {
            e.printStackTrace();
            logger.warn("Failed to check card presence in terminal " + terminal.getName(), e);
            state = TerminalState.NO_CARD;
        }
    }

    /**
     * Search all terminals for a presence of any card
     *
     * @return terminal name of a card inserted, the current selected terminal otherwise
     */
    private String checkAnyCardPresence() {
        try {
            for (String term : cardReaderMap.keySet()) {
                if (checkCardInTerminal(cardReaderMap.get(term))) {
                    return term;
                }
            }
        } catch (CardException e) {
            e.printStackTrace();
            logger.warn("Failed to check card presence in terminals.", e);
            return selectedReader;
        }
        return selectedReader;
    }

    /**
     * Check for card presence in a specific terminal name
     *
     * @param terminal name of terminal to llok into
     * @return true if card found, false otherwise
     */
    private boolean checkCardPresence(String terminal) {
        checkCardPresence(cardReaderMap.get(terminal));
        return state == TerminalState.OK;
    }

    private boolean checkCardInTerminal(CardTerminal terminal) throws CardException {
        return terminal.isCardPresent();
    }
}
