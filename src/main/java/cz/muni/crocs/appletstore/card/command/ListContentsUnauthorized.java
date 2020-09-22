package cz.muni.crocs.appletstore.card.command;

import apdu4j.CommandAPDU;
import apdu4j.ResponseAPDU;
import com.googlecode.concurrenttrees.radix.node.Node;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import com.googlecode.concurrenttrees.radixinverted.ConcurrentInvertedRadixTree;
import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.util.Tuple;
import org.bouncycastle.util.encoders.Hex;
import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GPRegistryEntry;
import pro.javacard.gp.ISO7816;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ResourceBundle;

/**
 * Try to detect installed applets without authenticating to ISD. The search is done based on well_known_aids list in
 * resources/data/ folder.
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class ListContentsUnauthorized extends GPCommand<CardInstanceMetaData> {
    private static final Logger logger = LoggerFactory.getLogger(ListContentsUnauthorized.class);
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private static final int DETECTION_FROM_AID_PREFIX_BYTES_LEN = 3;

    private final File aidList;
    private final HashSet<AppletInfo> found = new HashSet<>();

    //could not find other TRIE implementations
    private final ConcurrentInvertedRadixTree<Tuple<String, String>> applets =
            new ConcurrentInvertedRadixTree<>(new DefaultCharArrayNodeFactory());
    private apdu4j.HexUtils HexUtils;

    public ListContentsUnauthorized(File aidList) {
        this.aidList = aidList;
    }

    @Override
    public boolean execute() throws GPException, IOException {
        //consider: TRY GET_DATA '2F00'
        //logger.info("Use GET_DATA command: '2F00' that lists applets if supported.");
        //logger.debug(">> 80CA2F00 00"); //todo possibly 00CBF200 00
        //result = channel.transmit(new CommandAPDU(0x80, ISO7816.INS_GET_DATA, 0xF2, 0x00, 256));
        //logger.debug("<< " + HexUtils.bin2hex(result.getBytes()));
        //if (result.getSW() == 0x9000) {
            //todo fill in data-- found.add(...);
            //todo possible need for repeated commands (send data part 2,3,4 .. too big)
            //todo compare with AID list to get more info? author etc.
            //return true;
        //}

        if (aidList == null || !aidList.exists()) return false;
        new Ini(aidList).forEach((key, value) ->
                applets.put(key, new Tuple<>(value.get("name"), value.get("author"))));
        String unknown = textSrc.getString("unknown");

        walkTree((prefix, at) -> {
            //odd numbers != bytes
            if (prefix.length() % 2 == 1 || prefix.length() < DETECTION_FROM_AID_PREFIX_BYTES_LEN * 2) return true;
            try {
                logger.info("Detect AID at prefix: " + prefix);
                ResponseAPDU response = channel.transmit(
                        new CommandAPDU(0x00, ISO7816.INS_SELECT, 0x04, 0x00, Hex.decode(prefix)));

                if (response.getSW() == 0x9000) {
                    Object value = at.getValue();
                    if (value != null) { //applet at this node present
                        Tuple<String, String> input = (Tuple<String, String>)value;
                        AppletInfo nfo = new AppletInfo(input.first == null ? prefix : input.first,
                                unknown, unknown, input.second == null ? unknown : input.second, "", prefix,
                                KeysPresence.UNKNOWN, prefix.equals("A000000003000000") ?
                                GPRegistryEntry.Kind.SecurityDomain : GPRegistryEntry.Kind.Application);
                        logger.info("AID found: " + prefix);
                        found.add(nfo);
                    }
                    return true;
                } else return false; //cut walking, no applet with such prefix exists
            } catch (Exception e) {
                logger.warn("Card threw an error: " + e.getMessage());
                logger.warn("The search on this AID prefix was aborted.");
                return false;
            }
        });
        result = new CardInstanceMetaData(found, new HashMap<>());

        return true;
    }

    private void walkTree(TreeWalkCallback callback) {
        walkTree(applets.getNode().getIncomingEdge().toString(), applets.getNode(), callback);
    }

    private void walkTree(String prefix, Node node, TreeWalkCallback callback) {
        if (node == null) return;

        if (!callback.call(prefix, node)) return;

        for (Node child : node.getOutgoingEdges()) {
            walkTree(prefix + child.getIncomingEdge(), child, callback);
        }
    }

    @Override
    public String getDescription() {
        return "Applets presence detection, based on " + aidList.getName();
    }

    private interface TreeWalkCallback {
        /**
         * Called when walking the tree
         * @param prefix prefix that has been crossed up until now
         * @param at current node in walk
         * @return true if continue to search the tree
         */
        boolean call(String prefix, Node at);
    }
}
