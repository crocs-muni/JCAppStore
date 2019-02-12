package cz.muni.crocs.appletstore.util;

import java.io.File;
import java.util.Objects;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Cleaner {

    /**
     * Deletes all files inside folder
     * @param folder folder to clear
     * @return true if deleting succeeded
     */
    public static boolean cleanFolder(File folder) {
        boolean res = true;
        for (File f : Objects.requireNonNull(folder.listFiles())){
            res = deleteRecursive(f);
        }
        return res;
    }

    /**
     * Deletes file (folder) recursively
     * @param path file to recursively delete
     * @return true if deleting succeeded
     */
    public static boolean deleteRecursive(File path) {
        if (!path.exists()) return false;
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }

}
