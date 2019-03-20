package cz.muni.crocs.appletstore.iface;

import pro.javacard.gp.GPException;

import javax.smartcardio.CardException;

/**
 * Interface for custom overlaylayout
 * @author Jiří Horák
 * @version 1.0
 */
public interface AbsolutePositionEditable {

    /**
     * Set new absolute position of a child
     */
    void setNewPosition(int childIdx, int x, int y);
}
