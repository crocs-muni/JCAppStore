package cz.muni.crocs.appletstore.util;

import com.sun.istack.internal.NotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.layout.CsvParameterLayout;
import org.apache.logging.log4j.core.layout.HtmlLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;

import javax.swing.*;
import java.nio.charset.Charset;


@Plugin(name = "JCAppStoreAppender", category = "Core", elementType = "appender", printObject = true)
public class LoggerAppender extends AbstractAppender {

    private volatile LoggerConsole appender;
    public static final int LINES = 150;

    public LoggerAppender(@NotNull LoggerConsole console) {
        //todo filter?
        super("JCAppStore", null, PatternLayout.createDefaultLayout(), false);

        this.appender = console;
        this.appender.setText("JCAppStore logger: for detailed logs see TODO output logs to accessible user folder");
        start();
    }

    @Override
    public void append(LogEvent event) {
        Level l = event.getLevel();
        if (!OptionsFactory.getOptions().isVerbose() && (l == Level.DEBUG || l == Level.TRACE))
            return;

        String message = new String(this.getLayout().toByteArray(event));
        try {
            assert (appender != null);
            SwingUtilities.invokeLater(() ->
            {
                try {
                    // https://stackoverflow.com/questions/24005748/how-to-output-logs-to-a-jtextarea-using-log4j2
                    appender.append("\n" + message);
                    if (LINES > 0 & appender.getLineCount() > LINES + 1) {
                        int endIdx = appender.getDocument().getText(0, appender.getDocument().getLength()).indexOf("\n");
                        appender.getDocument().remove(0, endIdx + 1);
                    }
                    String content = appender.getText();
                    appender.setText(content.substring(0, content.length() - 1));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
