package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.ErrorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JPanel;
import java.awt.GridBagLayout;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalWindowPane extends JPanel {

    private AppletStore context;
    private JPanel currentPanel = null;
    private static final Logger logger = LogManager.getLogger(LocalWindowPane.class);

    public LocalWindowPane(AppletStore context) {
        this.context = context;
        setLayout(new GridBagLayout());
        setOpaque(false);
    }

    public void updatePanes(Terminals.TerminalState state) {
        removeAll();
        revalidate();
        System.out.println(state);
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
            default:
        }
    }

    private void addError(String imageName, int translationId) {
        add(new ErrorPane(translationId, imageName));
    }

    private void setupWindow() {

    }
}
