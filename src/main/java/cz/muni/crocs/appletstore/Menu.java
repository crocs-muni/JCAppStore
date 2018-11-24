package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.ui.CustomJmenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Menu extends JMenuBar implements ActionListener, ItemListener {

    private JMenu submenu;
    private AppletStore context;

    private JMenu readers;
    private ButtonGroup readersPresent = new ButtonGroup();

    public Menu(AppletStore parent) {
        context = parent;

        setBackground(new Color(0, 0, 0));
        setMargin(new Insets(10, 100, 5, 5));
        buildMenu();


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
        menuItem.setMargin(new Insets(4, 4, 4, 4));
        return menuItem;
    }

    private void setItemLook(Component component, String descripton) {
        component.setForeground(new Color(0x000000));
        component.setBackground(new Color(0xffffff));
        component.setFont(CustomFont.plain);
        component.getAccessibleContext().setAccessibleDescription(descripton);
    }

    private JRadioButtonMenuItem selectableMenuItem(String title, String descripton) {
        JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(title);
        setItemLook(rbMenuItem, descripton);
        rbMenuItem.setMargin(new Insets(4, 4, 4, 4));
        return rbMenuItem;
    }


    //TODO menu into func and return menu
    private void buildMenu() {
        JMenuItem item;
//Create the menu bar.
//Build the first menu.
        CustomJmenu menu = new CustomJmenu("A menu", "Description", KeyEvent.VK_A);
        add(menu);
        //a group of JMenuItems
//        menu.add(menuItemWithKeyShortcut("First", "Description", KeyEvent.VK_1, InputEvent.ALT_MASK));
//        menu.add(menuItemNoShortcut("Second", "Description"));
//        menu.add(menuItemNoShortcut("Third", "Description"));
        //a group of radio button menu items
        menu.addSeparator();

//
//
//        rbMenuItem = new JRadioButtonMenuItem("Another one");
//        rbMenuItem.setMnemonic(KeyEvent.VK_O);
//        group.add(rbMenuItem);
//        menu.add(rbMenuItem);
//
////a group of check box menu items
//        menu.addSeparator();
//        cbMenuItem = new JCheckBoxMenuItem("A check box menu item");
//        cbMenuItem.setMnemonic(KeyEvent.VK_C);
//        menu.add(cbMenuItem);
//
//        cbMenuItem = new JCheckBoxMenuItem("Another one");
//        cbMenuItem.setMnemonic(KeyEvent.VK_H);
//        menu.add(cbMenuItem);

//a submenu

        submenu = new JMenu("A submenu");
        submenu.setMnemonic(KeyEvent.VK_S);

//        submenu.add(menuItemWithKeyShortcut("Item submenu", "Description",
//                KeyEvent.VK_2, InputEvent.ALT_MASK));


//Build second menu in the menu bar.
        add(new CustomJmenu("Another Menu", "This menu does nothing", KeyEvent.VK_N));

        //BUILD READERS MENU
        readers = new CustomJmenu(Config.translation.get(90), "", KeyEvent.VK_R);

        resetTerminalButtonGroup(); //possible to call multiple times in order to refresh readers in a menu
        add(readers);

//right shift
        add(Box.createGlue());

        menu = new CustomJmenu("", "Minimize", KeyEvent.VK_UNDEFINED);
        menu.setIcon(new ImageIcon("src/main/resources/img/minimize.png"));
        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    context.setState(JFrame.ICONIFIED);
                }
            }
        });
        menu.setHorizontalAlignment(SwingConstants.RIGHT);
        add(menu);

        menu = new CustomJmenu("", "Exit", KeyEvent.VK_UNDEFINED);
        menu.setIcon(new ImageIcon("src/main/resources/img/close_white.png"));

        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    System.exit(0);
                }
            }
        });
        menu.setHorizontalAlignment(SwingConstants.RIGHT);
        add(menu);
    }


    public void resetTerminalButtonGroup() {
        readers.removeAll(); //TODO dont recreate refresh button
        JMenuItem refreshItem = menuItemWithKeyShortcut(refreshReaders(), Config.translation.get(92), KeyEvent.VK_R, InputEvent.ALT_MASK);
        refreshItem.setIcon(new ImageIcon(Config.IMAGE_DIR + "sync.png"));
        readers.add(refreshItem);
        readers.addSeparator();
        if (context.terminals.isFound()) {
            readersPresent = new ButtonGroup();
            for (String name : context.terminals.getTerminals().keySet()) {
                //todo set selected
                JRadioButtonMenuItem item = selectableMenuItem(name, Config.translation.get(56));
                readersPresent.add(item);
                readers.add(item);
            }
            readersPresent.setSelected(((JRadioButtonMenuItem)readers.getMenuComponent(2)).getModel(), true);
        } else {
            JMenuItem item = menuItemDisabled(Config.translation.get(2), "");
            item.setIcon(new ImageIcon(Config.IMAGE_DIR + "no-reader-small.png"));
            item.setEnabled(false);
            readers.add(item);
        }
    }


    private AbstractAction refreshReaders() {
        return new AbstractAction(Config.translation.get(91)) {
            @Override
            public void actionPerformed(ActionEvent e) {
                context.refresh(true);
            }
        };
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        //TODO
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        //TODO
    }
}
