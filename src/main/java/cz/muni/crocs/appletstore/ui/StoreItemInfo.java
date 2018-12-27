package cz.muni.crocs.appletstore.ui;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.Config;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreItemInfo extends JPanel {

    public StoreItemInfo(JsonObject dataSet) {
        setOpaque(false);
        JLabel icon = new JLabel(new ImageIcon(Config.RESOURCES + dataSet.get(Config.JSON_TAG_ICON).getAsString()));

        add(icon);

    }
}
