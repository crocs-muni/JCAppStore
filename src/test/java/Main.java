
import cz.muni.crocs.appletstore.card.Terminals;
import cz.muni.crocs.appletstore.card.command.GetDetails;
import pro.javacard.gp.GPException;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Main {

    public static void main(String[] args) throws CardException, GPException {
        Terminals terminals = new Terminals();
        Card c = terminals.getTerminal().connect("*");

        GetDetails details = new GetDetails(c.getBasicChannel());
        details.execute();
    }
}
