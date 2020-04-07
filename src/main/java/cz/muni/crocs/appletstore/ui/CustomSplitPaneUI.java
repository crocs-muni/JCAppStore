package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;

/**
 * Custom split pane UI divider design
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CustomSplitPaneUI extends BasicSplitPaneUI {

    @Override
    public BasicSplitPaneDivider createDefaultDivider() {
        CustomSplitPaneDivider divider = new CustomSplitPaneDivider(this);
        divider.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        return divider;
    }
}
