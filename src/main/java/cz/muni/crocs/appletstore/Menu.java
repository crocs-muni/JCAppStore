package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.help.*;
import cz.muni.crocs.appletstore.ui.*;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;

/**
 * Main Menu Bar
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class Menu extends JMenuBar {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private AppletStore context;
    private int lastNumOfReadersConnected = 0;
    private CustomNotifiableJmenu readers;
    private JLabel currentCard;

    public Menu(AppletStore parent) {
        context = parent;
        setBackground(new Color(0, 0, 0));
        setMargin(new Insets(10, 100, 5, 5));
        setBorder(null);
        buildMenu();
    }

    /**
     * Set new name of the card inserted in the application bar
     *
     * @param card       custom card name provided by user OR obtained from database when inserted
     * @param identifier card identifier, null or empty string if no card present
     */
    public void setCard(String card, String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            card = textSrc.getString("no_card");
        } else {
            card += (card != null && !card.isEmpty()) ?
                    " <font color='#a3a3a3'>[" + identifier + "]</font>" : identifier;
        }
        currentCard.setText(card);
        revalidate();
        repaint();
    }

    /**
     * Reset if new card readers found
     */
    public void resetTerminalButtonGroup() {
        CardManager manager = CardManagerFactory.getManager();
        readers.removeAll();

        if (manager.getTerminalState() != Terminals.TerminalState.NO_READER) {
            ButtonGroup readersPresent = new ButtonGroup();
            int readersFound = 0;
            for (String name : manager.getTerminals()) {
                JRadioButtonMenuItem item = selectableMenuItem(name, textSrc.getString("reader_avail"));
                if (name.equals(manager.getSelectedTerminalName())) {
                    item.setSelected(true);
                }
                item.addActionListener(selectReaderListener());
                readersPresent.add(item);
                readers.add(item);
                readersFound++;
            }
            if (lastNumOfReadersConnected < readersFound) {
                readers.setNotify(true);
            }
            lastNumOfReadersConnected = readersFound;
        } else {
            JMenuItem item = menuItemDisabled(textSrc.getString("no_reader"), "");
            item.setIcon(new ImageIcon(Config.IMAGE_DIR + "no-reader-small.png"));
            item.setEnabled(false);
            lastNumOfReadersConnected = 0;
            readers.add(item);
            readers.setNotify(false);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public boolean isBorderPainted() {
        return false;
    }

    private void buildMenu() {
        buildFileItem();
        buildReadersItem();
        buildHelpItem();
    }

    private ActionListener selectReaderListener() {
        return e -> CardManagerFactory.getManager().setSelectedTerminal(e.getActionCommand());
    }

    private void buildFileItem() {
        CustomJmenu menu = new CustomNotifiableJmenu(textSrc.getString("file"), "", KeyEvent.VK_A);

        menu.add(buildCardSubMenu());

        menu.add(buildWindowSettingsSubMenu());

        menu.add(buildModesMenu());

        menu.add(menuItemWithKeyShortcutAndIcon(new AbstractAction(textSrc.getString("settings")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Settings settings = new Settings(context);
                Object[] options = {textSrc.getString("ok"), textSrc.getString("cancel")};
                int result = JOptionPane.showOptionDialog(null, settings, textSrc.getString("settings"),
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                        null, options, null);
                if (result == JOptionPane.YES_OPTION) {
                    settings.apply();
                }
            }
        }, Config.IMAGE_DIR + "settings.png", "", KeyEvent.VK_S, InputEvent.ALT_MASK));

        menu.add(menuItemNoShortcut(new AbstractAction(textSrc.getString("quit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        }, textSrc.getString("H_quit"), Config.IMAGE_DIR + "close_black.png"));

        add(menu);
    }

    private JMenuItem buildCardSubMenu() {
        JMenu submenu = jmenuWithBackground(textSrc.getString("card"));
        setItemLook(submenu, textSrc.getString("card_desc"));

        submenu.add(menuItemWithKeyShortcutAndIcon(new AbstractAction(textSrc.getString("get_memory")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        new CardInfoPanel(context, context.getWindow().getRefreshablePane()), textSrc.getString("card_info"),
                        JOptionPane.PLAIN_MESSAGE, new ImageIcon(Config.IMAGE_DIR + "info.png"));
            }
        }, Config.IMAGE_DIR + "memory.png", "", KeyEvent.VK_I, InputEvent.ALT_MASK));

        submenu.add(menuItemWithKeyShortcutAndIcon(new AbstractAction(textSrc.getString("test_card")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        new CardTestPanel(context, context.getWindow().getRefreshablePane()), textSrc.getString("card_info"),
                        JOptionPane.PLAIN_MESSAGE, new ImageIcon(Config.IMAGE_DIR + "info.png"));
            }
        }, Config.IMAGE_DIR + "test.png", "", KeyEvent.VK_T, InputEvent.ALT_MASK));

        return submenu;
    }

    private JMenuItem buildWindowSettingsSubMenu() {
        JMenu submenu = jmenuWithBackground(textSrc.getString("display"));
        setItemLook(submenu, textSrc.getString("display_desc"));

        submenu.add(selectableMenuItem(new AbstractAction(textSrc.getString("logger")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                context.getWindow().toggleLogger();
            }
        }, "", KeyEvent.VK_L, InputEvent.ALT_MASK));

        JMenuItem hints = selectableMenuItem(new AbstractAction(textSrc.getString("enable_hints")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsFactory.getOptions().addOption(Options.KEY_HINT,
                        OptionsFactory.getOptions().is(Options.KEY_HINT) ? "false" : "true");
                HintPanel.enableHint(OptionsFactory.getOptions().is(Options.KEY_HINT));
            }
        }, "", KeyEvent.VK_D, InputEvent.ALT_MASK);
        hints.setSelected(OptionsFactory.getOptions().is(Options.KEY_HINT));
        submenu.add(hints);
        return submenu;
    }

    private JMenuItem buildModesMenu() {
        JMenu submenu = jmenuWithBackground(textSrc.getString("modes"));
        setItemLook(submenu, textSrc.getString("modes_desc"));

        JMenuItem verbose = selectableMenuItem(new AbstractAction(textSrc.getString("enable_verbose")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsFactory.getOptions().addOption(Options.KEY_VERBOSE_MODE,
                        OptionsFactory.getOptions().is(Options.KEY_VERBOSE_MODE) ? "false" : "true");
            }
        }, "", KeyEvent.VK_V, InputEvent.ALT_MASK);
        verbose.setSelected(OptionsFactory.getOptions().is(Options.KEY_VERBOSE_MODE));
        submenu.add(verbose);

        JMenuItem intuitive = selectableMenuItem(new AbstractAction(textSrc.getString("enable_simple")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsFactory.getOptions().addOption(Options.KEY_SIMPLE_USE,
                        OptionsFactory.getOptions().is(Options.KEY_SIMPLE_USE) ? "false" : "true");
            }
        }, "", KeyEvent.VK_E, InputEvent.ALT_MASK);
        intuitive.setSelected(OptionsFactory.getOptions().is(Options.KEY_SIMPLE_USE));
        submenu.add(intuitive);

        JMenuItem exclusive = selectableMenuItem(new AbstractAction(textSrc.getString("exclusive_mode")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsFactory.getOptions().addOption(Options.KEY_EXCLUSIVE_CARD_CONNECT,
                        OptionsFactory.getOptions().is(Options.KEY_EXCLUSIVE_CARD_CONNECT) ? "false" : "true");
            }
        }, "", KeyEvent.VK_S, InputEvent.ALT_MASK);
        exclusive.setSelected(OptionsFactory.getOptions().is(Options.KEY_EXCLUSIVE_CARD_CONNECT));
        submenu.add(exclusive);

        JMenuItem jcmemory = selectableMenuItem(new AbstractAction(textSrc.getString("enable_jcmemory")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsFactory.getOptions().addOption(Options.KEY_KEEP_JCMEMORY,
                        OptionsFactory.getOptions().is(Options.KEY_KEEP_JCMEMORY) ? "false" : "true");
            }
        }, "", KeyEvent.VK_J, InputEvent.ALT_MASK);
        jcmemory.setSelected(OptionsFactory.getOptions().is(Options.KEY_KEEP_JCMEMORY));
        submenu.add(jcmemory);

        return submenu;
    }

    private void buildReadersItem() {
        readers = new CustomNotifiableJmenu(textSrc.getString("readers"), "", KeyEvent.VK_R);
        add(readers);
        resetTerminalButtonGroup();

        JPanel midContainer = new JPanel();
        midContainer.setBackground(Color.black);
        midContainer.add(new Text(new ImageIcon(Config.IMAGE_DIR + "creditcard-white.png")));
        currentCard = new HtmlText();
        currentCard.setForeground(Color.white);
        currentCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                CardInstance card = CardManagerFactory.getManager().getCard();
                if (card == null) return;
                String newName = showFormForNewCardName();
                if (newName != null) {
                    try {
                        card.setName(newName);
                    } catch (LocalizedCardException ex) {
                        InformerFactory.getInformer().showInfoToClose("E_save_card_name", Notice.Importance.SEVERE);
                    }
                    setCard(newName, card.getId());
                }
            }
        });
        midContainer.add(currentCard);
        add(midContainer);
    }

    private void buildHelpItem() {
        CustomJmenu help = new CustomJmenu(textSrc.getString("help"), "", KeyEvent.VK_H);
        add(help);

        help.add(menuItemNoShortcut(new AbstractAction(textSrc.getString("applet_usage")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new HelpWindow(textSrc.getString("applet_usage"), HelpFactory.getAppletUsageHelp()).showIt();
            }
        }, textSrc.getString("H_applet_usage")));

        help.add(menuItemNoShortcut(new AbstractAction(textSrc.getString("cmd")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new HelpWindow(textSrc.getString("cmd"), HelpFactory.getCmdHelp()).showIt();
            }
        }, textSrc.getString("H_cmd")));

        help.add(menuItemNoShortcut(new AbstractAction(textSrc.getString("auth")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new HelpWindow(textSrc.getString("auth"), HelpFactory.getMasterKeyHelp()).showIt();
            }
        }, textSrc.getString("H_auth")));

        help.add(menuItemNoShortcut(new AbstractAction(textSrc.getString("ifaq_title")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new HelpWindow(textSrc.getString("ifaq_title"), HelpFactory.getInstallFailuresFAQ()).showIt();
            }
        }, textSrc.getString("ifaq_h")));

        help.add(menuItemNoShortcut(new AbstractAction(textSrc.getString("def_title")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new HelpWindow(textSrc.getString("def_title"), HelpFactory.getMainAppletHelp()).showIt();
            }
        }, textSrc.getString("def_h")));
    }

    /**
     * @param action         action to perform
     * @param keyEvent       KeyEvent key constant
     * @param inputEventMask InputEvent constant - mask for accelerated access
     * @return constructed item
     */
    private JMenuItem menuItemWithKeyShortcutAndIcon(AbstractAction action, String imagePath,
                                                     String descripton, int keyEvent, int inputEventMask) {
        JMenuItem menuItem = menuItemWithKeyShortcut(action, descripton, keyEvent, inputEventMask);
        menuItem.setIcon(new ImageIcon(imagePath));

        return menuItem;
    }

    private JMenu jmenuWithBackground(String title) {
        return new CustomJmenu(title) {
            @Override
            public void paint(Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paint(g);
            }
        };
    }

    /**
     * @param action         action to perform
     * @param keyEvent       KeyEvent key constant
     * @param inputEventMask InputEvent constant - mask for accelerated access
     * @return constructed item
     */
    private JMenuItem menuItemWithKeyShortcut(AbstractAction action, String descripton,
                                              int keyEvent, int inputEventMask) {
        JMenuItem menuItem = menuItemNoShortcut(action, descripton);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEventMask));
        return menuItem;
    }

    private JMenuItem menuItemDisabled(String title, String descripton) {
        JMenuItem menuItem = menuItemNoShortcut(null, descripton);
        menuItem.setText(title);
        return menuItem;
    }

    private JMenuItem menuItemNoShortcut(AbstractAction action, String descripton) {
        JMenuItem menuItem = new CustomJmenuItem(action);
        setItemLook(menuItem, descripton);
        return menuItem;
    }

    private JMenuItem menuItemNoShortcut(AbstractAction action, String descripton, String image) {
        JMenuItem menuItem = new CustomJmenuItem(action);
        setItemLook(menuItem, descripton);
        menuItem.setIcon(new ImageIcon(image));
        return menuItem;
    }

    private void setItemLook(AbstractButton component, String descripton) {
        component.setForeground(new Color(0x000000));
        component.setBackground(new Color(0xffffff));

        component.getAccessibleContext().setAccessibleDescription(descripton);
        component.setMargin(new Insets(4, 4, 4, 16));
        component.setFont(OptionsFactory.getOptions().getTitleFont());
    }

    private JRadioButtonMenuItem selectableMenuItem(String title, String descripton) {
        JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(title) {
            @Override
            public void paint(Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paint(g);
            }
        };
        setItemLook(rbMenuItem, descripton);
        return rbMenuItem;
    }

    private JRadioButtonMenuItem selectableMenuItem(Action action, String description, int keyEvent, int inputEventMask) {
        JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(action) {
            @Override
            public void paint(Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paint(g);
            }
        };
        setItemLook(rbMenuItem, description);
        rbMenuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEventMask));
        return rbMenuItem;
    }

    private String showFormForNewCardName() {
        JTextField field = new JTextField();
        if (JOptionPane.showOptionDialog(this, field, textSrc.getString("ask_for_card_name"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, new ImageIcon(Config.IMAGE_DIR + "creditcard.png"),
                new String[]{textSrc.getString("ok"), textSrc.getString("cancel")}, textSrc.getString("ok")) == JOptionPane.YES_OPTION) {
            return field.getText();
        }
        return null;
    }
}
