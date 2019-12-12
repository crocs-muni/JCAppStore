package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.ui.LeftMenuButton;
import cz.muni.crocs.appletstore.ui.InputHintTextField;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private JPanel container = new JPanel(new GridBagLayout());

    private InputHintTextField searchInput;
    private JLabel searchIcon;
    private boolean close = false;

    private ImageIcon searchImage = new ImageIcon(Config.IMAGE_DIR + "search.png");
    private ImageIcon closeImage = new ImageIcon(Config.IMAGE_DIR + "close_black.png");

    private LeftMenuButton local;
    private LeftMenuButton remote;
    private boolean isLocal = true;

    private MainPanel parent;

    public LeftMenu(MainPanel parent) {
        this.parent = parent;

        setOpaque(false);
        setBackground(new Color(255, 255, 255, 65));
        container.setOpaque(false);

        setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
        setLayout(new BorderLayout());
        buildMenuComponents();
        setListeners();
    }

    public void buildMenuComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.PAGE_START;

        container.add(buildSearchPane(), gbc);

        local = new LeftMenuButton("creditcard.png", textSrc.getString("my_card"), false);
        remote = new LeftMenuButton("shop.png", textSrc.getString("app_store"), false);
        container.add(local, gbc);
        container.add(remote, gbc);
        setChoosed();

        add(container, BorderLayout.NORTH);
    }

    private JPanel buildSearchPane() {
        JPanel searchPane = new JPanel();
        searchPane.setLayout(new FlowLayout());
        searchPane.setOpaque(false);
        searchPane.setBorder(new CompoundBorder(
                new EmptyBorder(5, 15, 15, 15), //outer margin
                new MatteBorder(0, 0, 5, 0, Color.BLACK))); //inner nice bottom line
        searchPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        searchPane.setOpaque(false); //transparent ?? or color

        searchInput = new InputHintTextField(textSrc.getString("search"));
        searchInput.setHorizontalAlignment(SwingConstants.LEFT);
        searchInput.setFont(OptionsFactory.getOptions().getFont());
        searchInput.setPreferredSize(new Dimension(160, 30));
        searchPane.add(searchInput);

        searchIcon = new JLabel(searchImage);
        searchIcon.setBorder(new EmptyBorder(5, 5, 5, 5));
        searchIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchPane.add(searchIcon);
        return searchPane;
    }

    /**
     * Sets the "choosed button" border
     */
    private void setChoosed() {
        local.setSelectedBorder(isLocal);
        remote.setSelectedBorder(!isLocal);
        local.setSelectedBackground(isLocal);
        remote.setSelectedBackground(!isLocal);
    }

    private void resetSearch() {
        close = false;
        searchIcon.setIcon(searchImage);
        searchInput.setText("");
    }

    private void checkIfSetClose() {
        boolean hasSearchText = !searchInput.getText().isEmpty();
        if (close != hasSearchText) {
            searchIcon.setIcon(hasSearchText ? closeImage : searchImage);
        }
        close = hasSearchText;
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
                    parent.getSearchablePane().refresh();
                } else {
                    parent.getSearchablePane().showItems(null);
                }
            }
        });

        //searching icon on click search
        searchIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                resetSearch();
                parent.getSearchablePane().showItems(null);
            }
        });
        //searching on enter press
        searchInput.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.getSearchablePane().showItems(searchInput.getText());
                checkIfSetClose();
            }
        });

        searchInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                parent.getSearchablePane().showItems(searchInput.getText());
                checkIfSetClose();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                parent.getSearchablePane().showItems(searchInput.getText());
                checkIfSetClose();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        g.setColor( getBackground() );
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }
}

