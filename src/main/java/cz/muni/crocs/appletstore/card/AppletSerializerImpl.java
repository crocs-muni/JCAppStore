package cz.muni.crocs.appletstore.card;

import java.io.*;
import java.util.Set;

/**
 * Applet info serialization
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class AppletSerializerImpl implements AppletSerializer<Set<AppletInfo>> {

    @Override
    public void serialize(Set<AppletInfo> data, File file) throws LocalizedCardException {
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream out = new ObjectOutputStream(fos)) {

            out.writeObject(data);

        } catch (IOException e) {
            throw new LocalizedCardException("Failed to save applets into file.", "E_serialize_applets", e);
        }
    }

    @Override
    public Set<AppletInfo> deserialize(File file) throws LocalizedCardException {
        if (!file.exists())
            throw new LocalizedCardException("No file.", "E_no_applets_file");

        Set<AppletInfo> result;

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream in = new ObjectInputStream(fis)) {

            result = (Set<AppletInfo>) in.readObject();

        } catch (IOException e) {
            throw new LocalizedCardException("Failed to save applets into file.", "E_deserialize_applets", e);
        } catch (ClassNotFoundException e) {
            throw new LocalizedCardException("Unable to serialize: class not present.", "E_deserialize_applets", e);
        }
        return result;
    }
}
