package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.ui.CustomFlowLayout;
import cz.muni.crocs.appletstore.ui.CustomScrollBarUI;
import cz.muni.crocs.appletstore.ui.DisablePanel;
import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.ui.LoadingPaneCircle;

import cz.muni.crocs.appletstore.util.OnEventCallBack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;


/**
 * Panel that lists all card contents. Allows the applet
 * installation through special LocalInstallItem object
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalWindowPane extends DisablePanel implements Searchable {

    private static final Logger logger = LogManager.getLogger(LocalWindowPane.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private OnEventCallBack<Void, Void, Void> callback;
    private LocalSubMenu submenu;
    private LocalItemInfo infoLayout;
    private JPanel windowLayout;
    private JScrollPane windowScroll;

    private TreeSet<LocalItem> items = new TreeSet<>();
    private LocalInstallItem installCmd = new LocalInstallItem();

    private GridBagConstraints constraints;

    public LocalWindowPane(BackgroundChangeable context) {
        callback = new WorkCallback(context);
        setOpaque(false);

        submenu = new LocalSubMenu();
        submenu.setOnSubmit(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showItems(null);
            }
        });

        GridBagLayout gb = new GridBagLayout();
        gb.columnWeights = new double[]{1d, 0.1d};
        gb.rowWeights = new double[]{0.01d, 1d};
        this.setLayout(gb);

        constraints = new GridBagConstraints();

        setupComponents();
        updatePanes();
    }

    @Override
    public void showItems(String query) {
        if (query == null || query.isEmpty()) {
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

//    @Override
//    protected void paintComponent(Graphics g) {
//        infoLayout.setVisible(CardManagerFactory.getManager().isAppletSelected());
//        super.paintComponent(g);
//    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        infoLayout.setVisible(CardManagerFactory.getManager().isAppletSelected());
    }

    /**
     * Update the local pane according to the info obtained from the Card Manager
     */
    public void updatePanes() {
        removeAll();
        infoLayout.set(null);

        CardManager manager = CardManagerFactory.getManager();
        logger.info("Local pane updated: " + manager.getTerminalState().toString());
        if (verifyTerminalState(manager.getTerminalState())
                && verifyCardLifeState(manager.getCardLifeCycle())) {

            List<AppletInfo> cardApplets = manager.getInstalledApplets();
            if (cardApplets == null) {
                showError("no-card.png", "failed_to_list_aps");
                logger.warn("Applet list failed, null returned.");
                return;
            } else {
                loadApplets(manager.getInstalledApplets(), manager);
            }

            constraints.fill = GridBagConstraints.BOTH;

            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridwidth = 2;
            add(submenu, constraints);

            constraints.gridy = 1;
            constraints.gridwidth = 1;
            add(windowScroll, constraints);
            constraints.gridx = 1;

            add(infoLayout, constraints);

            infoLayout.setBackground(Color.WHITE);
        }
        revalidate();
    }

    public void updatePanes(String errorTitleKey, String errorText) {
        removeAll();
        add(new ErrorPane(textSrc.getString(errorTitleKey), errorText, "announcement_white.png"));
        revalidate();
    }

    /**
     * Setup Swing components
     */
    private void setupComponents() {
        infoLayout = new LocalItemInfo(callback);
        windowLayout = new JPanel();
        windowScroll = new JScrollPane();

        windowScroll.setOpaque(false);
        windowScroll.getViewport().setOpaque(false);
        windowScroll.setOpaque(false);
        windowScroll.setBorder(BorderFactory.createEmptyBorder());
        windowScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        windowScroll.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        windowScroll.getVerticalScrollBar().setUnitIncrement(16);
        windowScroll.getVerticalScrollBar().setOpaque(false);

        windowLayout.setLayout(new CustomFlowLayout(FlowLayout.LEFT, 20, 20));
        windowLayout.setBorder(new EmptyBorder(10, 50, 50, 50));
        windowLayout.setOpaque(false);

        installCmd.addMouseListener(new InstallAction(callback));
    }

    /**
     * Verify whether the terminal is persent and card inserted
     * @param state state of the terminal
     * @return true if card present
     */
    private boolean verifyTerminalState(Terminals.TerminalState state) {
        switch (state) {
            case OK:
                break;
            case NO_CARD:
                showError("no-card.png", "no_card");
                return false;
            case NO_READER:
                showError("no-reader.png", "no_reader");
                return false;
            case LOADING:
                add(new LoadingPaneCircle());
                return false;
            default:
        }
        return true;
    }

    private boolean verifyCardLifeState(Integer isdLifeState) {
        if (isdLifeState == null) {
            showError("E_authentication", "H_authentication", "announcement_white.png");
            return false;
        }
        switch (isdLifeState) {
            case 0x1:
                return true;
            case 0x7:
                showError("E_initialized", "H_initialized", "announcement_white.png");
                return false;
            case 0xF:
                showError("E_secure_state", "H_secure_state", "announcement_white.png");
                return false;
            case 0x7F:
                showError("E_locked", "H_locked", "announcement_white.png");
                return false;
            case 0xFF:
                showError("E_terminated", "H_terminated", "announcement_white.png");
                return false;
            default:
                showError("E_no_life_state", "H_no_life_state", "announcement_white.png");
                return false;
        }
    }

    private void showError(String keyTitle, String keyDesc, String imgName) {
        add(new ErrorPane(textSrc.getString(keyTitle), textSrc.getString(keyDesc), imgName));
    }

    private void showError(String imageName, String titleKey) {
        add(new ErrorPane(textSrc.getString(titleKey), imageName));
    }

    private void loadApplets(List<AppletInfo> applets, CardManager manager) {
        items.clear();
        for (AppletInfo appletInfo : applets) {
            LocalItem item = new LocalItem(appletInfo);
            item.setCursor(new Cursor(Cursor.HAND_CURSOR));
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    manager.switchApplet(item.info.getAid());
                    if (manager.isAppletSelected())
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
            windowLayout.add(new LocalItem(textSrc.getString("no_results"),
                    "no_results.png", "", "", null));
        } else {
            for (LocalItem item : sortedItems) {
                if (submenu.accept(item.info.getKind()))
                    windowLayout.add(item);
            }
        }
        windowLayout.add(installCmd);
        windowLayout.revalidate();
    }
}
