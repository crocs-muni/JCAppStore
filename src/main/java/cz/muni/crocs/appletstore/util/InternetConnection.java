package cz.muni.crocs.appletstore.util;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cz.muni.crocs.appletstore.Config;
import netscape.javascript.JSObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Jiří on 21. 6. 2017.
 */

public class InternetConnection {

    /**
     * Check for the latest version and internet connection as well
     * @param currentVersion string of the latest version downloaded
     * @return url for download if not up to date, "ok" if up to date, null if no internet connection
     */
    public static String[] checkAndGetLatestReleaseVersion(String currentVersion) {
        try {
            final URL url = new URL("https://api.github.com/repos/ShareX/ShareX/releases/latest");
            final URLConnection conn = url.openConnection();
            conn.connect();
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) conn.getContent()));
            conn.getInputStream().close();

            JsonArray assets = root.getAsJsonObject().get("assets").getAsJsonArray();
            JsonObject latest = assets.get(0).getAsJsonObject();

            String latestVersion = latest.get("name").getAsString();
            //TODO check the folder emptiness before returning OK
            if (currentVersion.equals(latestVersion)) {
                return new String[] {"ok", "", ""};
            }
            return new String[] {latest.get("browser_download_url").getAsString(), latestVersion, latest.get("size").getAsString()};
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }
}
