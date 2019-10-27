package cz.muni.crocs.appletstore.card.command;

import apdu4j.APDUBIBO;
import pro.javacard.gp.GPSession;

/**
 * GlobalPlatform-like command
 * @author Jiří Horák
 * @version 1.0
 */
public abstract class GPCommand<T> implements CardCommand {

    protected GPSession context;
    protected APDUBIBO channel;

    protected T result;
    public T getResult() {
        return result;
    }

    public void setGP(GPSession session) {
        context = session;
    }
    public void setChannel(APDUBIBO channel) {
        this.channel = channel;
    }
}
