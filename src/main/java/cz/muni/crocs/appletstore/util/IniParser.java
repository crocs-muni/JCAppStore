package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class IniParser {

    private Ini ini;
    private String header;

    public IniParser(File file, String header) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        this.ini = new Ini(file);
        this.header = header;
    }

    public IniParser(String filename,  String header) throws IOException {
        this(new File(Config.APP_DATA_DIR + Config.SEP + filename), header);
    }

    public String getValue(String key) {
        return ini.get(header, key, String.class);
    }

    public IniParser addValue(String key, String value) {
        ini.put(header, key, value);
        return this;
    }

    public IniParser addValue(String key, byte[] value) {
        ini.put(header, key, Arrays.toString(value));
        return this;
    }

    public void store() throws IOException {
        ini.store();
    }

    public boolean isHeaderPresent() {
        //calling on the whole file checks headers
        return ini.containsKey(header);
    }
}
