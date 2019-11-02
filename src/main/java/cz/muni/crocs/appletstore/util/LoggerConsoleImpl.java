package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.ui.CustomScrollBarUI;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.Document;

public class LoggerConsoleImpl extends JScrollPane implements LoggerConsole {
    //logger xml-file defined
    private static final Logger logger = LoggerFactory.getLogger(LoggerConsole.class);
    private final Console console = new Console();

    public LoggerConsoleImpl() {
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        setViewportView(console);
        setViewportBorder(null);
        getVerticalScrollBar().setUI(new CustomScrollBarUI());
        getHorizontalScrollBar().setUI(new CustomScrollBarUI());

        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = loggerContext.getConfiguration();
        LoggerConfig rootLoggerConfig = configuration.getLoggerConfig("");
        Appender appender = new LoggerAppender(this);
        rootLoggerConfig.addAppender(appender, Level.ALL, null);
    }



    @Override
    public int getLineCount() {
        return console.getLineCount();
    }

    @Override
    public String getText() {
        return console.getText();
    }

    @Override
    public void setText(String text) {
        console.setText(text);
    }

    @Override
    public void append(String text) {
        console.append(text);
    }

    @Override
    public Document getDocument() {
        return console.getDocument();
    }

    private class Console extends JTextArea {
        Console() {
            super(12, 0);
            setLineWrap(false);
            setWrapStyleWord(false);
            setEditable(false);
            setFont(getFont().deriveFont(11f));
        }
    }
}
