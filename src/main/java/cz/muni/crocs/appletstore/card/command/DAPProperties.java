package cz.muni.crocs.appletstore.card.command;
import pro.javacard.AID;
import pro.javacard.gp.GPRegistry;
import pro.javacard.gp.GPSession;

import java.io.IOException;

/**
 * COPAPASTE, TODO WILL BE REMOVED
 */
public class DAPProperties {
    private AID targetDomain = null;
    private AID dapDomain = null;
    private boolean required = false;

    public DAPProperties(GPSession gp) throws IOException, IllegalArgumentException {
        GPRegistry reg = gp.getRegistry();
    }

    public AID getTargetDomain() {
        return targetDomain;
    }

    public AID getDapDomain() {
        return dapDomain;
    }

    public boolean isRequired() {
        return required;
    }
}
