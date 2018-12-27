package cz.muni.crocs.appletstore.iface;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public interface Searchable {

    /**
     * Display all items with reference to given string
     * @param query search query for items to look for
     */
    void showItems(String query);
}
