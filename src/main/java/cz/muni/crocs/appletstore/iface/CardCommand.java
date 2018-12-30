package cz.muni.crocs.appletstore.iface;

import cz.muni.crocs.appletstore.card.CardCommandExecutionException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public interface CardCommand {

    /**
     * Executes the command on a card
     * @return true if execution succeeded
     */
    boolean execute() throws CardCommandExecutionException;
}
