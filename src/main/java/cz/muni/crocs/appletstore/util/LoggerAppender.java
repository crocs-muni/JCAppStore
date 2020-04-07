package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import javax.swing.*;

/**
 * Logger appender, appends the logs to GUI logger
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LoggerAppender extends AbstractAppender {

    private volatile LoggerConsole appender;
    public static final int LINES = 150;

    public LoggerAppender(LoggerConsole console) {
        super("JCAppStore", null, PatternLayout.createDefaultLayout(), false);

        this.appender = console;
        this.appender.setText("JCAppStore logger: for detailed logs see '" + Config.APP_LOG_DIR.getAbsolutePath()
                + " folder'\n");
        start();
    }

    @Override
    public void append(LogEvent event) {
        if (LINES == 0 || appender == null) return;

        Level l = event.getLevel();
        if (!OptionsFactory.getOptions().is(Options.KEY_VERBOSE_MODE) && (l == Level.DEBUG || l == Level.TRACE))
            return;

        String message = new String(this.getLayout().toByteArray(event));
        try {
            SwingUtilities.invokeLater(() ->
            {
                try {
                    while (appender.getLineCount() > LINES) {
                        appender.getDocument().remove(0, appender.getDocument().getText(
                                0, appender.getDocument().getLength()).indexOf("\n") + 1);
                    }
                    appender.append(message);
                } catch (Throwable throwable) {
                    //do not log
                    throwable.printStackTrace();
                }
            });
        } catch (Exception e) {
            //do not log
            e.printStackTrace();
        }
    }
}
