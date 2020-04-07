package cz.muni.crocs.appletstore.help;

import cz.muni.crocs.appletstore.ui.TextField;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Help section builder, works with keys from translation file ResourceBundle
 * does not require any toValue() call as it adds the data instantly.
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class HelpBuilder extends Help {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private float title;
    private float subtitle;
    private String css;

    /**
     * Create new help builder
     * @param titleSize size of the title
     * @param subtitleSize size of the subtitle
     * @param paragraphWith width of the help text sections
     */
    public HelpBuilder(float titleSize, float subtitleSize, int paragraphWith) {
        this.title = titleSize;
        this.subtitle = subtitleSize;
        this.css = "width: " + paragraphWith + "px;";
    }

    /**
     * Adds title to the help section
     * @param textResourceKey key from ResourceBundle
     * @return builder for pattern chain
     */
    public HelpBuilder addTitle(String textResourceKey) {
        add(getLabel(textSrc.getString(textResourceKey), title), "wrap");
        return this;
    }

    /**
     * Adds title to the help section
     * @param textResourceKey key from ResourceBundle
     * @return builder for pattern chain
     */
    public HelpBuilder addSubTitle(String textResourceKey) {
        add(getLabel(textSrc.getString(textResourceKey), subtitle), "wrap");
        return this;
    }

    /**
     * Adds title to the help section
     * @param textResourceKey key from ResourceBundle
     * @return builder for pattern chain
     */
    public HelpBuilder addText(String textResourceKey) {
        add(TextField.getTextField(textSrc.getString(textResourceKey), css, null), "gapleft 10, wrap");
        return this;
    }
}