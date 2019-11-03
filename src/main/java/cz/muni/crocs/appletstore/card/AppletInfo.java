package cz.muni.crocs.appletstore.card;

import apdu4j.HexUtils;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.util.IniParserImpl;
import pro.javacard.AID;
import pro.javacard.gp.GPRegistryEntry;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Simplified GPRegistryEntry version with additional information obtained from our database (about specific applet)
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class AppletInfo implements Serializable {
    private static final long serialVersionUID = 458932548615025100L;
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private transient int lifecycle;
    private transient GPRegistryEntry.Kind kind;
    private transient List<AID> modules;
    private transient AID domain;

    private String aid;
    private String name;
    private String image;
    private String version;
    private String author;
    private String sdk;
    public KeysPresence hasKeys = KeysPresence.UNKNOWN;

    public AppletInfo(String name, String image, String version, String author, String sdk) {
        this.name = name;
        this.image = image;
        this.version = version;
        this.author = author;
        this.sdk = sdk;
    }

    public AppletInfo(String name, String image, String version, String author, String sdk, String strAid) {
        this(name, image, version, author, sdk);
        this.aid = strAid;
    }

    public AppletInfo(String name, String image, String version, String author, String sdk, String strAid,
                      KeysPresence hasKeys, GPRegistryEntry.Kind kind) {
        this(name, image, version, author, sdk, strAid);
        this.hasKeys = hasKeys;
        this.kind = kind;
    }

    public AppletInfo(String name, String image, String version, String author, String sdk, String strAid,
                      KeysPresence hasKeys, GPRegistryEntry.Kind kind, AID ... instances) {
        this(name, image, version, author, sdk, strAid, hasKeys, kind);
        this.modules = Arrays.asList(instances);
    }

    /**
     * Get the info from card registry
     *
     * @param registry GP info from a card
     */
    public AppletInfo(GPRegistryEntry registry) {
        if (registry != null) {
            aid = registry.getAID().toString();
            lifecycle = registry.getLifeCycle();
            kind = registry.getType();
            domain = registry.getDomain();
            fillModules(registry);
            deduceData(registry);
        }
    }

    public AppletInfo(GPRegistryEntry registry, Set<AppletInfo> savedApplets) {
        if (registry != null) {
            aid = registry.getAID().toString();
            lifecycle = registry.getLifeCycle();
            kind = registry.getType();
            domain = registry.getDomain();
            fillModules(registry);
        }
        if (savedApplets != null) {
            getAdditionalInfo(savedApplets, registry);
        } else if (registry != null) {
            deduceData(registry);
        }
    }

    public KeysPresence hasKeys() {
        return hasKeys;
    }

    public List<AID> getModules() {
        return modules;
    }

    private void fillModules(GPRegistryEntry entry){
        if (entry.getType() == GPRegistryEntry.Kind.ExecutableLoadFile) {
            modules = entry.getModules();
        } else {
            modules = new ArrayList<>();
        }
    }

    private void deduceData(GPRegistryEntry registry) {
        try {
            IniParserImpl parser = new IniParserImpl(Config.DATA_DIR + "well_known_aids.ini",
                    HexUtils.bin2hex(registry.getAID().getBytes()));
            if (parser.isHeaderPresent()) {
                name = parser.getValue("name");
                name = (name.isEmpty()) ? getDefaultName(registry) : name;
                author = parser.getValue("author");
                author = (author.isEmpty()) ? getAuthorByRid(registry) : author;
            } else {
                setDefaultValues(registry);
            }
        } catch (IOException e) {
            e.printStackTrace();
            setDefaultValues(registry);
        }
    }

    private String getAuthorByRid(GPRegistryEntry registry) {
        try {
            IniParserImpl parser = new IniParserImpl(Config.DATA_DIR + "well_known_rids.ini",
                    registry.getAID().toString().toUpperCase().substring(0, 10));
            if (parser.isHeaderPresent()) {
                return parser.getValue("author");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return textSrc.getString("unknown");
    }

    private String getDefaultName(GPRegistryEntry registry) {
        return ((registry.getType() == GPRegistryEntry.Kind.ExecutableLoadFile) ?
                "Package" : "Applet") + " ID: " + aid.toString();
    }

    private void setDefaultValues(GPRegistryEntry registry) {
        name = getDefaultName(registry);
        image = "unknown";
        version = "";
        author = getAuthorByRid(registry);
    }

    private void getAdditionalInfo(Set<AppletInfo> savedApplets, GPRegistryEntry entry) {
        for (AppletInfo saved : savedApplets) {
            if (saved.aid != null && saved.aid.equals(aid)) {
                this.name = saved.name;
                this.image = saved.image;
                this.version = saved.version;
                this.author = saved.author;
                this.hasKeys = saved.hasKeys;
                this.sdk = saved.sdk;
                return;
            }
        }
        if (entry != null) deduceData(entry);
    }

    public AID getAid() {
        return AID.fromString(aid);
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
        this.aid = aid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppletInfo that = (AppletInfo) o;
        return (kind == null || kind == that.kind) && Objects.equals(aid, that.aid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, aid);
    }
}
