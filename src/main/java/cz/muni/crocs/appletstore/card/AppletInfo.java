package cz.muni.crocs.appletstore.card;

import apdu4j.HexUtils;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.util.IniParser;
import cz.muni.crocs.appletstore.util.Sources;
import pro.javacard.AID;
import pro.javacard.gp.GPRegistryEntry;

import java.io.IOException;

/**
 * Simplified GPRegistryEntry version with additional information obtained from our database (about specific applet)
 * @author Jiří Horák
 * @version 1.0
 */
public class AppletInfo {

    private AID aid;
    private int lifecycle;
    private GPRegistryEntry.Kind kind;
    private AID domain;

    private String name; //todo set values to be right to use for display e g. missing title -> put aid isntead
    private String image; //todo check image and put default if not found
    private String version;
    private String author;
    private HasKeys hasKeys = HasKeys.UNKNOWN;

    private boolean selected = false;

    public boolean isSelected() {
        return selected;
    }
    public HasKeys hasKeys() {
        return hasKeys;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Get the info from card registry
     * @param registry GP info from a card
     */
    public AppletInfo(GPRegistryEntry registry, String cardId) {
        aid = registry.getAID();
        lifecycle = registry.getLifeCycle();
        kind = registry.getType();
        domain = registry.getDomain();
        deduceData(registry);

//        try {
//            getAdditionalInfo(cardId);
//        } catch (IOException e) {
//            deduceData(registry);
//        }
    }

    private void deduceData(GPRegistryEntry registry) {
        try {
            IniParser parser = new IniParser("src/main/resources/data/well_known_aids.ini",
                    HexUtils.bin2hex(registry.getAID().getBytes()));
            if (parser.isHeaderPresent()) {
                name = parser.getValue("name");
                name = (name.isEmpty()) ? getDefaultName(registry) : name;
                author = parser.getValue("author");
                author = (author.isEmpty()) ? Sources.language.get("unknown") : author;
            } else {
                setDefaultValues(registry);
            }
        } catch (IOException e) {
            setDefaultValues(registry);
        }
    }

    private String getDefaultName(GPRegistryEntry registry) {
        return ((registry.getType() == GPRegistryEntry.Kind.ExecutableLoadFile) ? "Package" : "Applet") + " ID: " + aid.toString();
    }

    private void setDefaultValues(GPRegistryEntry registry) {
        name = getDefaultName(registry);
        image = "unknown"; //will be replaced based on its type
        version = "";
        //todo deduce author
        author = Sources.language.get("unknown");
    }

    private void getAdditionalInfo(String cardId) throws IOException {
        IniParser parser = new IniParser("", cardId);
        //todo heskeys whether contains sensitive info
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

    public enum HasKeys {
        PRESENT, NO_KEYS, UNKNOWN
    }
}
