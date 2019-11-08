package cz.muni.crocs.appletstore.card;

import apdu4j.ResponseAPDU;
import pro.javacard.AID;
import pro.javacard.CAPFile;

import javax.smartcardio.CardTerminal;
import java.io.File;
import java.io.IOException;
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
     * Check if any applet selected by store (not by card)
     * @return true if any card applet selected
     */
    boolean isAppletStoreSelected();

    /**
     * Check if applet selected by store (not by card)
     * @return true if applet with AID provided selected
     */
    boolean isAppletStoreSelected(AID applet);

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
    Set<AppletInfo> getInstalledApplets();

    /**
     * Return applet info associated with AID given
     * @param aid aid to search for
     * @return AppletInfo or null
     */
    AppletInfo getInfoOf(AID aid);

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
    void loadCard() throws LocalizedCardException, UnknownKeyException;

    /**
     * Get life cycle of the card
     * @return int, where value determines card state - OP_READY, LOCKED...
     * as designed by GlobalPlatform specification
     */
    Integer getCardLifeCycle();

    /**
     * Get the last installed applet aid
     * @return AID of the last installed applet
     */
    AID getLastAppletInstalledAid();

    /**
     * Get the default selected applet AID
     * @return null if not default selected applet, AID otherwise
     */
    AID getDefaultSelected();

    /**
     * Set whether
     */
    void setReloadCard();

    /**
     * Set whether the app should try default test key
     * next time the card is attempted to authenticate to
     */
    void setTryGenericTestKey();

    /**
     * Install new applet onto current card
     * @param file file with the applet
     * @param data data from install user, namely 3 items: install params, force install and custom AID
     * @throws LocalizedCardException exception with localized text on failure
     * @throws IOException when the file is incorrect or missing
     */
    void install(File file, InstallOpts data) throws LocalizedCardException, UnknownKeyException, IOException;

    /**
     * Install new applet onto current card
     * @param file file with the applet (already parsed)
     * @param data data from install user, namely 3 items: install params, force install and custom AID
     * @throws LocalizedCardException exception with localized text on failure
     */
    void install(final CAPFile file, InstallOpts data) throws LocalizedCardException, UnknownKeyException;

    /**
     * Install new applet onto current card, makes the applet default selected (e.g. adding Card Reset privilege)
     * @param file file with the applet
     * @param data data from install user, namely 3 items: install params, force install and custom AID
     * @throws LocalizedCardException exception with localized text on failure
     */
    void installAndSelectAsDefault(final File file, InstallOpts data) throws LocalizedCardException, UnknownKeyException, IOException;

    /**
     * Install new applet onto current card, makes the applet default selected (e.g. adding Card Reset privilege)
     * @param file file with the applet (already parsed)
     * @param data data from install user, namely 3 items: install params, force install and custom AID
     * @throws LocalizedCardException exception with localized text on failure
     */
    void installAndSelectAsDefault(final CAPFile file, InstallOpts data) throws LocalizedCardException, UnknownKeyException;

    /**
     * Uninstall applet from the card
     * @param nfo which applet to uninstall (only the AID is used though)
     * @param force whether the uninstall is forced
     * @throws LocalizedCardException exception with localized text on failure
     */
    void uninstall(AppletInfo nfo, boolean force) throws LocalizedCardException, UnknownKeyException;

    /**
     * Send command to applet
     * @param AID target applet AID to send the command to
     * @param APDU commandAPDU to send
     * @return response, or null if failed
     * @throws LocalizedCardException when failed to execute the command transfer
     */
    ResponseAPDU sendApdu(String AID, String APDU) throws LocalizedCardException, UnknownKeyException;
}
