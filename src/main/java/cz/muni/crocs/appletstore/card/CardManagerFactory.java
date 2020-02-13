package cz.muni.crocs.appletstore.card;

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
