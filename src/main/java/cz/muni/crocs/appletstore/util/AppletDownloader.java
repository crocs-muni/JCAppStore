package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * @author Jiří Horák
 * @version 1.0
 */
public class AppletDownloader {

    private static final Logger logger = LogManager.getLogger(AppletDownloader.class);
    private String zipFile;
    private String address;
    private File directory;
    private int downloaded = 0;
    private int size;

    private DownloaderWorkerThread parent;

    public AppletDownloader(int size, String address, DownloaderWorkerThread parentThread) {
        parent = parentThread;
        zipFile = Config.APP_STORE_DIR + "/root.rar";
        this.size = size;
        this.address = address;
        directory = new File(Config.APP_STORE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        cleanDirectory();
    }

    public boolean run() {
        cleanDirectory();
        if (!downloadZip(address)) {
            return false;
        }
        parent.setProgress(80);

        if (downloaded != size || !unZipIt()) {
            return false;
        } else {
            new File(zipFile).delete();
        }
        return true;
    }

    private void cleanDirectory() {
        String[] files;
        if (directory.isDirectory()) {
            files = directory.list();

            if (files == null) return;

            for (String file1 : files) {
                File newfile = new File(directory, file1);
                newfile.delete();
            }
        }
    }

    private boolean downloadZip(String address) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(address).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(zipFile)) {
             byte dataBuffer[] = new byte[1024];
             int bytesRead;
             while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                 fileOutputStream.write(dataBuffer, 0, bytesRead);
                 downloaded += bytesRead;
                 parent.setProgress((size * 80) / downloaded); //80% of progress
             }
        } catch (IOException e) {
            logger.error("Could not download file from github.");
            return false;
        }
        return true;
    }

    private static void saveFile(File newFile, ZipInputStream input) throws IOException {
        byte[] buffer = new byte[1024];
        FileOutputStream fos = new FileOutputStream(newFile);
        int len;
        while ((len = input.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fos.close();
    }

    /*
    From https://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
    Corrected errors, the srouce code on the website is wrong, at least for windows.
     */
    private boolean unZipIt() {
        ZipInputStream input = null;
        ZipEntry entry = null;
        try{
            input = new ZipInputStream(new FileInputStream(zipFile));
            entry = input.getNextEntry();

            while(entry != null){
                String fileName = entry.getName();
                File newFile = new File(Config.APP_STORE_DIR + File.separator + fileName);
                System.out.println("file unzip : "+ newFile.getAbsoluteFile());

                File par = new File(newFile.getParent());
                if(!par.mkdirs()) {
                    logger.error("Fatal Error: Failed attempt to create folder: " + par.getAbsolutePath());
                    return false;
                }

                if (fileName.contains(".")) {
                    saveFile(newFile, input);
                    parent.raiseProgressByOne();
                }
                entry = input.getNextEntry();
                input.closeEntry();
                input.close();
            }
            System.out.println("Done");
        } catch(IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}

/*
 *
 * Modified from https://stackoverflow.com/questions/13441720/download-binary-file-from-github-using-java
 * Handles multiple redirections from GitHub
 *
 */
//    ArrayList<String> files = new ArrayList<>();
//
//    public AppletDownloader() throws IOException, SAXException, ParserConfigurationException {
//        parseFilesList();
//        ArrayList<String> missingApplets = checkMissing();
//        getMissingFiles(missingApplets);
//    }
//
//    private String fileName(Path path) {
//        return path.getFileName().toString();
//    }
//
//    private boolean validXml(Path filepath) {
//        String name = fileName(filepath);
//        int lastIndexOf = name.lastIndexOf(".");
//        if (lastIndexOf == -1) {
//            return false; // empty
//        }
//        return name.substring(lastIndexOf + 1).equals("xml");
//    }
//
//
//    public ArrayList<String> checkMissing() {
//        ArrayList<String> missing = new ArrayList<>();
//        try (Stream<Path> paths = Files.walk(Paths.get(Config.APPLET_INFO_DIR))) {
//            paths.filter(f -> validXml(f) && !files.contains(fileName(f))).map(f -> missing.add(fileName(f)));
//            System.out.println(missing);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return missing;
//    }
//
//    /**
//     * Get input stream from given url
//     *
//     * @param urlAddress url to check
//     * @return input stream of a file
//     * @throws IOException probably invalid url
//     */
//    private InputStream fetchInputstream(String urlAddress) throws IOException {
//        URL url = new URL(urlAddress);
//        HttpURLConnection http = (HttpURLConnection) url.openConnection();
//        Map<String, List<String>> header = http.getHeaderFields();
//        while (isRedirected(header)) {
//            urlAddress = header.get("Location").get(0);
//            url = new URL(urlAddress);
//            http = (HttpURLConnection) url.openConnection();
//            header = http.getHeaderFields();
//        }
//        return http.getInputStream();
//    }
//
//    private static boolean isRedirected(Map<String, List<String>> header) {
//        for (String hv : header.get(null)) {
//            if (hv.contains(" 301 ") || hv.contains(" 302 ")) return true;
//        }
//        return false;
//    }
//
//    public void parseFilesList() throws IOException, SAXException, ParserConfigurationException {
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        factory.setIgnoringComments(true);
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document document = builder.parse(fetchInputstream(Config.REMOTE_DIR_INFO + Config.FILE_LIST_SOURCE));
//        NodeList nodes = document.getElementsByTagName(Config.CONTAINER_FILE_LIST_TAG);
//        parseXmlFileList(nodes);
//    }
//
//    private void parseXmlFileList(NodeList nodes) {
//        for (int i = 0; i < nodes.getLength(); i++) {
//            Node node = nodes.item(i);
//            if (node.getNodeType() == Node.ELEMENT_NODE) {
//                Element eElement = (Element) node;
//                files.add(eElement.getTextContent());
//            }
//        }
//    }
//
//    public void getMissingFiles(ArrayList<String> missingFiles) throws ParserConfigurationException, IOException, SAXException {
//        for (String missing : missingFiles) {
//            byte[] buffer = new byte[4096];
//            int n = -1;
//            InputStream input = fetchInputstream(Config.REMOTE_DIR_INFO + missing);
//            OutputStream output = new FileOutputStream(new File(missing));
//            while ((n = input.read(buffer)) != -1) {
//                output.write(buffer, 0, n);
//            }
//            output.close();
//        }
//    }
//
//    public ArrayList<String> getFiles() {
//        return files;
//    }

