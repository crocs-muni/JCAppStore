package cz.muni.crocs.appletstore.util;

import javax.swing.text.Document;

/**
 * Defines required subset of JTextArea functions required by logger console,
 * e.g. to implement this, simply add extends JTextArea
 */
public interface LoggerConsole {

    /**
     * Return number of lines of the console
     * @return number of lines
     */
    int getLineCount();

    /**
     * Get the console text
     * @return text displayed on the console
     */
    String getText();

    /**
     * Set text to the console
     * @param text text to set
     */
    void setText(String text);

    /**
     * Append text to the console
     * @param text to append
     */
    void append(String text);

    /**
     * Get document
     * @return swing Document instance of the console
     */
    Document getDocument();
}
