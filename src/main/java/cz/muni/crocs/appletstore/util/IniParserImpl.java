package cz.muni.crocs.appletstore.util;

import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class IniParserImpl implements IniParser {

    private Ini ini;
    private String header;

    /**
     * @param file file to open
     * @param header header to read from
     * @param comment comment to add at the beggining of the file if not exists
     * @throws IOException cannot modify or read file
     */
    public IniParserImpl(File file, String header, String comment) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        this.ini = new Ini(file);
        this.header = header;
        if (comment != null && !comment.isEmpty()) {
            ini.setComment(comment);
//            ini.putComment("JCAppStore guide", comment);
            ini.store();
        }
    }

    /**
     *
     * @param path path to the file
     * @param header header to read from
     * @param comment comment to add at the beggining of the file if not exists
     * @throws IOException cannot modify or read file
     */
    public IniParserImpl(String path, String header, String comment) throws IOException {
        this(new File(path), header, comment);
    }

    /**
     * @param file file to open
     * @param header header to read from
     * @throws IOException cannot modify or read file
     */
    public IniParserImpl(File file, String header) throws IOException {
        this(file, header, "");
    }

    /**
     * @param path path to the file
     * @param header header to read from
     * @throws IOException cannot modify or read file
     */
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

    public Set<String> keySet() {
        Profile.Section section = ini.get(header);
        if (section == null) return null;
        return section.keySet();
    }
}
