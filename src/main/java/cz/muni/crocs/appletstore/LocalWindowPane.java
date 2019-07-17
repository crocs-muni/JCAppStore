package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.action.InstallAction;
import cz.muni.crocs.appletstore.card.CardInstance;
import cz.muni.crocs.appletstore.card.CardManagerImpl;
import cz.muni.crocs.appletstore.card.Terminals;
import cz.muni.crocs.appletstore.iface.CardManager;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import cz.muni.crocs.appletstore.iface.Searchable;
import cz.muni.crocs.appletstore.ui.CustomFlowLayout;
import cz.muni.crocs.appletstore.ui.CustomScrollBarUI;
import cz.muni.crocs.appletstore.ui.DisablePanel;
import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.ui.LoadingPaneCircle;
import cz.muni.crocs.appletstore.ui.Warning;
import cz.muni.crocs.appletstore.card.AppletInfo;

import cz.muni.crocs.appletstore.util.Sources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;


/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalWindowPane extends DisablePanel implements Searchable, OnEventCallBack<Void, Void, Void> {

    private static final Logger logger = LogManager.getLogger(LocalWindowPane.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private AppletStore context;
    private LocalItemInfo infoLayout = new LocalItemInfo(this);
    //private JSplitPane splitPane
    private JPanel windowLayout = new JPanel();
    private JScrollPane windowScroll = new JScrollPane();

    private TreeSet<LocalItem> items = new TreeSet<>();
    private LocalInstallItem installCmd = new LocalInstallItem();

    private GridBagConstraints constraints;

    public LocalWindowPane(AppletStore context) {
        this.context = context;
        setOpaque(false);

        GridBagLayout gb = new GridBagLayout();
        gb.columnWeights = new double[]{1, 0.1d};
        gb.rowWeights = new double[]{1};
        this.setLayout(gb);

        constraints = new GridBagConstraints();

        //GENERAL SETTINGS
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

        installCmd.addMouseListener(new InstallAction(this));

        updatePanes(Sources.manager.getTerminalState());
    }

    private void addError(String imageName, String titleKey) {
        add(new ErrorPane(textSrc.getString(titleKey), imageName));
    }

    public void updatePanes(Terminals.TerminalState state) {
        removeAll();
        revalidate();
        System.out.println("updated:" + state);

        CardManager manager = Sources.manager;
        if (verifyTerminalState(state)
                && verifyCardState(manager)
                && verifyCardLifeState(manager.getCardLifeCycle())) {

            CardInstance card = manager.getCard();
            List<AppletInfo> cardApplets = card.getApplets();
            if (cardApplets == null) {
                //todo ???
                updatePanes(Terminals.TerminalState.LOADING);
                return;
            } else {
                loadApplets(card.getApplets(), manager);
            }

            constraints.fill = GridBagConstraints.BOTH;
            constraints.gridx = 0;
            constraints.gridy = 0;
            add(windowScroll, constraints);
            constraints.gridx = 1;

            infoLayout.setVisible(false);
            add(infoLayout, constraints);

            infoLayout.setBackground(Color.WHITE);
        }
    }

    private boolean verifyTerminalState(Terminals.TerminalState state) {
        switch (state) {
            case OK:
                break;
            case NO_CARD:
                addError("no-card.png", "no_card");
                return false;
            case NO_READER:
                addError("no-reader.png", "no_reader");
                return false;
            case LOADING:
                add(new LoadingPaneCircle());
                return false;
            default:
        }
        return true;
    }

    private boolean verifyCardState(CardManager manager) {
        switch (manager.getCard().getState()) {
            case OK:
                break;
            case WORKING:
                return false;
            case FAILED:
                if (items.isEmpty())
                    add(new ErrorPane(textSrc.getString("E_communication"),
                            manager.getErrorCause(), "announcement_white.png"));
                else
                    Informer.getInstance().showWarningToClose(manager.getErrorCause(), Warning.Importance.SEVERE);
                return false;
            default: //continue, probably card locked
        }
        return true;
    }

    private boolean verifyCardLifeState(Integer isdLifeState) {
        if (isdLifeState == null) {
            add(new ErrorPane(textSrc.getString("E_authentication"), textSrc.getString("H_authentication"), "announcement_white.png"));
            return false;
        }
        switch (isdLifeState) {
            case 0x1:
                return true;
            case 0x7:
                add(new ErrorPane(textSrc.getString("E_initialized"), textSrc.getString("H_initialized"), "announcement_white.png"));
                return false;
            case 0xF:
                add(new ErrorPane(textSrc.getString("E_secure_state"), textSrc.getString("H_secure_state"), "announcement_white.png"));
                return false;
            case 0x7F:
                add(new ErrorPane(textSrc.getString("E_locked"), textSrc.getString("H_locked"), "announcement_white.png"));
                return false;
            case 0xFF:
                add(new ErrorPane(textSrc.getString("E_terminated"), textSrc.getString("H_terminated"), "announcement_white.png"));
                return false;
            default:
                add(new ErrorPane(textSrc.getString("E_no_life_state"), textSrc.getString("H_no_life_state"), "announcement_white.png"));
                return false;
        }
    }

    private void loadApplets(ArrayList<AppletInfo> applets, CardManager manager) {
        items.clear();
        for (AppletInfo appletInfo : applets) {

            LocalItem item = new LocalItem(appletInfo);
            item.setCursor(new Cursor(Cursor.HAND_CURSOR));
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    manager.select(item.info.getAid());
                    if (manager.isSelected())
                        infoLayout.set(item.info);
                    else
                        infoLayout.unset();
                }
            });
            items.add(item);
        }
        showPanel(items);
        windowScroll.setViewportView(windowLayout);
    }

    private void showPanel(Collection<LocalItem> sortedItems) {
        windowLayout.removeAll();
        if (sortedItems.size() == 0) {
            windowLayout.add(new LocalItem(textSrc.getString("no_results"), "no_results.png", "", "", null));
        } else {
            for (LocalItem item : sortedItems) {
                windowLayout.add(item);
            }
        }
        windowLayout.add(installCmd);
        windowLayout.revalidate();
    }

    @Override
    public void showItems(String query) {
        if (query.isEmpty()) {
            showPanel(items);
        } else {
            TreeSet<LocalItem> sortedIems = new TreeSet<>();
            for (LocalItem item : items) {
                if (item.getSearchQuery().toLowerCase().contains(query.toLowerCase())) {
                    sortedIems.add(item);
                }
            }
            showPanel(sortedIems);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        infoLayout.setVisible(Sources.manager.isSelected());
        super.paintComponent(g);
    }

    @Override
    public Void onStart() {
        return null;
    }

    @Override
    public Void onFail() {
        return null;
    }

    @Override
    public Void onFinish() {
        return null;
    }
}
