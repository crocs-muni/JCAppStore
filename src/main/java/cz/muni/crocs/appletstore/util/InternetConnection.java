package cz.muni.crocs.appletstore.util;


import cz.muni.crocs.appletstore.Config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Jiří on 21. 6. 2017.
 */

public class InternetConnection {
    //from https://stackoverflow.com/questions/1402005/how-to-check-if-internet-connection-is-present-in-java
    public static boolean isAvailable() {
        try {
            final URL url = new URL(Config.REMOTE_DIR_INFO);
            final URLConnection conn = url.openConnection();
            conn.connect();
            conn.getInputStream().close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
