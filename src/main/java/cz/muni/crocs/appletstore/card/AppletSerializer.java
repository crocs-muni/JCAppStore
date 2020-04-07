package cz.muni.crocs.appletstore.card;

import java.io.File;

/**
 * Serialize installed-from-store applet info
 * @param <T> type to serialize
 */
public interface AppletSerializer<T> {

    void serialize(T data, File file) throws LocalizedCardException;

    T deserialize(File file) throws LocalizedCardException;
}
