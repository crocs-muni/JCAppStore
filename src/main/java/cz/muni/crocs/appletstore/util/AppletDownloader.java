package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Modified from https://stackoverflow.com/questions/13441720/download-binary-file-from-github-using-java
 * Handles multiple redirections from GitHub
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class AppletDownloader {

    ArrayList<String> files = new ArrayList<>();

    public AppletDownloader() throws IOException, SAXException, ParserConfigurationException {
        parseFilesList();
        ArrayList<String> missingApplets = checkMissing();
        getMissingFiles(missingApplets);
    }

    private String fileName(Path path) {
        return path.getFileName().toString();
    }

    private boolean validXml(Path filepath) {
        String name = fileName(filepath);
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return false; // empty
        }
        return name.substring(lastIndexOf + 1).equals("xml");
    }


    public ArrayList<String> checkMissing() {
        ArrayList<String> missing = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(Config.APP_DATA_DIR + Config.APPLET_INFO_DIR))) {
            paths.filter(f -> validXml(f) && !files.contains(fileName(f))).map(f -> missing.add(fileName(f)));
            System.out.println(missing);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return missing;
    }

    /**
     * Get input stream from given url
     *
     * @param urlAddress url to check
     * @return input stream of a file
     * @throws IOException probably invalid url
     */
    private InputStream fetchInputstream(String urlAddress) throws IOException {
        URL url = new URL(urlAddress);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        Map<String, List<String>> header = http.getHeaderFields();
        while (isRedirected(header)) {
            urlAddress = header.get("Location").get(0);
            url = new URL(urlAddress);
            http = (HttpURLConnection) url.openConnection();
            header = http.getHeaderFields();
        }
        return http.getInputStream();
    }

    private static boolean isRedirected(Map<String, List<String>> header) {
        for (String hv : header.get(null)) {
            if (hv.contains(" 301 ") || hv.contains(" 302 ")) return true;
        }
        return false;
    }

    public void parseFilesList() throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(fetchInputstream(Config.REMOTE_DIR_INFO + Config.FILE_LIST_SOURCE));
        NodeList nodes = document.getElementsByTagName(Config.CONTAINER_FILE_LIST_TAG);
        parseXmlFileList(nodes);
    }

    private void parseXmlFileList(NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                files.add(eElement.getTextContent());
            }
        }
    }

    public void getMissingFiles(ArrayList<String> missingFiles) throws ParserConfigurationException, IOException, SAXException {
        for (String missing : missingFiles) {
            byte[] buffer = new byte[4096];
            int n = -1;
            InputStream input = fetchInputstream(Config.REMOTE_DIR_INFO + missing);
            OutputStream output = new FileOutputStream(new File(missing));
            while ((n = input.read(buffer)) != -1) {
                output.write(buffer, 0, n);
            }
            output.close();
        }
    }

    public ArrayList<String> getFiles() {
        return files;
    }
}
