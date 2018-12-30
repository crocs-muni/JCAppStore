package cz.muni.crocs.appletstore.card;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CardCommandExecutionException extends Exception {

    public CardCommandExecutionException(String cause) {
        super(cause);
    }

    public CardCommandExecutionException(String cause, Throwable ex) {
        super(cause, ex);
    }

}
