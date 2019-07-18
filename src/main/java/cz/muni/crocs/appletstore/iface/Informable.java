package cz.muni.crocs.appletstore.iface;

import javax.swing.*;

public interface Informable {

    void showWarning(JComponent component);

    void hideWarning(JComponent component);

    void showInfo(String info);
}
