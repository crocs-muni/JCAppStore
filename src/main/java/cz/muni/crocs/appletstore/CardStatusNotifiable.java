package cz.muni.crocs.appletstore;

/**
 * To call notification on card change
 */
public interface CardStatusNotifiable {

    /**
     * Updates GUI accoring to card status - OK/NO_CARD.. etc
     */
    void updateCardState();
}
