package cz.muni.crocs.appletstore;

/**
 * Class enables to search, displays items according to the string given.
 * @author Jiří Horák
 * @version 1.0
 */
public interface Searchable {

    /**
     * Display all items with reference to given string
     * @param query search query for items to look for,
     *              display all items if empty query
     */
    void showItems(String query);

    /**
     * Refreshes currently displayed page
     */
    void refresh();
}
