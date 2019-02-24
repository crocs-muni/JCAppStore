package cz.muni.crocs.appletstore;


import cz.muni.crocs.appletstore.card.CardInstance;
import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.Terminals;
import cz.muni.crocs.appletstore.iface.Searchable;
import cz.muni.crocs.appletstore.ui.CapFileView;
import cz.muni.crocs.appletstore.ui.CustomFlowLayout;
import cz.muni.crocs.appletstore.ui.CustomScrollBarUI;
import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.ui.LoadingPaneSimple;
import cz.muni.crocs.appletstore.util.AppletInfo;

import cz.muni.crocs.appletstore.util.Informer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.smartcardio.CardException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;


/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalWindowPane extends JPanel implements Searchable {

    private static final Logger logger = LogManager.getLogger(LocalWindowPane.class);

    private AppletStore context;
    private JToolBar tools = new JToolBar();
    private LocalItemInfo infoLayout = new LocalItemInfo();
    //private JSplitPane splitPane
    private JPanel windowLayout = new JPanel();
    private JScrollPane windowScroll = new JScrollPane();

    private TreeSet<LocalItem> items = new TreeSet<>();

    private LocalInstallItem installCmd = new LocalInstallItem();

    public LocalWindowPane(AppletStore context) {
        this.context = context;
        setLayout(new GridBagLayout());
        setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;

        installCmd.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser(Config.APP_LOCAL_DIR);
                fileChooser.setFileView(new CapFileView());
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(Config.translation.get(130), "cap"));
                fileChooser.setAcceptAllFileFilterUsed(false);
                int r = fileChooser.showOpenDialog(null);

                InstallDialogWindow opts = new InstallDialogWindow();
                String[] additionalInfo = null;
                if (r == JFileChooser.APPROVE_OPTION) {
                    int result = JOptionPane.showConfirmDialog(context,
                            opts,
                            Config.translation.get(128),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            new ImageIcon(Config.IMAGE_DIR + "error.png"));

                    switch(result){
                        case JOptionPane.YES_OPTION:
                            if (!opts.validAID() || !opts.validInstallParams()) {
                                Informer.getInstance().showInfo(153);
                                return;
                            }
                            additionalInfo = opts.getAdditionalInfo();
                            break;
                        case JOptionPane.NO_OPTION:
                        case JOptionPane.CLOSED_OPTION:
                            return;
                    }
                    try {
                        CardManager.getInstance().install(fileChooser.getSelectedFile(), additionalInfo);
                    } catch (CardException e1) {
                        //todo log and notify
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        Informer.getInstance().showInfo("Failed to read data");
                    }
                }
            }
        });
    }


    public void updatePanes(Terminals.TerminalState state) {
        removeAll();
        revalidate();
        System.out.println("updated");
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
            case LOADING:
                add(new LoadingPaneSimple());
                break;
            default:
        }
    }

    private void addError(String imageName, int translationId) {
        add(new ErrorPane(translationId, imageName));
    }

    private JPanel getHintPanel(int translationId) {
        JPanel hint = new JPanel();
        hint.add(new JLabel("<html><p width=\"250\">" + Config.translation.get(translationId) + "</p></html>"));
        return hint;
    }

    private JPanel getHintPanel(String msg) {
        JPanel hint = new JPanel();
        hint.add(new JLabel("<html><p width=\"250\">" + msg + "</p></html>"));
        return hint;
    }

    private void setupWindow() {
        CardManager manager = CardManager.getInstance();
        CardInstance card = manager.getCard();

        switch (card.getState()) {
            case OK:
                break;
            case LOCKED:
                add(new ErrorPane(174, "announcement.png", getHintPanel(175), this));
                return;
            case UNAUTHORIZED:
                add(new ErrorPane(180, "announcement.png", getHintPanel(181), this));
                return;
            case FAILED:
                add(new ErrorPane(182, "announcement.png", getHintPanel(Config.translation.get(183) + card.error), this));
                return;
        }

        Integer isdLifeState = manager.getCardLifeCycle();
        if (isdLifeState == null) {
            add(new ErrorPane(180, "announcement.png", getHintPanel(181), this));
            return;
        }
        switch (isdLifeState) {
            case 0x1:
                break;
            case 0x7:
                add(new ErrorPane(170, "announcement.png", getHintPanel(171), this));
                return;
            case 0xF:
                add(new ErrorPane(172, "announcement.png", getHintPanel(173), this));
                return;
            case 0x7F:
                add(new ErrorPane(174, "announcement.png", getHintPanel(175), this));
                return;
            case 0xFF:
                add(new ErrorPane(176, "announcement.png", getHintPanel(177), this));
                return;
            default:
                add(new ErrorPane(178, "announcement.png", getHintPanel(179), this));
                return;
        }


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

        //todo use this maybe?
//        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
//                windowLayout, infoLayout);
//        splitPane.setOpaque(false);
//        splitPane.setOneTouchExpandable(true);
//        splitPane.setDividerLocation(150);

        setLayout(new BorderLayout());

        add(infoLayout, BorderLayout.EAST);
        add(windowScroll, BorderLayout.CENTER);

        infoLayout.setBackground(Color.WHITE);
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
        return true;
    }

    private void showPanel(Collection<LocalItem> sortedItems) {
        windowLayout.removeAll();
        if (sortedItems.size() == 0) {
            windowLayout.add(new LocalItem(Config.translation.get(113), "no_results.png", "", "", null));
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
        infoLayout.setVisible(CardManager.getInstance().isSelected());
        super.paintComponent(g);
    }
}
