package cz.muni.crocs.appletstore.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cz.muni.crocs.appletstore.Config;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
                + Sources.options.get(Config.OPT_KEY_LANGUAGE)
                + Config.FILE_INFO_SUFFIX);
        if (!info.exists()) {
            info = new File(Config.APP_STORE_DIR,Config.FILE_INFO_PREFIX + "en" + Config.FILE_INFO_SUFFIX);
            return info.exists();
        }
        return true;
    }

    public static List<JsonObject> getValues() throws FileNotFoundException {
        File file = getFileInfo(); //safe
        if (file == null) return null;

        ArrayList<JsonObject> result = new ArrayList<>();

        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader(new FileInputStream(file)));
        JsonArray applets = root.getAsJsonArray();
        for (JsonElement applet : applets) {
            result.add(applet.getAsJsonObject());

//            JsonObject item = applet.getAsJsonObject();
//            HashMap<String, String> appletInfo = new HashMap<>();
//            appletInfo.put(Config.JSON_TAG_TITLE, item.get(Config.JSON_TAG_TITLE).getAsString());
//            appletInfo.put(Config.JSON_TAG_ICON, item.get(Config.JSON_TAG_ICON).getAsString());
//            appletInfo.put(Config.JSON_TAG_AUTHOR, item.get(Config.JSON_TAG_AUTHOR).getAsString());
//            JsonArray versions = item.get(Config.JSON_TAG_VERSION).getAsJsonArray();
//            appletInfo.put(Config.JSON_TAG_VERSION, versions.get(versions.size()-1).getAsJsonObject().keySet().iterator().next());
//            result.put(item.get(Config.JSON_TAG_NAME).getAsString(), appletInfo);
        }
        return result;
    }

    public static String[] jsonArrayToDataArray(JsonArray array) {
        if (array == null) return null;
        int len = array.size();

        String[] result = new String[len];
        for (int i = 0; i < len; i++) {
            //todo try simpler
            result[i] = array.get(i).getAsString();
        }
        return result;
    }
}
