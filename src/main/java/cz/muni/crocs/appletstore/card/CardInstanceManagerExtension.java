package cz.muni.crocs.appletstore.card;


import cz.muni.crocs.appletstore.card.command.GPCommand;
import pro.javacard.AID;
import javax.smartcardio.CardException;
import java.util.List;

/**
 * Card instance extension, for manager only (simulates parckage-private methods, available to manager only,
 * outside CardInstance is visible only)
 */
interface CardInstanceManagerExtension extends CardInstance {

    /**
     * Get Card details instance
     * @return card details instance
     */
    CardDetails getDetails();

    /**
     * Set default selected applet of the card
     * @param defaultSelected applet that is default selected on card
     */
    void setDefaultSelected(AID defaultSelected);

    /**
     * Save card info data to non-volatile storage
     * @throws LocalizedCardException upon saving failure
     */
    void saveInfoData() throws LocalizedCardException;

    /**
     * Save applet list data
     * @param toSave list of applets metadata on card
     * @throws LocalizedCardException upon saving failure
     */
    void saveInfoData(List<AppletInfo> toSave) throws LocalizedCardException;

    /**
     * Delete a package from metadata list. Must save the operation (e.g. call saveInfoData())
     * @param pkg pkg which AID is compared to the appletInfo list on card and deleted upon AID equality
     * @throws LocalizedCardException upon saving failure
     */
    void deletePackageData(final AppletInfo pkg) throws LocalizedCardException;

    /**
     * Delete an applet from metadata list. Must save the operation (e.g. call saveInfoData())
     * @param applet applet which AID is compared to the appletInfo list on card and deleted upon AID equality
     * @throws LocalizedCardException upon saving failure
     */
    void deleteAppletData(final AppletInfo applet, boolean force) throws LocalizedCardException;

    /**
     * Executes any desired command without establishing secure channel
     * @param commands commands to execute
     * @throws LocalizedCardException unable to perform command
     * @throws CardException unable to perform command
     */
    void executeCommands(GPCommand<?>... commands) throws LocalizedCardException, CardException;

    /**
     * Executes any desired command using secure channel
     * @param commands commands to execute
     * @throws CardException unable to perform command
     */
    void secureExecuteCommands(GPCommand<?>... commands) throws LocalizedCardException, CardException;

    /**
     * Sets card metadata
     * @param metadata instantiated metadata, filled with card applets metadata and card meta info
     * @see CardInstanceMetaData
     */
    void setMetaData(CardInstanceMetaData metadata);

    /**
     * Disable JCAlgTestFinder for current card session
     */
    void disableTemporarilyJCAlgTestFinder();

    /**
     * Check whether card requests JCAlfgTestFinder
     * @return true when requested
     */
    boolean shouldJCAlgTestFinderRun();
}
