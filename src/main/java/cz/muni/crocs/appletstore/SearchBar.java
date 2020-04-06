package cz.muni.crocs.appletstore;

public interface SearchBar {

    /**
     * Get search query
     * @return search query provided by user
     */
    String getQuery();

    /**
     * Reset search bar
     */
    void resetSearch();
}
