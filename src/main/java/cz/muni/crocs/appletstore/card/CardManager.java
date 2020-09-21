package cz.muni.crocs.appletstore.card;

import apdu4j.ResponseAPDU;
import cz.muni.crocs.appletstore.iface.CallBack;
import pro.javacard.AID;
import pro.javacard.CAPFile;

import javax.smartcardio.CardTerminal;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Card Manager, the component visible from outside of this package
 */
public interface CardManager {

    /**
     * Check if card present
     * @return true if card inserted
     */
    boolean isCard();

    /**
     * Get current card
     * @return card instance if present, null otherwise
     */
    CardInstance getCard();

    /**
     * Set whether the app should try default test key
     * next time the card is attempted to authenticate to
     */
    void setTryGenericTestKey();

    /**
     * Set callback
     * @param call callback called every time an unusual event occurs
     *             that requires the loadCard() call (but cannot be called from loadCard()
     *             as this method is part of the interface itself
     */
    void setCallbackOnFailure(CallBack<Void> call);

    /**
     * Switches to the new aid as store selected applet (not default selected)
     * should not select if card not plugged
     * @param aid AID to select
     */
    void switchAppletStoreSelected(AID aid);

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

    /**
     * Get selected terminal
     * @return currently used CardTerminal instance
     */
    CardTerminal getSelectedTerminal();

    /**
     * Get selected terminal name
     * @return currently used card terminal name
     */
    String getSelectedTerminalName();

    /**
     * Set terminal as used
     * @param name name of the terminal to select
     */
    void setSelectedTerminal(String name);

    /**
     * Get last inserted card descriptor
     * @return card descriptor
     */
    String getLastCardDescriptor();

    /**
     * Get the last installed applet aids
     * @return AIDs of the last installed applets
     */
    String[] getLastAppletInstalledAids();

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
     * @see CardManager::setReloadCard()
     */
    void loadCard() throws LocalizedCardException, UnknownKeyException;

    /**
     * Look into terminals for a card. If state changed, e.g. terminals / cards switched,
     * makes necessary steps to be ready to work with this card. No authorization (to SD) is required.
     */
    void loadCardUnauthorized() throws LocalizedCardException;

    /**
     * Invalidates the card instance data
     */
    void setReloadCard();

    /**
     * Should download a dependencies file from
     * jcalgtest results
     * @return false if the file is already downloaded
     */
    boolean getJCAlgTestDependencies();

    /**
     * Load dependencies from a file
     * @param from file that contains the jcalgtest algorithm support results
     * @param rewrite true to replace if present
     * @return true on successful completition
     */
    boolean loadJCAlgTestDependencies(File from, boolean rewrite) throws LocalizedCardException;

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
     * Uninstall applet from the card
     * @param nfo which applet to uninstall (only the AID is used though)
     * @param force whether the uninstall is forced
     * @throws LocalizedCardException exception with localized text on failure
     */
    void uninstall(AppletInfo nfo, boolean force) throws LocalizedCardException, UnknownKeyException;

    /**
     * Perform SELECT operation
     *
     * @param AID target applet AID
     * @return true if applet present (e.g. select was successful)
     * @throws LocalizedCardException when failed to execute the command transfer
     */
    boolean select(String AID) throws LocalizedCardException;

    /**
     * Send command to applet
     * @param AID target applet AID to send the command to
     * @param APDU commandAPDU to send
     * @return response, or null if failed
     * @throws LocalizedCardException when failed to execute the command transfer
     */
    ResponseAPDU sendApdu(String AID, String APDU) throws LocalizedCardException;
}
