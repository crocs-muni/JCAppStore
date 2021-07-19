package cz.muni.crocs.appletstore;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.ui.CustomJScrollPaneLayout;
import cz.muni.crocs.appletstore.util.JsonParser;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
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
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreWindowPane extends JScrollPane implements Searchable {
    private final StoreWindowManager manager;
    private final JPanel storeLayout = new JPanel();
    private final TreeSet<Item> items = new TreeSet<>();
    private final List<JsonObject> data;
    private JsonObject currentlyShown;
    private SearchBar searchBar;
    private final StoreHeader header;
    private boolean showAll = false;
    private static final String CALLBACK_TAG = "ITEM_DETAILS_JCAPPSTORETAG_REFTERSHCARD";

    /**
     * Store panel
     */
    public StoreWindowPane(StoreWindowManager manager, List<JsonObject> data) {
        this.manager = manager;
        this.data = data;
        setLayout(new CustomJScrollPaneLayout());
        setOpaque(true);
        setBackground(new Color(238, 238, 238));

        setBorder(null);
        setViewportBorder(null);

        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        getVerticalScrollBar().setUI(new CustomScrollBarUI());
        getVerticalScrollBar().setUnitIncrement(16);
        getVerticalScrollBar().setOpaque(false);

        getVerticalScrollBar().setBorder(null);
        getHorizontalScrollBar().setBorder(null);

        setComponentZOrder(getVerticalScrollBar(), 0);
        setComponentZOrder(getViewport(), 1);

        //storeLayout.setOpaque(false);
        storeLayout.setLayout(new CustomFlowLayout(FlowLayout.LEFT, 9, 1, 1000));
        storeLayout.setBorder(new EmptyBorder(0, 0, 50, 0));

        this.header = new StoreHeader(this);
        loadStore();

        CardManagerFactory.getManager().onReload(() -> {
            SwingUtilities.invokeLater(this::refresh);
            return null;
        }, CALLBACK_TAG);
    }


    private void loadStore() {
        items.clear();

        int position = 0;
        String category = "";
        for (JsonObject dataSet : data) {
            if (dataSet.get(JsonParser.TAG_TYPE).getAsString().equals("category")) {
                category = dataSet.get(JsonParser.TAG_TITLE).getAsString();
                items.add(new StoreTitle(category, position++, dataSet.get(JsonParser.TAG_HIDDEN).getAsBoolean()));
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
    }

    public void redownload() {
        //delegate
        manager.redownload();
    }

    public void setShowAll(boolean showAll) {
        if (this.showAll == showAll) return;
        this.showAll = showAll;
        refresh();
    }

    @Override
    public void refresh() {
        if (currentlyShown == null) {
            showItems(searchBar.getQuery());
        } else {
            showInfo(currentlyShown);
        }
    }

    @Override
    public void registerSearchBar(SearchBar bar) {
        this.searchBar = bar;
        showItems(null);
    }

    private void showInfo(JsonObject dataSet) {
        currentlyShown = dataSet;

        JPanel container = new JPanel();
        container.setBackground(getBackground());
        StoreItemInfo info = new StoreItemInfo(this, dataSet);
        container.add(info);

        setViewportView(container);
    }

    private void showPanel(SortedSet<Item> sortedItems) {
        storeLayout.removeAll();
        storeLayout.add(header);

        if (sortedItems.size() == 0) {
            storeLayout.add(new NotFoundItem());
        } else {
            for (Item item : sortedItems) {
                storeLayout.add((JComponent) item);
            }
        }
        storeLayout.revalidate();
        currentlyShown = null;
        setViewportView(storeLayout);
    }

    @Override
    public void showItems(String query) {
        if (query == null) query = searchBar.getQuery();
        TreeSet<Item> sortedIems = new TreeSet<>();
        if (query == null || query.isEmpty()) {
            for (Item item : items) {
                if (showAll || !item.byDefaultHidden()) {
                    sortedIems.add(item);
                }
            }
            showPanel(items);
        } else {
            for (Item item : items) {
                if ((showAll || !item.byDefaultHidden()) && item.getSearchQuery().toLowerCase().contains(query.toLowerCase())) {
                    sortedIems.add(item);
                }
            }
        }
        showPanel(sortedIems);
    }
}