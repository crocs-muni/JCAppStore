package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import java.awt.*;

public class Scroll extends JScrollPane {

    public Scroll(Component component) {
        setViewportView(component);
    }
}
