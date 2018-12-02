package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.ErrorPane;

import javax.swing.JPanel;
import java.awt.GridBagLayout;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalWindowPane extends JPanel {

    private AppletStore context;
    private JPanel currentPanel = null;

    public LocalWindowPane(AppletStore context) {
        this.context = context;
        setLayout(new GridBagLayout());
        setOpaque(false);
        init();
    }

    public void init() {
        removeAll();
        revalidate();
        //if (currentPanel != null) remove(currentPanel);

        if (context.terminals.isFound()) {
            if (context.terminals.isCard()) {
                //terminal and card found
                setupWindow();
            } else {
                //card not in terminal
                addError("no-card.png", 5);
            }
        } else {
            addError("no-reader.png", 2);
        }
    }

    private void addError(String imageName, int translationId) {
        add(new ErrorPane(translationId, imageName));
    }

    private void setupWindow() {
//        currentPanel = new LoadingPane();
//        add(currentPanel);
    }
}
