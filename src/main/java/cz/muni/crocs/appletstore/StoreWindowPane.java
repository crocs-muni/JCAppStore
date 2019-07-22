package cz.muni.crocs.appletstore;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import cz.muni.crocs.appletstore.ui.CustomFlowLayout;
import cz.muni.crocs.appletstore.ui.CustomScrollBarUI;

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
public class StoreWindowPane extends JScrollPane implements Searchable, OnEventCallBack<Void, Void, Void> {

    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private BackgroundChangeable context;
    private JPanel storeLayout = new JPanel();
    private ArrayList<StoreItem> items = new ArrayList<>();
    private List<JsonObject> data;

    public StoreWindowPane(List<JsonObject> data, BackgroundChangeable context) {
        this.data = data;
        this.context = context;

        setOpaque(false);
        getViewport().setOpaque(false);
        storeLayout.setOpaque(false);

        setBorder(BorderFactory.createEmptyBorder());
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        getVerticalScrollBar().setUI(new CustomScrollBarUI());
        getVerticalScrollBar().setUnitIncrement(16);
        getVerticalScrollBar().setOpaque(false);

        storeLayout.setLayout(new CustomFlowLayout(FlowLayout.LEFT, 20, 20));
        storeLayout.setBorder(new EmptyBorder(50, 50, 50, 50));
        loadStore();
    }

    private void loadStore()  {
        items.clear();

        for (JsonObject dataSet : data) {
            StoreItem item = new StoreItem(dataSet);
            item.setCursor(new Cursor(Cursor.HAND_CURSOR));
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showInfo(dataSet);
                }
            });
            items.add(item);
        }
        showPanel(items);
    }

    private void showInfo(JsonObject dataSet) {
        StoreItemInfo info = new StoreItemInfo(dataSet, this, this);
        setViewportView(info);
    }

    private void showPanel(Collection<StoreItem> sortedItems) {
        storeLayout.removeAll();
        if (sortedItems.size() == 0) {
            storeLayout.add(new StoreItem(textSrc.getString("no_results"),
                    "no_results.png", "", ""));
        } else {
            for (StoreItem item : sortedItems) {
                storeLayout.add(item);
            }
        }
        storeLayout.revalidate();
        setViewportView(storeLayout);
    }

    @Override
    public void showItems(String query) {
        if (query.isEmpty()) {
            showPanel(items);
        } else {
            ArrayList<StoreItem> sortedIems = new ArrayList<>();
            for (StoreItem item : items) {
                if (item.getSearchQuery().toLowerCase().contains(query.toLowerCase())) {
                    sortedIems.add(item);
                }
            }
            showPanel(sortedIems);
        }
    }

    @Override
    public Void onStart() {
        context.switchEnabled(false);
        return null;
    }

    @Override
    public Void onFail() {
        context.switchEnabled(true);
        return null;
    }

    @Override
    public Void onFinish() {
        context.switchEnabled(true);
        return null;
    }
}