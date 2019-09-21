package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.Warning;
import cz.muni.crocs.appletstore.util.Informer;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.ui.CustomButton;
import cz.muni.crocs.appletstore.ui.InputHintTextField;
import cz.muni.crocs.appletstore.ui.NotifLabel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Menu for switching between store & local panel
 * and to display info in left pane
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LeftMenu extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private JPanel container;
    private InputHintTextField searchInput;
    private JLabel searchIcon;

    private CustomButton local = new CustomButton("creditcard.png");
    private CustomButton remote = new CustomButton("shop.png");

    private boolean isLocal = true;
    private MainPanel parent;
    private Color choosedButtonBG = new Color(255, 255, 255, 60);

    public LeftMenu(MainPanel parent) {
        this.parent = parent;
        setBackground(new Color(255, 255, 255, 65));

        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        setMaximumSize(new Dimension(240, Integer.MAX_VALUE));
        setMinimumSize(new Dimension(240, 0));
        setPreferredSize(new Dimension(240, parent.getHeight()));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        buildMenuComponents();
        setListeners();
    }

    /**
     * Build Swing components
     */
    private void buildMenuComponents() {
        JPanel searchPane = new JPanel();
        searchPane.setOpaque(false);
        searchPane.setBorder(new CompoundBorder(
                new EmptyBorder(5, 15, 15, 15),
                new MatteBorder(0, 0,5 ,0, Color.BLACK)));
        searchPane.setMaximumSize( new Dimension(Integer.MAX_VALUE, 60));
        searchInput = new InputHintTextField(textSrc.getString("search"));
        searchInput.setHorizontalAlignment(SwingConstants.LEFT);
        searchInput.setFont(OptionsFactory.getOptions().getDefaultFont());
        searchInput.setPreferredSize(new Dimension(160, 30));
        searchPane.add(searchInput);

        searchIcon = new JLabel(new ImageIcon(Config.IMAGE_DIR +  "search.png"));
        searchIcon.setBorder(new EmptyBorder(5, 5, 5,5 ));
        searchIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchPane.add(searchIcon);
        container.add(searchPane);

        setButton(local, textSrc.getString("my_card"), true);
        local.setBackground(choosedButtonBG);
        local.setCursor(new Cursor(Cursor.HAND_CURSOR));
        container.add(local);

        setButton(remote, textSrc.getString("app_store"), false);
        remote.setOpaque(false);
        remote.setCursor(new Cursor(Cursor.HAND_CURSOR));
        container.add(remote);

        container.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(container);
        add(Box.createRigidArea(new Dimension(200, 20)));
    }

    /**
     * Display notification to the user below the buttons
     * @param msg msg to display
     */
    public void addNotification(String msg) {
        NotifLabel label = new NotifLabel(msg, this);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(label, 2);

        if (getComponentCount() > 25)
            remove(getComponent(getComponentCount() - 1));
        revalidate();
    }

    /**
     * Sets the "choosed button" border
     */
    private void setChoosed() {
        local.setBorder(isLocal);
        remote.setBorder(!isLocal);
    }

    /**
     * Set button properties
     * @param button CustomButton instance
     * @param text button title
     * @param defaultChoosed whether the button is choosed by default
     */
    private void setButton(CustomButton button, String text, boolean defaultChoosed) {
        button.setText(text);
        button.setBorder(defaultChoosed);
    }

    /**
     * Setup actions for the buttons
     */
    private void setListeners() {
        local.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isLocal) {
                    isLocal = true;
                    setChoosed();
                    parent.setLocalPanelVisible();

                    local.setOpaque(true);
                    local.setBackground(choosedButtonBG);
                    remote.setOpaque(false);
                }
            }
        });
        remote.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isLocal) {
                    isLocal = false;
                    setChoosed();
                    parent.setStorePaneVisible();

                    remote.setOpaque(true);
                    remote.setBackground(choosedButtonBG);
                    local.setOpaque(false);
                } else {
                    parent.getSearchablePane().showItems(null);
                }
            }
        });

        //searching icon on click search
        searchIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parent.getSearchablePane().showItems(searchInput.getText());
            }
        });
        //searching on enter press
        searchInput.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.getSearchablePane().showItems(searchInput.getText());
            }
        });
    }
}
