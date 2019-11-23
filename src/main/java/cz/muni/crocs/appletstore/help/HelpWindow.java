package cz.muni.crocs.appletstore.help;

import javax.swing.*;
import java.awt.*;

public class HelpWindow extends JDialog {

   public HelpWindow(String title, Container component) {
       setTitle(title);
       setModal(true);
       component.setBackground(Color.WHITE);
       setPreferredSize(new Dimension(850, 550));

       setContentPane(new JScrollPane(component));
       pack();
   }

   public void showIt() {
       setVisible(true);
   }
}
