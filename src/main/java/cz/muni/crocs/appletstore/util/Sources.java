package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.FeedbackFatalError;
import cz.muni.crocs.appletstore.OptionsManager;
import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.ui.CustomFont;

import javax.smartcardio.Card;
import javax.swing.*;
import javax.swing.text.html.StyleSheet;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Sources {

    public static HashMap<String, String> options;
    public static Language language;
    public static CardManager manager;
    public final static StyleSheet sheet = new StyleSheet();

    public static void generalSetupAndLoadOptions() {
        options = OptionsManager.getFileOptions();
        CustomFont.refresh();
        language = new Language("en");
    }

    public static void setupManager() {
        manager = new CardManager();
        manager.needsCardRefresh();
        manager.refreshCard();
    }

    public static void setStyles() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream("src/main/resources/css/default.css")));
            sheet.loadRules(br, null);
            br.close();
        } catch (IOException e) {
            sheet.addRule("body {\n" +
                    "    font-size: 11px;\n" +
                    "}\n" +
                    ".code {\n" +
                    "    background: #F4F4F4;\n" +
                    "    border-radius: 5px;\n" +
                    "    border-left: 3px solid #f36d33;\n" +
                    "    color: #676767;\n" +
                    "    page-break-inside: avoid;\n" +
                    "    font-family: monospace;\n" +
                    "    font-size: 10px;\n" +
                    "    line-height: 1.6;\n" +
                    "    margin: 15px 5px;\n" +
                    "    max-width: 550px;\n" +
                    "    overflow: auto;\n" +
                    "    padding: 4px;\n" +
                    "    display: block;\n" +
                    "    word-wrap: break-word;\n" +
                    "}");
        }
    }
}
