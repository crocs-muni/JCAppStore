package cz.muni.crocs.appletstore.card;

import cz.muni.crocs.appletstore.util.ErrDisplay;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.util.Tuple;
import pro.javacard.gp.ISO7816;

/**
 * SW constants from GPPro, returns error messages that may hint user what went wrong.
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class SW extends ISO7816 {

    public static final short NO_SPECIFIC_DIAGNOSIS = 0x6400;
    public static final short MEMORY_FAIL = 0x6581;
    public static final short CARD_LOCKED = 0x6283;

    public static Tuple<String, ErrDisplay> getErrorCauseKey(int sw, String msgKey) {
       return (OptionsFactory.getOptions().is(Options.KEY_VERBOSE_MODE)) ?
               getErrorVerbose(sw, msgKey) : getError(sw);
    }

    /**
     * Get error description
     * @param sw SW err value
     * @return tuple [String:: message, Boolean:: should the message be displayed over card full screen?]
     */
    public static Tuple<String, ErrDisplay> getError(int sw) {
        switch (sw) {
            case SW_BYTES_REMAINING_00: return responsePopup("");
            case SW_END_OF_FILE: return responsePopup("ES_eof");
            case SW_SECURITY_STATUS_NOT_SATISFIED:
            case SW_LESS_DATA_RESPONDED_THAN_REQUESTED:
            case SW_COMMAND_NOT_ALLOWED:
            case SW_EXPECTED_SM_DATA_OBJECTS_MISSING:
            case SW_SM_DATA_OBJECTS_INCORRECT:
            case SW_RECORD_NOT_FOUND:
            case SW_INCORRECT_P1P2:
            case SW_WRONG_P1P2:
            case MEMORY_FAIL:
            case SW_CORRECT_LENGTH_00:
            case SW_NO_PRECISE_DIAGNOSIS:
            case NO_SPECIFIC_DIAGNOSIS:
            case SW_INS_NOT_SUPPORTED:
            case SW_CLA_NOT_SUPPORTED:
            case SW_KEY_NOT_FOUND:
            case SW_WRONG_LENGTH: return responsePopup("ES_generic");
            case SW_AUTHENTICATION_METHOD_BLOCKED: return responseFullScreen("E_init_failed");
            case SW_WRONG_DATA:
            case SW_DATA_INVALID: return responsePopup("E_invalid_data");
            case SW_CONDITIONS_OF_USE_NOT_SATISFIED: return responsePopup("E_use_failure");
            case SW_KEY_USAGE_ERROR: return responseFullScreen("E_key_usage_error");
            case SW_FUNC_NOT_SUPPORTED: return responseFullScreen("ES_probably_locked");
            case SW_FILE_NOT_FOUND: return responsePopup("E_pkg_not_found");
            case SW_OUT_OF_MEMORY: return responsePopup("E_full_memory");
            case SW_CARD_TERMINATED: return responseFullScreen("H_terminated");
            case SW_NO_ERROR: return responsePopup("OK");
            case CARD_LOCKED: return responsePopup("E_action_denied");
            default: return responsePopup("ES_generic");
        }
    }

    /**
     * Get error _verbose_ description
     * @param sw SW err value
     * @return tuple [String:: message, Boolean:: should the message be displayed over card full screen?]
     */
    public static Tuple<String, ErrDisplay> getErrorVerbose(int sw, String defaultKey) {
        switch (sw) {
            case SW_BYTES_REMAINING_00: return responsePopup("");
            case SW_END_OF_FILE: return responsePopup("E_eof");
            case SW_LESS_DATA_RESPONDED_THAN_REQUESTED: return responsePopup("E_not_enough_data");
            case SW_WRONG_LENGTH: return responsePopup("E_invalid_LC");
            case SW_SECURITY_STATUS_NOT_SATISFIED: return responseFullScreen("E_security_failure");
            case SW_AUTHENTICATION_METHOD_BLOCKED: return responseFullScreen("E_init_failed");
            case SW_WRONG_DATA:
            case SW_DATA_INVALID: return responsePopup("E_invalid_data");
            case SW_CONDITIONS_OF_USE_NOT_SATISFIED: return responsePopup("E_use_failure");
            case SW_COMMAND_NOT_ALLOWED: return responsePopup("E_command_not_allowed");
            case SW_EXPECTED_SM_DATA_OBJECTS_MISSING:
            case SW_SM_DATA_OBJECTS_INCORRECT: return responsePopup("E_generic_invalid_command");
            case SW_KEY_USAGE_ERROR: return responsePopup("E_key_usage_error");
            case SW_FUNC_NOT_SUPPORTED: return responsePopup("E_action_not_supported");
            case SW_FILE_NOT_FOUND: return responsePopup("E_pkg_not_found");
            case SW_RECORD_NOT_FOUND: return responsePopup("E_record_not_found");
            case MEMORY_FAIL: return responsePopup("E_invalid_memory");
            case SW_OUT_OF_MEMORY: return responsePopup("E_full_memory");
            case SW_WRONG_P1P2:
            case SW_INCORRECT_P1P2: return responsePopup("E_invalid_P1P2");
            case SW_KEY_NOT_FOUND: return responsePopup("E_referenced_not_found");
            case SW_CORRECT_LENGTH_00: return responsePopup("E_length_00");
            case SW_INS_NOT_SUPPORTED: return responsePopup("E_invalid_INS");
            case SW_CLA_NOT_SUPPORTED: return responsePopup("E_invalid_CLA");
            case SW_NO_PRECISE_DIAGNOSIS:
            case NO_SPECIFIC_DIAGNOSIS: return responsePopup("E_generic" + defaultKey + "search_SW" + sw + "'.");
            case SW_CARD_TERMINATED: return responseFullScreen("H_terminated");
            case SW_NO_ERROR: return responsePopup("OK");
            case CARD_LOCKED: return responsePopup("E_action_denied");
            default: return responsePopup(defaultKey);
        }
    }

    private static Tuple<String, ErrDisplay> responseFullScreen(String msg) {
        return new Tuple<>(msg, ErrDisplay.FULL_SCREEN);
    }

    private static Tuple<String, ErrDisplay> responsePopup(String msg) {
        return new Tuple<>(msg, ErrDisplay.POPUP);
    }
}