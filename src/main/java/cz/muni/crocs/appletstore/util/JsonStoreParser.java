package cz.muni.crocs.appletstore.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class JsonStoreParser implements JsonParser {

    private File info;
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    public List<JsonObject> getValues() throws FileNotFoundException {
        File file = getFileInfo(); //safe
        if (file == null) return null;

        ArrayList<JsonObject> result = new ArrayList<>();

        com.google.gson.JsonParser jp = new com.google.gson.JsonParser();
        JsonElement root = jp.parse(new InputStreamReader(new FileInputStream(file)));
        JsonArray applets = root.getAsJsonArray();
        for (JsonElement applet : applets) {
            result.add(applet.getAsJsonObject());
        }
        return result;
    }

    public String[] jsonArrayToDataArray(JsonArray array) {
        if (array == null) return null;
        int len = array.size();
        String[] result = new String[len];
        for (int i = 0; i < len; i++) {
            result[i] = array.get(i).getAsString();
        }
        return result;
    }

    private File getFileInfo() {
        if (info == null) {
            if (!verifyInfoFile()) {
                return null;
            }
        }
        return info;
    }

    private boolean verifyInfoFile() {
        info = new File(Config.APP_STORE_DIR, Config.FILE_INFO_PREFIX
                + OptionsFactory.getOptions().getOption(Options.KEY_LANGUAGE)
                + Config.FILE_INFO_SUFFIX);
        if (!info.exists()) {
            info = new File(Config.APP_STORE_DIR,Config.FILE_INFO_PREFIX + "en" + Config.FILE_INFO_SUFFIX);
            return info.exists();
        }
        return true;
    }
}
