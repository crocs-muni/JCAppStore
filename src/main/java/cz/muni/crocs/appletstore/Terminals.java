package cz.muni.crocs.appletstore;

import apdu4j.TerminalManager;
import pro.javacard.gp.GlobalPlatform;

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
        try {
            final TerminalFactory tf;
            //tf = TerminalManager.getTerminalFactory(reader);
            tf = TerminalFactory.getDefault();
            CardTerminals terminals = tf.terminals();
            // List terminals if needed
            System.out.println("# Detected readers from " + tf.getProvider().getName());

            int number = 0;
            cardReaderMap.clear();
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

    public CardTerminal getTerminal(String name) {
        return cardReaderMap.get(name);
    }

    public TreeMap<String, CardTerminal> getTerminals() {
        return cardReaderMap;
    }
}
