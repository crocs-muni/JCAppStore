package cz.muni.crocs.appletstore.card;

import apdu4j.HexUtils;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.util.IniCardTypesParser;
import cz.muni.crocs.appletstore.util.IniCardTypesParserImpl;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.gp.GPRegistryEntry;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * GPRegistryEntry "extension" with additional information obtained from our database (about specific applet)
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class AppletInfo implements Serializable, Cloneable {
    public static final int RID_SUBSTRING_LENGTH = 10;

    private static final long serialVersionUID = 458932548615025100L;
    private static final Logger logger = LoggerFactory.getLogger(AppletInfo.class);
    private static final ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private static IniCardTypesParser knownAids;
    private static IniCardTypesParser knownRids;

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

    private static IniCardTypesParser loadSource(String path, String error) {
        try {
            return new IniCardTypesParserImpl(path, "");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(error, e);
            return null;
        }
    }

    public AppletInfo() {
        if (knownAids == null) {
            knownAids = loadSource(Config.DATA_DIR + "well_known_aids.ini", "Failed to get known aids.");
        }
        if (knownRids == null) {
            knownRids = loadSource(Config.DATA_DIR + "well_known_rids.ini", "Failed to get known rids.");
        }
    }

    public AppletInfo(String name, String image, String version, String author, String sdk) {
        this();
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
     * @param registry GP info from a card
     */
    public AppletInfo(GPRegistryEntry registry) {
        this();
        if (registry != null) {
            aid = registry.getAID().toString();
            lifecycle = registry.getLifeCycle();
            kind = registry.getType();
            domain = registry.getDomain();
            fillModules(registry);
            deduceData(registry);
        }
    }

    /**
     * Get the info from card registry and saved applet data
     * @param registry GP info from a card
     * @param savedApplets applets serialized from this card before
     */
    public AppletInfo(GPRegistryEntry registry, Set<AppletInfo> savedApplets) {
        this();
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
        if (knownAids != null) {
            knownAids.header(HexUtils.bin2hex(registry.getAID().getBytes()));
            if (knownAids.isHeaderPresent()) {
                name = knownAids.getValue("name");
                name = ((name.isEmpty()) ? getDefaultName(registry) : name);
                author = knownAids.getValue("author");
                author = (author.isEmpty()) ? getAuthorByRid(registry) : author;
                return;
            }
        }
        setDefaultValues(registry);
    }

    private String getAuthorByRid(GPRegistryEntry registry) {
        if (knownRids != null) {
            knownRids.header(registry.getAID().toString().trim().toUpperCase().substring(0, RID_SUBSTRING_LENGTH));
            if (knownRids.isHeaderPresent()) {
                return knownRids.getValue("author");
            }
        }
        return textSrc.getString("unknown");
    }

    private String getDefaultName(GPRegistryEntry registry) {
        return registry.getAID().toString();
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
        if (aid == null || aid.isEmpty()) return null;
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

    public void setAppletInstanceName(String name) {
        if (name != null && !name.isEmpty())
            this.name = this.name + ": " + name;
    }

    public int compareAIDs(AppletInfo other) {
        return aid.compareTo(other.aid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppletInfo that = (AppletInfo) o;
        return (kind != null || kind == that.kind) && Objects.equals(aid, that.aid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, aid);
    }

    @Override
    public String toString() {
        String unknown = textSrc.getString("unknown");
        return type(kind) + ": " + (valid(name) ? name : unknown) + ", " + aid + textSrc.getString("comma_author") +
                (valid(author) ? author : unknown) + textSrc.getString("comma_version") +
                (valid(version) ? version : unknown) + textSrc.getString("comma_sdk") + (valid(sdk) ? sdk : unknown);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return (AppletInfo)super.clone();
    }

    private boolean valid(String value) {
        return value != null && !value.isEmpty();
    }

    private String type(GPRegistryEntry.Kind kind) {
        if (kind == null) return textSrc.getString("unknown");

        switch (kind) {
            case ExecutableLoadFile: return textSrc.getString("package");
            case SecurityDomain: return textSrc.getString("sd");
            case IssuerSecurityDomain: return textSrc.getString("isd");
            case Application: return textSrc.getString("applet");
        }
        return textSrc.getString("unkown");
    }

    public static AppletInfo empty() {
        return new AppletInfo("", "", "", "", "");
    }

    public static AppletInfo with(String aid) {
        return new AppletInfo("", "", "", "", "", aid);
    }
}
