package cz.muni.crocs.appletstore.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cz.muni.crocs.appletstore.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class JSONStoreParser {

    private static File info;

    private static File getFileInfo() {
        if (info == null) {
            if (!verifyInfoFile()) {
                return null;
            }
        }
        return info;
    }

    private static boolean verifyInfoFile() {
        info = new File(Config.APP_STORE_DIR, Config.FILE_INFO_PREFIX
                + Config.options.get(Config.OPT_KEY_LANGUAGE)
                + Config.FILE_INFO_SUFFIX);
        if (!info.exists()) {
            info = new File(Config.APP_STORE_DIR,Config.FILE_INFO_PREFIX + "en" + Config.FILE_INFO_SUFFIX);
            return info.exists();
        }
        return true;
    }

    public static HashMap<String, HashMap<String, String>> getDefaultValues() throws FileNotFoundException {
        File file = getFileInfo(); //safe
        if (file == null) return null;

        HashMap<String, HashMap<String, String>> result = new HashMap<>();

        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader(new FileInputStream(file)));
        JsonArray applets = root.getAsJsonArray();
        for (JsonElement applet : applets) {
            HashMap<String, String> appletInfo = new HashMap<>();
            JsonObject item = applet.getAsJsonObject();
            appletInfo.put(Config.JSON_TAG_TITLE, item.get(Config.JSON_TAG_TITLE).getAsString());
            appletInfo.put(Config.JSON_TAG_ICON, item.get(Config.JSON_TAG_ICON).getAsString());
            appletInfo.put(Config.JSON_TAG_AUTHOR, item.get(Config.JSON_TAG_AUTHOR).getAsString());
            JsonArray versions = item.get(Config.JSON_TAG_VERSION).getAsJsonArray();
            appletInfo.put(Config.JSON_TAG_VERSION, versions.get(versions.size()-1).getAsJsonObject().keySet().iterator().next());
            result.put(item.get(Config.JSON_TAG_NAME).getAsString(), appletInfo);
        }
        return result;
    }


}
