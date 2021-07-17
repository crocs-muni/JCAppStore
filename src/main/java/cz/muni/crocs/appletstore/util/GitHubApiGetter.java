package cz.muni.crocs.appletstore.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

/**
 * Check GitHub connection and get latest release name
 *
 * @author Jiří Horák
 * @version 1.0
 */

public class GitHubApiGetter {
    private static final Logger logger = LoggerFactory.getLogger(GitHubApiGetter.class);

    /**
     * Use GitHub API to open a Json file
     * @param URL url of the Github API JSON file
     * @return json element (google parser lib) of the root
     */
    public static JsonElement getJsonContents(String URL) {
        URLConnection conn = null;
        try {
            final URL url = new URL(URL);
            conn = url.openConnection();
            conn.connect();
            JsonParser parser = new JsonParser();
            return parser.parse(new InputStreamReader((InputStream) conn.getContent()));
        } catch (UnknownHostException e) {
            logger.warn("Could not recognize the Github API URL.", e);
            conn = null;
            return null;
        } catch (IOException | NullPointerException e) {
            logger.warn("Could not open a connection to github.", e);
            return null;
        } finally {
            if (conn != null) {
                try {
                    InputStream is = conn.getInputStream();
                    if (is != null) is.close();
                } catch (IOException e) {
                    logger.warn("Failed to close a stream from github connection.", e);
                }
            }
        }
    }
}
