package cz.muni.crocs.appletstore.card.command;

import pro.javacard.gp.GPException;

import javax.smartcardio.CardException;

/**
 * Interface for javacard commands that don't require secure channel
 * such as obtaining card info
 * @author Jiří Horák
 * @version 1.0
 */
public interface CardCommand<T> {

    /**
     * Executes the command on a card
     * using insecure card channel (no key required)
     * @return true if execution succeeded
     */
    boolean execute() throws CardException, GPException;
}
