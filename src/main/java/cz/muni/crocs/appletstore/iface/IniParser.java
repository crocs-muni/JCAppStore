package cz.muni.crocs.appletstore.iface;
import java.io.IOException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public interface IniParser {

    String getValue(String key);

    IniParser addValue(String key, String value);

    IniParser addValue(String key, byte[] value);

    void store() throws IOException;

    boolean isHeaderPresent() ;
}
