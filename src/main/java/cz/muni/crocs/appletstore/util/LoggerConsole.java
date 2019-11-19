package cz.muni.crocs.appletstore.util;

import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.text.Document;

/**
 * Defines required subset of JTextArea functions required by logger console,
 * e.g. to implement this, simply add extends JTextArea
 */
public interface LoggerConsole {
    int getLineCount();
    String getText();
    void setText(String text);
    void append(String text);
    Document getDocument();
}
