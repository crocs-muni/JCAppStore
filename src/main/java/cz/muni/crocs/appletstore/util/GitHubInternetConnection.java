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
 * Check GitHubConnection and get latest release name
 *
 * @author Jiří Horák
 * @version 1.0
 */

public class GitHubInternetConnection {

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
            JsonParser parser = new JsonParser();
            JsonElement root = parser.parse(new InputStreamReader((InputStream) conn.getContent()));
            conn.getInputStream().close();
            JsonObject latest = root.getAsJsonObject();

            String latestVersion = latest.get("name").getAsString();

            return new String[] {
                    currentVersion.equals(latestVersion) ? "ok" : "nok",
                    latest.get("zipball_url").getAsString(),
                    latestVersion
            };
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }
}
