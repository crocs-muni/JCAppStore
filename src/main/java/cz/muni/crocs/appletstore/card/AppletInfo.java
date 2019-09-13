package cz.muni.crocs.appletstore.card;

import apdu4j.HexUtils;
import cz.muni.crocs.appletstore.util.IniParserImpl;
import pro.javacard.AID;
import pro.javacard.gp.GPRegistryEntry;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Simplified GPRegistryEntry version with additional information obtained from our database (about specific applet)
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class AppletInfo implements Serializable {
    private static final long serialVersionUID = 458932548615025100L;
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private transient AID aid;
    private transient int lifecycle;
    private transient GPRegistryEntry.Kind kind;
    private transient AID domain;

    private String strAid;
    private String name;
    private String image;
    private String version;
    private String author;
    private String sdk;
    public KeysPresence hasKeys = KeysPresence.UNKNOWN;

    private transient boolean selected = false;

    public AppletInfo(String name, String image, String version, String author, String sdk, KeysPresence hasKeys) {
        this.name = name;
        this.image = image;
        this.version = version;
        this.author = author;
        this.sdk = sdk;
        this.hasKeys = hasKeys;
    }

    /**
     * Get the info from card registry
     *
     * @param registry GP info from a card
     */
    public AppletInfo(GPRegistryEntry registry) {
        if (registry != null) {
            aid = registry.getAID();
            strAid = aid.toString();
            lifecycle = registry.getLifeCycle();
            kind = registry.getType();
            domain = registry.getDomain();
            deduceData(registry);
        }
    }

    public AppletInfo(GPRegistryEntry registry, List<AppletInfo> savedApplets) {
        if (registry != null) {
            aid = registry.getAID();
            strAid = aid.toString();
            lifecycle = registry.getLifeCycle();
            kind = registry.getType();
            domain = registry.getDomain();
            deduceData(registry);
        }
        if (savedApplets != null) {
            getAdditionalInfo(savedApplets);
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public KeysPresence hasKeys() {
        return hasKeys;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
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
            e.printStackTrace();
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

    private void getAdditionalInfo(List<AppletInfo> savedApplets) {
        for (AppletInfo saved : savedApplets) {
            if (saved.strAid.equals(strAid)) {
                this.name = saved.name;
                this.image = saved.image;
                this.version = saved.version;
                this.author = saved.author;
                this.hasKeys = saved.hasKeys;
                break;
            }
        }
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

    public String getSdk() {
        return sdk;
    }

    public String getAuthor() {
        return author;
    }

    /**
     * For newly installed to be recognized
     * @param aid str representation of AID, should equal to AID.toString() result
     */
    public void setAID (String aid) {
        this.strAid = aid;
    }

}
