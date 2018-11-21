package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.CustomJListFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * @author Jiří Horák
 * @version 1.0
 */
public class OptionsLoader extends JFrame {

    //available translations
    private final String[] langs = new String[] {
            "English", "eng",
            "Česky", "cz"
    };

    OptionsLoader(File file, boolean startAppOnSelect) {

        Config.options.clear();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //center this dialog
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
        this.setSize(50, 30 * (langs.length / 2) + 40 /*bar height*/);


        JPanel panel = (JPanel) this.getContentPane();
        CustomJListFactory list = new CustomJListFactory();
        list.setCellSize(50, 20);
        for (int i = 0; i < langs.length; i += 2) {
            list.add(langs[i], Config.IMAGE_DIR + langs[i+1] + ".jpg");
        }
        JList jList = list.build();
        jList.setBorder(new EmptyBorder(10,10, 10, 10));

        //on click save choosed and run
        jList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int valueIndex = e.getLastIndex();
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(file));
                    writer.write("lang=" + langs[(2*valueIndex+1)] + "\n");
                    writer.close();
                    populateOptions(langs[(2*valueIndex+1)]);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    populateOptions("eng");
                }
                //context.dispose();
                //todo problem cannot get translation while fetching options
                JOptionPane.showMessageDialog(null, "Changes will apply after an restart.");

            }
        });
        panel.add(jList);
    }

    private void populateOptions(String lang) {
        Config.options.put("lang", lang);
    }
}
