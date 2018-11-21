package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.CustomButton;
import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.ui.InputHintTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class TabbedPaneSimulator {

    AppletStore context;

    private JPanel parent;
    private JPanel bar;
    //button for switching
    private JButton local = new CustomButton("creditcard.png");
    private JButton remote = new CustomButton("shop.png");
    //to switch panes
    private JPanel content;
    private LocalWindowPane localPanel;
    private JPanel storePanel;
    //to perform searching
    private JPanel searchPane;
    private InputHintTextField searchInput;
    private JLabel searchIcon;
    //of which screen we are currently
    private boolean isLocal = true;

    public TabbedPaneSimulator(AppletStore context) {
        this.context = context;
        createPanes();
        buildComponents();
        setListeners();
    }

    private void createPanes() {
        parent = new JPanel();
        parent.setLayout(new BorderLayout());
        //panel for left menu
        bar = new JPanel(); // { //set background image
//            @Override
//            protected void paintComponent(Graphics g) {
//                super.paintComponent(g);
//                g.drawImage(new BackgroundImage("bg-menu.jpg", this).get(), 0, 0, null);
//            }
//        };
        bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
        bar.setBorder(new EmptyBorder(5,10, 5,10 ));

        //switching between localEnvironment and store panes
        localPanel = new LocalWindowPane(context);
        localPanel.setVisible(true); //screen visible by default
        storePanel = new JPanel();
        storePanel.setOpaque(false);
        storePanel.setVisible(false); //not visible by default
        //container for local and store panes
        content = new JPanel();
        content.setBackground(Color.WHITE); //white background
        content.setLayout(new OverlayLayout(content));
        content.add(localPanel);
        content.add(storePanel);

        parent.add(bar, BorderLayout.WEST);
        parent.add(content, BorderLayout.CENTER);
    }

    /**
     * Sets the "choosed button" border
     */
    private void setChoosed() {
        ((CustomButton)local).setBorder(isLocal);
        ((CustomButton)remote).setBorder(!isLocal);
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

    private JSeparator separator() {
        JSeparator separator = new JSeparator();
        separator.setOpaque(false);
        separator.setOrientation(SwingConstants.HORIZONTAL);
        separator.setMaximumSize( new Dimension(Integer.MAX_VALUE, 10));
        return separator;
    }

    public TabbedPaneSimulator buildComponents() {
        //create panel with search input text window and icon
        searchPane = new JPanel();
        searchPane.setLayout(new FlowLayout());
//        search.setBorder(new CompoundBorder(
//                new EmptyBorder(new Insets(0, 10,0 ,10 )),
//                new MatteBorder(0, 0,5 ,0, Color.BLACK)
//        ));
        //set margin and size
        searchPane.setBorder(new MatteBorder(0, 0,5 ,0, Color.BLACK));
        searchPane.setMaximumSize( new Dimension(Integer.MAX_VALUE, 40));
        searchPane.setOpaque(false);
        //set search intpu text
        searchInput = new InputHintTextField(Config.translation.get(72));
        searchInput.setHorizontalAlignment(SwingConstants.LEFT);
        searchInput.setFont(CustomFont.plain);
        searchInput.setPreferredSize(new Dimension(180, 30));
        searchPane.add(searchInput);
        //create search icon
        searchIcon = new JLabel(new ImageIcon(Config.IMAGE_DIR + "search.png"));
        searchIcon.setBorder(new EmptyBorder(5, 5, 5,5 ));
        searchPane.add(searchIcon);
        //add search to left menu
        bar.add(searchPane);

        bar.add(separator());
        //init button for local storage
        setButton((CustomButton)local, Config.translation.get(70), true); //TODO more terminals or cards?
        bar.add(local);
        //init button for store
        setButton((CustomButton)remote, Config.translation.get(71), false);
        bar.add(remote);
        return this;
    }

    private void setListeners() {
        local.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isLocal) {
                    isLocal = true;
                    setChoosed();
                    localPanel.setVisible(true);
                    storePanel.setVisible(false);
                    //TODO refresh or not?
                    if (!context.terminals.isFound()) {
                        context.terminals.update();
                        localPanel.init();
                    }
                }
            }
        });
        remote.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isLocal) {
                    isLocal = false;
                    setChoosed();
                    localPanel.setVisible(false);
                    storePanel.setVisible(true);
                }
            }
        });
    }

    public JPanel get() {
        return parent;
    }

    public InputHintTextField getSearchInput() {
        return searchInput;
    }

    //** FORM GENERATED CODE IN A BINARY FILE **//
}
