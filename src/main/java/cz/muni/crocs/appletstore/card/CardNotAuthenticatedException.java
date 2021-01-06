package cz.muni.crocs.appletstore.card;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import java.util.ResourceBundle;

/**
 * Exception to be thrown on unknown key
 * indicates that user should've been asked for keys to provide
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardNotAuthenticatedException extends Exception {
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private final String cardId;

    private String localizeMsg;

    public CardNotAuthenticatedException(String cardId) {
        this.cardId = cardId;
    }

    public CardNotAuthenticatedException(String cardId, String localizeMsg) {
        this.cardId = cardId;
        this.localizeMsg = localizeMsg;
    }

    @Override
    public String getMessage() {
        return "Unknown key for the card: " + cardId;
    }

    @Override
    public String getLocalizedMessage() {
        if (localizeMsg != null) return localizeMsg;
        return textSrc.getString("H_not_authenticated");
    }
}
