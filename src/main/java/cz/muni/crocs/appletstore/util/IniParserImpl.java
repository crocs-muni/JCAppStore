package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.iface.IniParser;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class IniParserImpl implements IniParser {

    private Ini ini;
    private String header;

    public IniParserImpl(File file, String header) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        this.ini = new Ini(file);
        this.header = header;
    }

    public IniParserImpl(String path, String header) throws IOException {
        this(new File(path), header);
    }

    public String getValue(String key) {
        String value = ini.get(header, key, String.class);
        return (value == null) ? "" : value.trim();
    }

    public IniParserImpl addValue(String key, String value) {
        ini.put(header, key, value);
        return this;
    }

    public IniParserImpl addValue(String key, byte[] value) {
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
