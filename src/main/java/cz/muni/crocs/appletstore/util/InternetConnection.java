package cz.muni.crocs.appletstore.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cz.muni.crocs.appletstore.Config;

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
            final URL url = new URL(Config.REMOTE_STORE_LATEST_URL);
            final URLConnection conn = url.openConnection();
            conn.connect();
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) conn.getContent()));
            conn.getInputStream().close();
            JsonObject latest = root.getAsJsonObject();

            String latestVersion = latest.get("name").getAsString();

            return new String[] {
                    currentVersion.equals(latestVersion) ? "ok" : "nok",
                    latest.get("zipball_url").getAsString(),
                    latestVersion
            };
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }
}
