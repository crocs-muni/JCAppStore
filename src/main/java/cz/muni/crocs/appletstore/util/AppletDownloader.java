package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * @author Jiří Horák
 * @version 1.0
 */
public class AppletDownloader {

    private static final Logger logger = LogManager.getLogger(AppletDownloader.class);
    private File zipFile = new File(Config.APP_STORE_DIR, "JCAppStoreContent.zip");
    private String address;
    private int downloaded = 0;
    private int size;

    private DownloaderWorker parent;

    public AppletDownloader(String address, DownloaderWorker parentThread) {
        parent = parentThread;
        this.address = address;
    }

    public boolean run() {
        parent.setLoaderMessage("downloading");
        FileCleaner.cleanFolder(Config.APP_STORE_DIR);
        if (!downloadZip(address)) {
            System.out.println("failed to download");
            parent.setLoaderMessage(Sources.language.get("failed"));
            return false;
        }
        if (downloaded != size || !unZipIt()) {
            System.out.println("failed to unzip");

            parent.setLoaderMessage(Sources.language.get("failed"));
            return false;
        }
        parent.setLoaderMessage(Sources.language.get("done"));
        zipFile.delete();
        return true;
    }

    private boolean downloadZip(String address) {
        try {
            URLConnection connection;
            BufferedInputStream in;
            FileOutputStream fileOutputStream;
            connection = new URL(address).openConnection();

            connection.connect();
            size = connection.getContentLength();
            if (size == -1) return false;

            in = new BufferedInputStream(connection.getInputStream());
            fileOutputStream = new FileOutputStream(zipFile);
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                downloaded += bytesRead;
                parent.safeSetProgress((downloaded * 100) / size); //80% of progress
            }
            in.close();
            fileOutputStream.close();
        } catch (IOException ex) {
            logger.error("Could not download release from Github.");
            return false;
        }
        return true;
    }

    private static void saveFile(File newFile, InputStream input) throws IOException {
        byte[] buffer = new byte[1024];
        FileOutputStream fos = new FileOutputStream(newFile);
        int len;
        while ((len = input.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fos.flush();
    }

    /*
    From https://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
    Corrected errors, the srouce code on the website is wrong, at least for windows.
     */
    private boolean unZipIt() {
        parent.setLoaderMessage(Sources.language.get("unzip"));
        ZipInputStream input;
        ZipEntry entry;
        try {
            input = new ZipInputStream(new FileInputStream(zipFile));
            input.getNextEntry(); //first, ignore the most outer folder
            entry = input.getNextEntry();

            while (entry != null) {
                String fileName = entry.getName().substring(32); //TODO: can be invalid if changed the package id length
                System.out.println(fileName);

                File newFile = new File(Config.APP_STORE_DIR, fileName);

                System.out.println("file unzip : " + newFile.getAbsoluteFile());

                File par = new File(newFile.getParent());
                par.mkdirs();

                if (fileName.contains(".")) {
                    saveFile(newFile, input);
                    parent.raiseProgressByOne();
                }
                entry = input.getNextEntry();
            }
            input.closeEntry();
            input.close();
        } catch (IOException ex) {
            logger.error("Could not unzip store contents: " + ex.getMessage());
            return false;
        }
        return true;
    }
}