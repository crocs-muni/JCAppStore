package cz.muni.crocs.appletstore;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.card.CardInstance;
import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.Terminals;
import cz.muni.crocs.appletstore.iface.Searchable;
import cz.muni.crocs.appletstore.ui.CustomFlowLayout;
import cz.muni.crocs.appletstore.ui.CustomScrollBarUI;
import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.util.AppletInfo;
import cz.muni.crocs.appletstore.util.Cleaner;
import cz.muni.crocs.appletstore.util.JSONStoreParser;
import net.miginfocom.swing.MigLayout;
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
import java.util.HashMap;

import static cz.muni.crocs.appletstore.StoreWindowPane.StoreState.UNINITIALIZED;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalWindowPane extends JPanel implements Searchable {

    private static final Logger logger = LogManager.getLogger(LocalWindowPane.class);

    private AppletStore context;
    private JToolBar tools = new JToolBar();
    private JPanel infoLayout = new JPanel();
    private JSplitPane splitPane;
    private JPanel windowLayout = new JPanel();
    private JScrollPane windowScroll = new JScrollPane();

    private GridBagConstraints constraints;
    private ArrayList<LocalItem> items = new ArrayList<>();

    public LocalWindowPane(AppletStore context) {
        this.context = context;
        setLayout(new GridBagLayout());
        setOpaque(false);
        constraints = new GridBagConstraints();

        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
    }


    public void updatePanes(Terminals.TerminalState state) {
        removeAll();
        revalidate();

        switch (state) {
            case OK:
                setupWindow();
                break;
            case NO_CARD:
                addError("no-card.png", 5);
                break;
            case NO_READER:
                addError("no-reader.png", 2);
                break;
            default:
        }
    }

    private void addError(String imageName, int translationId) {
        add(new ErrorPane(translationId, imageName));
    }

    private void setupWindow() {
        CardManager manager = context.manager();
        CardInstance card = manager.getCard();

        if (card.getState() == CardInstance.CardState.LOCKED) {

        } else if (card.getState() == CardInstance.CardState.UNAUTHORIZED) {

        } else {
            windowScroll.setOpaque(false);
            windowScroll.getViewport().setOpaque(false);
            windowScroll.setOpaque(false);

            windowScroll.setBorder(BorderFactory.createEmptyBorder());
            //never show horizontal one
            windowScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            //custom scroll bar design
            windowScroll.getVerticalScrollBar().setUI(new CustomScrollBarUI());
            //speed up scrolling
            windowScroll.getVerticalScrollBar().setUnitIncrement(16);
            windowScroll.getVerticalScrollBar().setOpaque(false);

            windowLayout.setLayout(new CustomFlowLayout(FlowLayout.LEFT, 20, 20));
            windowLayout.setBorder(new EmptyBorder(50, 50, 50, 50));
            windowLayout.setOpaque(false);

            try {
                loadApplets(card.getApplets(), manager);
            } catch (IOException e) {
                //todo handle
                e.printStackTrace();
            }

            //todo discards the crolling feature, maybe info as a jump dialog window?
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    windowLayout, infoLayout);
            splitPane.setOpaque(false);
            splitPane.setOneTouchExpandable(true);
            splitPane.setDividerLocation(150);

//Provide minimum sizes for the two components in the split pane
            Dimension minimumSize = new Dimension(100, 0);
            windowLayout.setMinimumSize(minimumSize);
            infoLayout.setMinimumSize(minimumSize);

            infoLayout.setOpaque(false);

            constraints.fill = GridBagConstraints.BOTH;
            add(splitPane, constraints);


//            tools.add(new JLabel("Install"));
//            tools.add(new JLabel("Update"));
//            tools.add(new JLabel("Delete"));
//            add(infoLayout, "dock east");


            infoLayout.setBackground(Color.WHITE);
        }
    }


    private boolean loadApplets(ArrayList<AppletInfo> applets, CardManager manager) throws IOException {
        items.clear();
        for (AppletInfo appletInfo : applets) {

            LocalItem item = new LocalItem(appletInfo);
            item.setCursor(new Cursor(Cursor.HAND_CURSOR));
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    manager.select(item.info.getAid());
                }
            });
            items.add(item);
        }
        showPanel(items);
        windowScroll.setViewportView(windowLayout);
        return true;
    }

    private void showPanel(Collection<LocalItem> sortedItems) {
        windowLayout.removeAll();
        if (sortedItems.size() == 0) {
            try {
                windowLayout.add(new LocalItem(Config.translation.get(113),
                        "no_results.png", "", "", null));
            } catch (IOException e) {
                e.printStackTrace();
                //todo handle, log
            }
        } else {
            for (LocalItem item : sortedItems) {
                windowLayout.add(item);
            }
        }
        windowLayout.revalidate();
    }


    private void showAppletInfo() {

    }

    @Override
    public void showItems(String query) {
        if (query.isEmpty()) {
            showPanel(items);
        } else {
            ArrayList<LocalItem> sortedIems = new ArrayList<>();
            for (LocalItem item : items) {
                if (item.getSearchQuery().toLowerCase().contains(query.toLowerCase())) {
                    sortedIems.add(item);
                }
            }
            showPanel(sortedIems);
        }
    }
}
