package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.card.Terminals;
import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.ui.CustomJmenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Menu extends JMenuBar implements ActionListener, ItemListener {

    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private AppletStore context;
    private JMenu readers;
    private JLabel currentCard;

    public Menu(AppletStore parent) {
        context = parent;
        setBackground(new Color(0, 0, 0));
        setMargin(new Insets(10, 100, 5, 5));
        setBorder(null);

        buildMenu();

        //set action events
//TODO: for each menu
//        for (int pos = 0; pos < menu.getItemCount(); ++pos) {
//            menu.getItem(pos).addActionListener(this);
//        }

//        //for each JRadioButtonMenuItem:
//        rbMenuItem.addActionListener(this);
//        ...
//        //for each JCheckBoxMenuItem:
//        cbMenuItem.addItemListener(this);
    }

    //TODO menu into func and return menu
    private void buildMenu() {
        CustomJmenu menu = new CustomJmenu(textSrc.getString("file"), "", KeyEvent.VK_A);
        add(menu);

        menu.add(menuItemWithKeyShortcutAndIcon(new AbstractAction(textSrc.getString("settings")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Settings settings = new Settings(context);
                Object[] options = { textSrc.getString("ok"), textSrc.getString("cancel") };
                int result = JOptionPane.showOptionDialog(null, settings, textSrc.getString("settings"),
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                        null, options, null);
                if (result == JOptionPane.YES_OPTION){
                    settings.apply();
                }
            }
        }, Config.IMAGE_DIR + "settings.png", "", KeyEvent.VK_S, InputEvent.ALT_MASK));

        menu.add(menuItemNoShortcut(new AbstractAction(textSrc.getString("quit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        }, Config.IMAGE_DIR + "close_black.png"));

        readers = new CustomJmenu(textSrc.getString("readers"), "", KeyEvent.VK_R);
        add(readers);

        JPanel midContainer = new JPanel();
        midContainer.setBackground(Color.black);
        midContainer.add(new JLabel(new ImageIcon(Config.IMAGE_DIR + "creditcard-white.png")));
        currentCard = new JLabel();
        currentCard.setForeground(Color.white);
        midContainer.add(currentCard);
        add(midContainer);
    }

    public void setCard(String card) {
        if (card == null || card.isEmpty())
            card = textSrc.getString("no_card");
        currentCard.setText(card);
        revalidate();
    }

    public void resetTerminalButtonGroup() {
        CardManager manager = CardManagerFactory.getManager();
        readers.removeAll();
        if (manager.getTerminalState() != Terminals.TerminalState.NO_READER) {
            ButtonGroup readersPresent = new ButtonGroup();
            for (String name : manager.getTerminals()) {
                JRadioButtonMenuItem item = selectableMenuItem(name, textSrc.getString("reader_avail"));
                if (name.equals(manager.getSelectedTerminalName())) {
                    item.setSelected(true);
                }
                item.addActionListener(selectReaderListener());
                readersPresent.add(item);
                readers.add(item);
            }
            readers.repaint();

        } else {
            JMenuItem item = menuItemDisabled(textSrc.getString("no_reader"), "");
            item.setIcon(new ImageIcon(Config.IMAGE_DIR + "no-reader-small.png"));
            item.setEnabled(false);
            readers.add(item);
        }
    }

    private ActionListener selectReaderListener() {
        return e -> CardManagerFactory.getManager().setSelectedTerminal(e.getActionCommand());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //TODO
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        //TODO
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

    @Override
    public boolean isBorderPainted() {
        return false;
    }


    /**
     * @param action         action to perform
     * @param keyEvent       KeyEvent key constant
     * @param inputEventMask InputEvent constant - mask for accelerated access
     * @return
     */
    private JMenuItem menuItemWithKeyShortcutAndIcon(AbstractAction action, String imagePath,
                                                     String descripton, int keyEvent, int inputEventMask) {
        JMenuItem menuItem = menuItemWithKeyShortcut(action, descripton, keyEvent,inputEventMask);
        menuItem.setIcon(new ImageIcon(imagePath));

        return menuItem;
    }

    /**
     * @param action         action to perform
     * @param keyEvent       KeyEvent key constant
     * @param inputEventMask InputEvent constant - mask for accelerated access
     * @return
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
        JMenuItem menuItem = new JMenuItem(action);
        setItemLook(menuItem, descripton);
        return menuItem;
    }

    private void setItemLook(AbstractButton component, String descripton) {
        component.setForeground(new Color(0x000000));
        component.setBackground(new Color(0xffffff));
        component.setFont(OptionsFactory.getOptions().getDefaultFont().deriveFont(10f));
        component.getAccessibleContext().setAccessibleDescription(descripton);
        component.setMargin(new Insets(4, 4, 4, 16));
        Dimension preferred = component.getPreferredSize();
        component.setPreferredSize(new Dimension(200, (int)preferred.getHeight()));
    }

    private JRadioButtonMenuItem selectableMenuItem(String title, String descripton) {
        JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(title);
        setItemLook(rbMenuItem, descripton);
        return rbMenuItem;
    }

}
