package cz.muni.crocs.appletstore.card;

/**
 * Factory patter for manager
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardManagerFactory {
    private static CardManager manager = null;

    public static CardManager getManager() {
        if (manager == null) {
            manager = new CardManagerImpl();
        }
        return manager;
    }

    private CardManagerFactory() {}
}
