package cz.muni.crocs.appletstore;

import apdu4j.TerminalManager;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GlobalPlatform;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import javax.swing.*;
import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Terminals {

    private TreeMap<String, CardTerminal> cardReaderMap = new TreeMap<>();
    private String reader;

    private boolean found = false;

    public Terminals(String reader) {
        this.reader = reader;
    }

    public void update() {
        found = checkTerminals();
    }

    public boolean isFound() {
        return found;
    }

    private boolean checkTerminals() {
        cardReaderMap.clear();
        found = false;
        try {
            final TerminalFactory tf;
            //tf = TerminalManager.getTerminalFactory(reader);
            tf = TerminalFactory.getDefault();
            CardTerminals terminals = tf.terminals();
            // List terminals if needed
            System.out.println("# Detected readers from " + tf.getProvider().getName());

            int number = 0;
            for (CardTerminal term : terminals.list()) {
                number++;
                cardReaderMap.put(term.getName(), term);
                System.out.println((term.isCardPresent() ? "[*] " : "[ ] ") + term.getName());
            }

            if (number == 0) {
                System.out.println("no readers.");
                return false;
            }
        } catch (CardException ex) {
            //TODO ask changed to getdefault and on exception return false
//            JOptionPane.showMessageDialog(null, ex.getMessage(), "Start", JOptionPane.INFORMATION_MESSAGE);
//            Logger.getLogger(AppletStore.class.getName()).log(Level.SEVERE, null, ex);
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

    public TreeMap<String, CardTerminal> getTerminals() {
        return cardReaderMap;
    }
}
