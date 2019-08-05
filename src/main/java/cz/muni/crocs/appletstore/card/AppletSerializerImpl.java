package cz.muni.crocs.appletstore.card;

import java.io.*;
import java.util.List;

public class AppletSerializerImpl implements AppletSerializer<List<AppletInfo>> {

    @Override
    public void serialize(List<AppletInfo> data, File file) throws LocalizedCardException {
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream out = new ObjectOutputStream(fos)) {

            out.writeObject(data);

        } catch (IOException e) {
            throw new LocalizedCardException("Failed to save applets into file.", "E_serialize_applets", e);
        }
    }

    @Override
    public List<AppletInfo> deserialize(File file) throws LocalizedCardException {
        if (!file.exists())
            throw new LocalizedCardException("No file.", "E_no_applets_file");

        List<AppletInfo> result;

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream in = new ObjectInputStream(fis)) {

            result = (List<AppletInfo>) in.readObject();

        } catch (IOException e) {
            throw new LocalizedCardException("Failed to save applets into file.", "E_deserialize_applets", e);
        } catch (ClassNotFoundException e) {
            throw new LocalizedCardException("Unable to serialize: class not present.", "E_deserialize_applets", e);
        }
        return result;
    }
}
