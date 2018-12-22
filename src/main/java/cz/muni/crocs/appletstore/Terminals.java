package cz.muni.crocs.appletstore;

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
    private String reader;

    private TerminalState state = TerminalState.NO_READER;

    public Terminals(String reader) {
        this.reader = reader;
    }

    public TerminalState getState() {
        return state;
    }

    private boolean checkCardInTerminal(CardTerminal terminal) throws CardException {
        return terminal.isCardPresent();
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

    public boolean checkTerminals() {
        cardReaderMap.clear();
        try {
            final TerminalFactory tf;
            //tf = TerminalFactory.getDefault();  //TODO the differemce?
            tf = TerminalManager.getTerminalFactory(null); //TODO get provider Specification in a // jar:class:args form
            CardTerminals terminals = tf.terminals();

            //System.out.println("# Detected readers from " + tf.getProvider().getName());

            int number = 0;
            for (CardTerminal term : terminals.list()) { //TODO cardException list failed with getDefult(), processes 0x times otherwise
                number++;
                cardReaderMap.put(term.getName(), term);
                checkCardPresence(term);
            }

            if (number == 0) {
                state = TerminalState.NO_READER;
                return false;
            }
        } catch (CardException | NoSuchAlgorithmException ex) {
            //TODO ask changed to getdefault and on exception return false
//            JOptionPane.showMessageDialog(null, ex.getMessage(), "Start", JOptionPane.INFORMATION_MESSAGE);
//            Logger.getLogger(AppletStore.class.getName()).log(Level.SEVERE, null, ex);
            state = TerminalState.NO_READER;
            return false;
        }
        return true;
    }

//    public CardDetails getCardDetails(StringBuilder statusMessage) {
//        if (cardReaderListComboBox.getSelectedItem() == null) {
//            return null;
//        }
//        String reader = cardReaderListComboBox.getSelectedItem().toString();
//
//        if (reader != null && reader.length() > 0) {
//            try {
//                CardTerminal cardTerminal = terminals.getTerminal(reader);
//                if (cardTerminal.isCardPresent()) {
//                    System.out.println("Connect to card");
//                    //statusMessage.append("Connect to card").append(System.lineSeparator());
//                    Card card = null;
//                    CardChannel channel = null;
//                    try {
//                        card = cardTerminal.connect("*");
//                        // We use apdu4j which by default uses jnasmartcardio
//                        // which uses real SCardBeginTransaction
//                        card.beginExclusive();
//                        channel = card.getBasicChannel();
//                        CardDetails cardDetails = new CardDetails();
//                        cardDetails.setAtr(card.getATR());
//                        setCardDetails(channel, cardDetails);
//                        return cardDetails;
//                    } catch (CardException e) {
//                        System.err.println("Could not connect to " + cardTerminal.getName() + ": " + TerminalManager.getExceptionMessage(e));
//                        statusMessage.append(translate.get(21)).append(cardTerminal.getName()).append(": ").append(TerminalManager.getExceptionMessage(e)).append(System.lineSeparator());
//                    } catch (GPException ex) {
//                        Logger.getLogger(JCPlayStoreClient.class.getName()).log(Level.SEVERE, null, ex);
//                    } finally {
//                        if (card != null) {
//                            card.endExclusive();
//                            card.disconnect(true);
//                            card = null;
//                        }
//                    }
//                } else {
//                    System.out.println("Card is not present!!!");
//                    statusMessage.append(translate.get(22)).append(System.lineSeparator());
//                }
//            } catch (CardException ex) {
//                JOptionPane.showMessageDialog(this, ex.getMessage(), "List", JOptionPane.INFORMATION_MESSAGE);
//                Logger.getLogger(JCPlayStoreClient.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return null;
//    }

    public CardTerminal getTerminal(String name) {
        return cardReaderMap.get(name);
    }

    /**
     * Select terminal as a main one, check if contains a card
     * @param name terminal to set as a main terminal
     * @return true if terminal found or card presence detecting didn't fail, false otherwise
     */
    public boolean selectTerminal(String name) {
        CardTerminal terminal = cardReaderMap.get(name);
        if (terminal == null) return false;
        try {
            checkCardInTerminal(terminal);
        } catch (CardException e) {
            state = TerminalState.NO_CARD;
            return false;
        }
        return true;
    }

    public TreeMap<String, CardTerminal> getTerminals() {
        return cardReaderMap;
    }
}
