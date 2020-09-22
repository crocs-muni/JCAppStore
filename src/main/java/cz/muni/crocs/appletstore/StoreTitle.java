package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.Title;

import javax.swing.*;
import java.awt.*;

/**
 * Store category title item
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreTitle extends Title implements Item {

    private final int position;

    /**
     * Create a category name
     * @param text category
     * @param position positiona s defined in json info_[lang].json file
     */
    public StoreTitle(String text, int position) {
        super(text, 25f);
        this.position = position;
        setForeground(Color.white);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 0));
        setAlignmentX(CENTER_ALIGNMENT);
    }

    @Override
    public String getSearchQuery() {
        return getText();
    }

    @Override
    public int hashCode() {
        return position;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Item)) return false;
        return obj.hashCode() == hashCode();
    }

    @Override
    public int compareTo(Item o) {
        int thisCode = hashCode();
        int otherCode = o.hashCode();

        if (thisCode == otherCode) {
            return 0;
        }
        return thisCode > otherCode ? 1 : -1;
    }
}
