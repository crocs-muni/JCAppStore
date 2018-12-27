package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.CustomButton;
import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.ui.InputHintTextField;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LeftMenu extends JPanel {

    //holds the content
    private JPanel container = new JPanel(new GridBagLayout());;
    //enables scrolling
    private JScrollPane scroll;
    //to perform searching
    private JPanel searchPane;
    private InputHintTextField searchInput;
    private JLabel searchIcon;
    //button for switching
    private CustomButton local = new CustomButton("creditcard.png");
    private CustomButton remote = new CustomButton("shop.png");

    private Color choosedButtonBG = new Color(255, 255, 255, 60);

    private boolean isLocal = true;

    private TabbedPaneSimulator parent;

    public LeftMenu(TabbedPaneSimulator parent) {
        this.parent = parent;

        setBackground(new Color(255, 255, 255, 65));
//        container.setBackground(Color.BLUE);
        //setOpaque(false);
        container.setOpaque(false);



        setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
        setLayout(new BorderLayout());
        buildMenuComponents();
        setListeners();
    }

    public void buildMenuComponents() {
        //create panel with search input text window and icon

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.PAGE_START;

        searchPane = new JPanel();
        searchPane.setLayout(new FlowLayout());
        searchPane.setOpaque(false);
        //set margin and size
        searchPane.setBorder(new CompoundBorder(
                new EmptyBorder(5, 15, 15, 15), //outer margin
                new MatteBorder(0, 0,5 ,0, Color.BLACK))); //inner nice bottom line
        searchPane.setMaximumSize( new Dimension(Integer.MAX_VALUE, 60));
        searchPane.setOpaque(false); //transparent ?? or color
        //set search intpu text
        searchInput = new InputHintTextField(Config.translation.get(72));
        searchInput.setHorizontalAlignment(SwingConstants.LEFT);
        searchInput.setFont(CustomFont.plain);
        searchInput.setPreferredSize(new Dimension(160, 30));
        searchPane.add(searchInput);
        //create search icon
        searchIcon = new JLabel(new ImageIcon(Config.IMAGE_DIR +  "search.png"));
        searchIcon.setBorder(new EmptyBorder(5, 5, 5,5 ));
        searchIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchPane.add(searchIcon);
        //add search to left menu container
        container.add(searchPane, gbc);

        //init button for local storage
        setButton(local, Config.translation.get(70), true); //TODO more terminals or cards?
        local.setBackground(choosedButtonBG);
        container.add(local, gbc);
        //init button for store
        setButton(remote, Config.translation.get(71), false);
        remote.setOpaque(false);
        container.add(remote, gbc);

//        scroll = new JScrollPane(container); //TODO scroll modify bars
//        scroll.setOpaque(false);
        add(container, BorderLayout.NORTH);
    }

    /**
     * Sets the "choosed button" border
     */
    private void setChoosed() {
        (local).setBorder(isLocal);
        (remote).setBorder(!isLocal);
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
        //TODO possible call from toher buttons - refreshing appstore window - make it an action
        local.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isLocal) {
                    isLocal = true;
                    setChoosed();
                    parent.setUpdateLocalPaneVisible();

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
        searchIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parent.getSearchablePane().showItems(searchInput.getText());
            }
        });
    }
}
