package cz.muni.crocs.appletstore.help;

import javax.swing.*;
import java.awt.*;

public class HelpWrapper extends JDialog {

   public HelpWrapper(String title, Container component) {
       setTitle(title);
       setModal(true);
       setPreferredSize(new Dimension(850, 550));

       setContentPane(new JScrollPane(component));
       pack();
   }

   public void showIt() {
       setVisible(true);
   }
}
