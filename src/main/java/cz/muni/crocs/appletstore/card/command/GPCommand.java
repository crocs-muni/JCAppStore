package cz.muni.crocs.appletstore.card.command;

import pro.javacard.gp.GlobalPlatform;

/**
 * GlobalPlatform-like command
 * @author Jiří Horák
 * @version 1.0
 */
public abstract class GPCommand<T> implements CardCommand<T> {

    protected GlobalPlatform context;
    protected String cardId;

    protected T result;
    public T getResult() {
        return result;
    }

    public void setGP(GlobalPlatform platform) {
        context = platform;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }
}
