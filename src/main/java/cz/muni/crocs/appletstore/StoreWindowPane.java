package cz.muni.crocs.appletstore;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.util.JsonParser;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import cz.muni.crocs.appletstore.ui.CustomFlowLayout;
import cz.muni.crocs.appletstore.ui.CustomScrollBarUI;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Scroll pane with items from store
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreWindowPane extends JScrollPane implements Searchable {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private OnEventCallBack<Void, Void> callback;
    private JPanel storeLayout = new JPanel();
    private TreeSet<Item> items = new TreeSet<>();
    private List<JsonObject> data;
    private JsonObject currentlyShown;

    public StoreWindowPane(List<JsonObject> data, OnEventCallBack<Void, Void> callback) {
        this.data = data;
        this.callback = callback;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        setViewportBorder(null);
        getViewport().setOpaque(false);

        setBorder(BorderFactory.createEmptyBorder());
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        getVerticalScrollBar().setUI(new CustomScrollBarUI());
        getVerticalScrollBar().setUnitIncrement(16);
        getVerticalScrollBar().setOpaque(false);

        storeLayout.setOpaque(false);
        storeLayout.setLayout(new CustomFlowLayout(FlowLayout.LEFT, 11, 11));
        storeLayout.setBorder(new EmptyBorder(0, 50, 50, 50));
        loadStore();
    }

    private void loadStore()  {
        items.clear();

        int position = 0;
        String category = "";
        for (JsonObject dataSet : data) {
            if (dataSet.get(JsonParser.TAG_TYPE).getAsString().equals("category")) {
                category = dataSet.get(JsonParser.TAG_TITLE).getAsString();
                items.add(new StoreTitle(category, position++));
            } else {
                StoreItem item = new StoreItem(dataSet, category, position++);
                item.setCursor(new Cursor(Cursor.HAND_CURSOR));
                item.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showInfo(dataSet);
                    }
                });
                items.add(item);
            }
        }
        showPanel(items);
    }

    @Override
    public void refresh() {
        if (currentlyShown == null)
            showItems(null);
        else
            showInfo(currentlyShown);
    }

    private void showInfo(JsonObject dataSet) {
        currentlyShown = dataSet;
        setViewportView(new StoreItemInfo(dataSet, this, callback));
    }

    private void showPanel(SortedSet<Item> sortedItems) {
        storeLayout.removeAll();

        if (sortedItems.size() == 0) {
            storeLayout.add(new NotFoundItem());
        } else {

            for (Item item : sortedItems) {
                storeLayout.add((JComponent)item);
            }
        }
        storeLayout.revalidate();
        currentlyShown = null;
        setViewportView(storeLayout);
    }

    @Override
    public void showItems(String query) {
        if (query == null || query.isEmpty()) {
            showPanel(items);
        } else {
            TreeSet<Item> sortedIems = new TreeSet<>();
            for (Item item : items) {
                if (item.getSearchQuery().toLowerCase().contains(query.toLowerCase())) {
                    sortedIems.add(item);
                }
            }
            showPanel(sortedIems);
        }
    }
}