package cz.muni.crocs.appletstore;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.iface.Searchable;
import cz.muni.crocs.appletstore.ui.CustomFlowLayout;
import cz.muni.crocs.appletstore.ui.CustomScrollBarUI;
import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.ui.LoadingPane;
import cz.muni.crocs.appletstore.ui.Warning;
import cz.muni.crocs.appletstore.util.DownloaderWorker;
import cz.muni.crocs.appletstore.util.FileCleaner;
import cz.muni.crocs.appletstore.util.JSONStoreParser;
import cz.muni.crocs.appletstore.util.Sources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static cz.muni.crocs.appletstore.StoreWindowManager.StoreState.*;

/**
 * Scroll pane with items from store
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreWindowPane extends JScrollPane implements Searchable {

    private static final Logger logger = LogManager.getLogger(StoreWindowPane.class);

    private JPanel storeLayout = new JPanel();
    private ArrayList<StoreItem> items = new ArrayList<>();
    private List<JsonObject> data;

    public StoreWindowPane(List<JsonObject> data) {
        this.data = data;

        setOpaque(false);
        getViewport().setOpaque(false);
        storeLayout.setOpaque(false);

        setBorder(BorderFactory.createEmptyBorder());
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //custom scroll bar design
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
        StoreItemInfo info = new StoreItemInfo(dataSet, this);
        setViewportView(info);
    }

    private void showPanel(Collection<StoreItem> sortedItems) {
        storeLayout.removeAll();
        if (sortedItems.size() == 0) {
            storeLayout.add(new StoreItem(Sources.language.get("no_results"),
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
}