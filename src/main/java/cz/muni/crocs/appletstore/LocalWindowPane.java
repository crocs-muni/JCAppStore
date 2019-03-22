package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.CardInstance;
import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.Terminals;
import cz.muni.crocs.appletstore.iface.Searchable;
import cz.muni.crocs.appletstore.ui.CapFileView;
import cz.muni.crocs.appletstore.ui.CustomFlowLayout;
import cz.muni.crocs.appletstore.ui.CustomScrollBarUI;
import cz.muni.crocs.appletstore.ui.DisablePanel;
import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.ui.LoadingPaneCircle;
import cz.muni.crocs.appletstore.ui.Warning;
import cz.muni.crocs.appletstore.card.AppletInfo;

import cz.muni.crocs.appletstore.util.Informer;
import cz.muni.crocs.appletstore.util.Sources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pro.javacard.CAPFile;

import javax.smartcardio.CardException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;


/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalWindowPane extends DisablePanel implements Searchable {

    private static final Logger logger = LogManager.getLogger(LocalWindowPane.class);

    private AppletStore context;
    private JToolBar tools = new JToolBar();
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

        installCmd.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                CAPFile file = getCapFile();
                if (file == null) return;

                InstallDialogWindow opts = new InstallDialogWindow(file);

                int result = JOptionPane.showOptionDialog(context,
                        opts,
                        Sources.language.get("CAP_install_applet"),
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        new ImageIcon(Config.IMAGE_DIR + "error.png"),
                        new String[]{Sources.language.get("install"), Sources.language.get("cancel")}, "error");
                switch (result) {
                    case JOptionPane.YES_OPTION:
                        if (!opts.validAID() || !opts.validInstallParams()) {
                            Informer.getInstance().showInfo("E_install_invalid_data");
                            return;
                        }
                        break;
                    case JOptionPane.NO_OPTION:
                    case JOptionPane.CLOSED_OPTION:
                        return;
                }

                setEnabledAll(false);
                new Thread(() -> {
                    try {
                        String[] additionalInfo = opts.getAdditionalInfo();
                        Sources.manager.install(file, additionalInfo);
                    } catch (CardException e1) {
                        //todo notify and log error
                        e1.printStackTrace();
                    }
               }).start();
            }
        });

        updatePanes(Sources.manager.getTerminalState());
    }

    private CAPFile getCapFile() {
        JFileChooser fileChooser = new JFileChooser(Config.APP_LOCAL_DIR);
        fileChooser.setFileView(new CapFileView());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(Sources.language.get("cap_files"), "cap"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File cap = fileChooser.getSelectedFile();
            if (!cap.exists()) {
                Informer.getInstance().showInfo(Sources.language.get("E_install_no_file_1") + cap.getAbsolutePath() + Sources.language.get("E_install_no_file_2"));
                return null;
            }

            CAPFile instcap = null;
            try (FileInputStream fin = new FileInputStream(cap)) {
                instcap = CAPFile.fromStream(fin);
            } catch (IOException e) {
                Informer.getInstance().showInfo(Sources.language.get("E_install_no_file_1") + cap.getAbsolutePath() + Sources.language.get("E_install_no_file_1´2"));
            }
            return instcap;
        }
        return null;
    }


    public void updatePanes(Terminals.TerminalState state) {
        removeAll();
        revalidate();
        System.out.println("updated:" + state);
        switch (state) {
            case OK:
                setupWindow();
                break;
            case NO_CARD:
                addError("no-card.png", "no_card");
                break;
            case NO_READER:
                addError("no-reader.png", "no_reader");
                break;
            case LOADING:
                add(new LoadingPaneCircle());
                break;
            default:
        }
    }

    private void addError(String imageName, String titleKey) {
        add(new ErrorPane(Sources.language.get(titleKey), imageName));
    }

    private JPanel getPanelByKey(String hintKey) {
        JPanel hint = new JPanel();
        hint.add(new JLabel("<html><p width=\"250\">" + Sources.language.get(hintKey) + "</p></html>"));
        return hint;
    }

    private JPanel getPanel(String msg) {
        JPanel hint = new JPanel();
        hint.add(new JLabel("<html><p width=\"250\">" + msg + "</p></html>"));
        return hint;
    }

    void setupWindow() {
        CardManager manager = Sources.manager;
        CardInstance card = manager.getCard();

        switch (card.getState()) {
            case OK:
                setEnabledAll(true);
                break;
            case WORKING:
                setEnabledAll(false);
                return; //do not update, just stuck the screen
            case FAILED:
                if (items.isEmpty())
                    add(new ErrorPane(Sources.language.get("E_communication"), manager.getErrorCause(), "announcement_white.png"));
                else
                    Informer.getInstance().showWarningToClose(manager.getErrorCause(), Warning.Importance.SEVERE);
                return;
            default: //continue, probably card locked
        }

        Integer isdLifeState = manager.getCardLifeCycle();
        if (isdLifeState == null) {
            add(new ErrorPane(Sources.language.get("E_authentication"), Sources.language.get("H_authentication"), "announcement_white.png"));
            return;
        }
        switch (isdLifeState) {
            case 0x1:
                break;
            case 0x7:
                add(new ErrorPane(Sources.language.get("E_initialized"), Sources.language.get("H_initialized"), "announcement_white.png"));
                return;
            case 0xF:
                add(new ErrorPane(Sources.language.get("E_secure_state"), Sources.language.get("H_secure_state"), "announcement_white.png"));
                return;
            case 0x7F:
                add(new ErrorPane(Sources.language.get("E_locked"), Sources.language.get("H_locked"), "announcement_white.png"));
                return;
            case 0xFF:
                add(new ErrorPane(Sources.language.get("E_terminated"), Sources.language.get("H_terminated"), "announcement_white.png"));
                return;
            default:
                add(new ErrorPane(Sources.language.get("E_no_life_state"), Sources.language.get("H_no_life_state"), "announcement_white.png"));
                return;
        }

        try {
            List<AppletInfo> cardApplets = card.getApplets();
            if (cardApplets == null) {
                updatePanes(Terminals.TerminalState.LOADING);
                return;
            } else
                loadApplets(card.getApplets(), manager);
        } catch (IOException e) {
            //todo handle
            e.printStackTrace();
        }

//        setLayout(new BorderLayout());
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridy = 0;
        add(windowScroll, constraints);
        constraints.gridx = 1;

        infoLayout.setVisible(false);
        add(infoLayout, constraints);

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
            windowLayout.add(new LocalItem(Sources.language.get("no_results"), "no_results.png", "", "", null));
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
}
