package cz.muni.crocs.appletstore.card;

import pro.javacard.AID;
import pro.javacard.CAPFile;

import javax.smartcardio.CardTerminal;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface CardManager {

    /**
     * Check if card present
     * @return true if card inserted
     */
    boolean isCard();

    /**
     * Switches to the new aid as selected applet
     * should not select if card not plugged
     * @param aid AID to select
     */
    void switchApplet(AID aid);

    /**
     * Check if any applet selected
     * @return true if any card applet selected
     */
    boolean isAppletSelected();

    /**
     * Check if applet selected
     * @return true if applet with AID provided selected
     */
    boolean isAppletSelected(AID applet);

    /**
     * Get state of the terminal instance
     * @return Terminals.TerminalState value (NO_CARD / NO_READER / OK)
     */
    Terminals.TerminalState getTerminalState();

    /**
     * Return set of connected terminal names
     * @return set of all terminals
     */
    Set<String> getTerminals();
    CardTerminal getSelectedTerminal();
    String getSelectedTerminalName();

    void setSelectedTerminal(String name);

    /**
     * Get applets on card
     * @return applets info list
     */
    List<AppletInfo> getInstalledApplets();

    /**
     * Get card identifier
     * @return card id
     */
    String getCardId();

    /**
     * Get card name and id
     * @return card descriptor
     */
    String getCardDescriptor();

    /**
     * Get last inserted card descriptor
     * @return card descriptor
     */
    String getLastCardDescriptor();

    /**
     * Evaluates the necessity of card refreshing
     * @return 0 no refresh /1 needs refresh no new card /2 needs refresh new card
     */
    int needsCardRefresh();

    /**
     * Look into terminals for a card. If state changed, e.g. terminals / cards switched,
     * makes necessary steps to be ready to work with
     *
     * @link Terminals::checkTerminals()
     */
    void loadCard() throws LocalizedCardException;

    /**
     * Get life cycle of the card
     * @return int, where value determines card state - OP_READY, LOCKED...
     * as designed by GlobalPlatform specification
     */
    Integer getCardLifeCycle();

    /**
     * Set the last applet AID installed
     * @param aid that was installed, null to delete
     */
    void setLastAppletInstalled(AID aid);

    /**
     * Get the last installed applet aid
     * @return AID of the last installed applet
     */
    AID getLastAppletInstalledAid();

    /**
     * Install new applet onto current card
     * @param file file with the applet
     * @param data data from install user, namely 3 items: install params, force install and custom AID
     * @throws LocalizedCardException exception with localized text on failure
     * @throws IOException when the file is incorrect or missing
     */
    void install(File file, InstallOpts data) throws LocalizedCardException, IOException;

    /**
     * Install new applet onto current card
     * @param file file with the applet (already parsed)
     * @param data data from install user, namely 3 items: install params, force install and custom AID
     * @throws LocalizedCardException exception with localized text on failure
     */
    void install(final CAPFile file, InstallOpts data) throws LocalizedCardException;

    /**
     * Uninstall applet from the card
     * @param nfo which applet to uninstall (only the AID is used though)
     * @param force whether the uninstall is forced
     * @throws LocalizedCardException exception with localized text on failure
     */
    void uninstall(AppletInfo nfo, boolean force) throws LocalizedCardException;

    /**
     * Unsupported yet.
     * @throws LocalizedCardException exception with localized text on failure
     */
    void sendApdu(String AID) throws LocalizedCardException;
}
