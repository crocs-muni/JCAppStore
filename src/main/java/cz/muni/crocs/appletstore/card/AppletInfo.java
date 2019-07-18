package cz.muni.crocs.appletstore.card;

import apdu4j.HexUtils;
import cz.muni.crocs.appletstore.iface.IniParser;
import cz.muni.crocs.appletstore.util.IniParserImpl;
import pro.javacard.AID;
import pro.javacard.gp.GPRegistryEntry;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Simplified GPRegistryEntry version with additional information obtained from our database (about specific applet)
 * @author Jiří Horák
 * @version 1.0
 */
public class AppletInfo {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private AID aid;
    private int lifecycle;
    private GPRegistryEntry.Kind kind;
    private AID domain;

    private String name;
    private String image;
    private String version;
    private String author;
    public KeysPresence hasKeys = KeysPresence.UNKNOWN;

    private boolean selected = false;

    public boolean isSelected() {
        return selected;
    }
    public KeysPresence hasKeys() {
        return hasKeys;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Get the info from card registry
     * @param registry GP info from a card
     */
    public AppletInfo(GPRegistryEntry registry) {
        if (registry != null) {
            aid = registry.getAID();
            lifecycle = registry.getLifeCycle();
            kind = registry.getType();
            domain = registry.getDomain();
            deduceData(registry);
        }
    }

    public AppletInfo(GPRegistryEntry registry, IniParser parser) {
        if (registry != null) {
            aid = registry.getAID();
            lifecycle = registry.getLifeCycle();
            kind = registry.getType();
            domain = registry.getDomain();
            deduceData(registry);
        }
    }

    public AppletInfo(GPRegistryEntry registry, String storeName) {
        if (registry != null) {
            aid = registry.getAID();
            lifecycle = registry.getLifeCycle();
            kind = registry.getType();
            domain = registry.getDomain();
            deduceData(registry);
        }

//        try {
//            getAdditionalInfo(cardId);
//        } catch (IOException e) {
//            deduceData(registry);
//        }
    }

    private void deduceData(GPRegistryEntry registry) {
        try {
            IniParserImpl parser = new IniParserImpl("src/main/resources/data/well_known_aids.ini",
                    HexUtils.bin2hex(registry.getAID().getBytes()));
            if (parser.isHeaderPresent()) {
                name = parser.getValue("name");
                name = (name.isEmpty()) ? getDefaultName(registry) : name;
                author = parser.getValue("author");
                author = (author.isEmpty()) ? textSrc.getString("unknown") : author;
            } else {
                setDefaultValues(registry);
            }
        } catch (IOException e) {
            setDefaultValues(registry);
        }
    }

    private String getDefaultName(GPRegistryEntry registry) {
        return ((registry.getType() == GPRegistryEntry.Kind.ExecutableLoadFile) ?
                "Package" : "Applet") + " ID: " + aid.toString();
    }

    private void setDefaultValues(GPRegistryEntry registry) {
        name = getDefaultName(registry);
        image = "unknown";
        version = "";
        author = textSrc.getString("unknown");
    }

    private void getAdditionalInfo(IniParser parser) throws IOException {

    }

    public AID getAid() {
        return aid;
    }

    public int getLifecycle() {
        return lifecycle;
    }

    public GPRegistryEntry.Kind getKind() {
        return kind;
    }

    public AID getDomain() {
        return domain;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

}
