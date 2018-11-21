package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.Config;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.TreeMap;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Translation {

    private TreeMap<Integer, String> data;

    public Translation (String lang) {
        if (!init(lang, 1)) {
            throw new IllegalArgumentException("Could not deduce a language: " + lang);
        }
    }

    private boolean init(String lang, int tries) {
        if (tries == 0) return false;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(Config.LANG_DIR + lang + ".xml" ));

            data = new TreeMap<>();
            NodeList nodes = document.getElementsByTagName("string");
            parseData(nodes);
            return !(data == null || data.size() < 1); //if data null, doesn't call the second one
        } catch (javax.xml.parsers.ParserConfigurationException | org.xml.sax.SAXException ex) {
            ex.printStackTrace();
        } catch (java.io.IOException ex) {
            init("eng", 0);
        }
        return false;
    }
    private void parseData(NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                data.put(Integer.valueOf(eElement.getAttribute("id")), eElement.getTextContent());
            }
        }
    }


    public String get(int id) {
        return data.get(id);
    }
}