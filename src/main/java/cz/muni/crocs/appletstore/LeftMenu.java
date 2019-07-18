package cz.muni.crocs.appletstore;

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
 * @author Jiří Horák
 * @version 1.0
 */
public class LeftMenu extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    //holds the content
    private JPanel container = new JPanel();

    //to perform searching
    private JPanel searchPane;
    private InputHintTextField searchInput;
    private JLabel searchIcon;
    //button for switching
    private CustomButton local = new CustomButton("creditcard.png");
    private CustomButton remote = new CustomButton("shop.png");

    private Color choosedButtonBG = new Color(255, 255, 255, 60);

    private boolean isLocal = true;
    private MainPanel parent;

    public LeftMenu(MainPanel parent) {
        this.parent = parent;
        setBackground(new Color(255, 255, 255, 65));

        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.setOpaque(false);

        setMaximumSize(new Dimension(240, Integer.MAX_VALUE));
        setMinimumSize(new Dimension(240, 0));
        setPreferredSize(new Dimension(240, parent.getHeight()));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        buildMenuComponents();
        setListeners();
    }

    public void buildMenuComponents() {

        searchPane = new JPanel();
        searchPane.setOpaque(false);
        //set margin and size
        searchPane.setBorder(new CompoundBorder(
                new EmptyBorder(5, 15, 15, 15), //outer margin
                new MatteBorder(0, 0,5 ,0, Color.BLACK))); //inner nice bottom line
        searchPane.setMaximumSize( new Dimension(Integer.MAX_VALUE, 60));
        //set search intpu text
        searchInput = new InputHintTextField(textSrc.getString("search"));
        searchInput.setHorizontalAlignment(SwingConstants.LEFT);
        searchInput.setFont(OptionsFactory.getOptions().getDefaultFont());
        searchInput.setPreferredSize(new Dimension(160, 30));
        searchPane.add(searchInput);
        //create search icon
        searchIcon = new JLabel(new ImageIcon(Config.IMAGE_DIR +  "search.png"));
        searchIcon.setBorder(new EmptyBorder(5, 5, 5,5 ));
        searchIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchPane.add(searchIcon);
        //add search to left menu container
        container.add(searchPane);

        //init button for local storage
        setButton(local, textSrc.getString("my_card"), true); //TODO more terminals or cards?
        local.setBackground(choosedButtonBG);
        local.setCursor(new Cursor(Cursor.HAND_CURSOR));
        container.add(local);
        //init button for store
        setButton(remote, textSrc.getString("app_store"), false);
        remote.setOpaque(false);
        remote.setCursor(new Cursor(Cursor.HAND_CURSOR));
        container.add(remote);

        container.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(container);
        add(Box.createRigidArea(new Dimension(200, 20)));
    }

    public void addNotification(String msg) {
        NotifLabel label = new NotifLabel(msg, this);
        //label.setAlignmentY(Component.TOP_ALIGNMENT);
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

    public boolean isLocal() {
        return isLocal;
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

    private void setListeners() {
        //TODO possible call from oher buttons - refreshing appstore window - make it an action
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
                     //init only if no readers
                }
            }
        });
        remote.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isLocal) {
                    isLocal = false;
                    setChoosed();
                    parent.setUpdateStorePaneVisible();

                    remote.setOpaque(true);
                    remote.setBackground(choosedButtonBG);
                    local.setOpaque(false);
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
