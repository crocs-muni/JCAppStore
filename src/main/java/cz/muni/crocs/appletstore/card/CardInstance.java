package cz.muni.crocs.appletstore.card;

import pro.javacard.AID;

import java.util.Set;

public interface CardInstance {

    /**
     * Get applets on card
     * @return applets info list
     */
    Set<AppletInfo> getInstalledApplets();

    /**
     * Return applet info associated with AID given
     * @param aid aid to search for
     * @return AppletInfo or null
     */
    AppletInfo getInfoOf(AID aid);

    /**
     * Get card identifier
     * @return card id
     */
    String getId();

    /**
     * Get card name and id
     * @return card descriptor
     */
    String getDescriptor();

    /**
     * Get life cycle of the card
     * @return int, where value determines card state - OP_READY, LOCKED...
     * as designed by GlobalPlatform specification
     */
    Integer getLifeCycle();

    /**
     * Get the default selected applet AID
     * @return null if not default selected applet, AID otherwise
     */
    AID getDefaultSelected();

    /**
     * Get card name (found in card database or provided by user)
     */
    String getName();

    /**
     * Set custom card name
     */
    void setName(String name) throws LocalizedCardException;
}
