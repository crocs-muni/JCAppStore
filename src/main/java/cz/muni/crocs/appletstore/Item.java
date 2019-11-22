package cz.muni.crocs.appletstore;

/**
 * Interface to mark class as displayable as items to click on in store or on card installed applets
 * @author Jiří Horák
 * @version 1.0
 */
public interface Item extends Comparable<Item> {

    /**
     * Method to get the item's description query to compare to user search query
     * @return item description, should contain title, maybe author and some other keywords (AID..)
     */
    String getSearchQuery();

    default String adjustLength(String value, int length) {
        if (value.length() <= length) return value;
        return value.substring(0, length - 3) + "...";
    }
}
