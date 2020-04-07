package cz.muni.crocs.appletstore.help;

import javax.swing.*;
import java.awt.*;

/**
 * Window to display the help in
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class HelpWindow extends JDialog {

    /**
     * Create help window
     * @param title window title
     * @param component component to embed in
     */
   public HelpWindow(String title, Container component) {
       setTitle(title);
       setModal(true);
       component.setBackground(Color.WHITE);
       setPreferredSize(new Dimension(850, 550));

       setContentPane(new JScrollPane(component));
       pack();
   }

    /**
     * Show help window
     */
   public void showIt() {
       setVisible(true);
   }
}
