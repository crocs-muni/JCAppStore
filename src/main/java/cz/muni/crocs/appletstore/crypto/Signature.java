package cz.muni.crocs.appletstore.crypto;

import cz.muni.crocs.appletstore.util.Tuple;

import java.io.File;
import java.io.IOException;

/**
 * Use various means to verify the signatures of signed aplets
 * keys are implicitly located at:   keybase    nowhere, it is downloaded from keybase server
 *                                   pgp        store: JCAppStore/store/keys/signer.asc
 *                                              custom: JCAppStore/keys
 */
public interface Signature {

    /**
     * Verify the file signature with auto author deduction
     * @param author author of the file
     * @param file path to the file to verify
     * @param fileSignature path to the signature file of the file
     * @return true if signature successful
     */
    boolean verify(String author, String file, String fileSignature) throws LocalizedSignatureException;

    /**
     * Verify the file signature with auto author deduction
     * @param author author of the file
     * @param file file to verify
     * @param fileSignature signature of the file
     * @return true if signature successful
     */
    boolean verify(String author, File file, File fileSignature) throws LocalizedSignatureException;

    /**
     * Verify the file signature
     * @param author author of the file
     * @param file path to the file to verify
     * @param fileSignature path to the signature file of the file
     * @return true if file signature verified
     * @throws LocalizedSignatureException when signature fails for reasons such as wrong parameters, failed to obrain key...
     */
    boolean verifyPGP(String author, String keyID, String file, String fileSignature) throws LocalizedSignatureException;

    /**
     * Verify the file signature
     * @param author author of the file
     * @param file file to verify
     * @param fileSignature the signature file of the file
     * @return true if file signature verified
     * @throws LocalizedSignatureException when signature fails for reasons such as wrong parameters, failed to obtain key...
     */
    boolean verifyPGP(String author, String keyID, File file, File fileSignature) throws LocalizedSignatureException;

    /**
     * Verify the file signature with auto author deduction
     * supposes that the signature is stored within the same directory as 'file' and its name is '[file].sig'
     * takes care of the situation if files do not exist and reproduces the message error
     * verifies the internet connection and returns error message if not accessible
     * @param author author of the signature, to verify that it was really signed by him, not by someone else
     * @param file path to the file to verify
     * @return true if signature successful
     */
    Tuple<String, String> verifyAndReturnMessage(String author, String file);

    /**
     * Verify the file signature with auto author deduction
     * supposes that the signature is stored within the same directory as 'file' and its name is '[file].sig'
     * takes care of the situation if files do not exist and reproduces the message error
     * verifies the internet connection and returns error message if not accessible
     * @param author author of the signature, to verify that it was really signed by him, not by someone else
     * @param file file to verify
     * @return tuple with first = imagename, second = message
     */
    Tuple<String, String> verifyAndReturnMessage(String author, File file);

    /**
     * supposes that the signature is stored within the same directory as 'file' and its name is '[file].sig'
     * takes care of the situation if files do not exist and reproduces the message error
     * verifies the internet connection and returns error message if not accessible
     * @param author author of the file
     * @param file path to the file to verify
     * @return tuple with first = imagename, second = message
     */
    Tuple<String, String> verifyPGPAndReturnMessage(String author, String keyID, String file);

    /**
     * supposes that the signature is stored within the same directory as 'file' and its name is '[file].sig'
     * takes care of the situation if files do not exist and reproduces the message error
     * verifies the internet connection and returns error message if not accessible
     * @param author author of the file
     * @param file file to verify
     * @return tuple with first = imagename, second = message
     */
    Tuple<String, String> verifyPGPAndReturnMessage(String author, String keyID, File file);

}
