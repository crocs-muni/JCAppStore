package cz.muni.crocs.appletstore.card;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

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
