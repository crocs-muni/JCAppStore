package cz.muni.crocs.appletstore.crypto;

import cz.muni.crocs.appletstore.util.Tuple;

import java.io.File;
import java.io.IOException;

/**
 * Use various means to verify the signatures of signed aplets
 * The signature file name conventions: for JCAppStore  filename.sig
 *                                      for author      filename.author.sig
 */
public interface Signature {

    final String storeAuthor = "JCAppStore";

    default File getSignatureFileFromString(String author, String filename) {
        if (author == null || author.equals(storeAuthor))
            return new File(filename + ".sig");
        return new File(filename + "." + author + ".sig");
    }

    static String getImageByErrorCode(int code) {
        /*todo once can get details about key trust include verify-trust image*/
        switch (code) {
            case 0:
                return "verify.png";
            default:
                return "not_verified.png";
        }
    }


    /**
     * Verify the file signature with auto author deduction using Keybase
     * @param author author of the file
     * @param file path to the file to verify
     * @param fileSignature path to the signature file of the file
     * @return true if signature successful
     */
    boolean verify(String author, String file, String fileSignature) throws LocalizedSignatureException;

    /**
     * Verify the file signature with auto author deduction using Keybase
     * @param author author of the file
     * @param file file to verify
     * @param fileSignature signature of the file
     * @return true if signature successful
     */
    boolean verify(String author, File file, File fileSignature) throws LocalizedSignatureException;

    /**
     * Verify the file signature using PGP
     * @param fingerprint signature key fingerprint or null of store key
     * @param file path to the file to verify
     * @param fileSignature path to the signature file of the file
     * @return true if file signature verified
     * @throws LocalizedSignatureException when signature fails for reasons such as wrong parameters, failed to obrain key...
     */
    boolean verifyPGP(String fingerprint, String file, String fileSignature) throws LocalizedSignatureException;

    /**
     * Verify the file signature
     * @param fingerprint of the signature key or null if store key
     * @param file file to verify
     * @param fileSignature the signature file of the file
     * @return true if file signature verified
     * @throws LocalizedSignatureException when signature fails for reasons such as wrong parameters, failed to obtain key...
     */
    boolean verifyPGP(String fingerprint, File file, File fileSignature) throws LocalizedSignatureException;

    /**
     * Verify the file signature with auto author deduction using Keybase
     * supposes that the signature is stored within the same directory as 'file' and its name is '[file].sig'
     * takes care of the situation if files do not exist and reproduces the message error
     * verifies the internet connection and returns error message if not accessible
     * @param author author of the signature, to verify that it was really signed by him, not by someone else
     * @param file path to the file to verify
     * @return tuple with first = exit code, second = message
     */
    Tuple<Integer, String> verifyAndReturnMessage(String author, String file) throws LocalizedSignatureException;

    /**
     * Verify the file signature with auto author deduction using Keybase
     * supposes that the signature is stored within the same directory as 'file' and its name is '[file].sig'
     * takes care of the situation if files do not exist and reproduces the message error
     * verifies the internet connection and returns error message if not accessible
     * @param author author of the signature, to verify that it was really signed by him, not by someone else
     * @param file file to verify
     * @return tuple with first = exit code, second = message
     */
    Tuple<Integer, String> verifyAndReturnMessage(String author, File file) throws LocalizedSignatureException;

    /**
     * Verify the store signature using PGP
     * supposes that the signature is stored within the same directory as 'file' and its name is '[file].sig'
     * takes care of the situation if files do not exist and reproduces the message error
     * @param file path to the file to verify
     * @return tuple with first = exit code, second = message
     */
    Tuple<Integer, String> verifyPGPAndReturnMessage(String file) throws LocalizedSignatureException;

    /**
     * Verify the store signature using PGP
     * supposes that the signature is stored within the same directory as 'file' and its name is '[file].sig'
     * takes care of the situation if files do not exist and reproduces the message error
     * @param file path to the file to verify
     * @return tuple with first = exit code, second = message
     */
    Tuple<Integer, String> verifyPGPAndReturnMessage(File file) throws LocalizedSignatureException;

    /**
     * Verify the file signature with Keybase: auto author deduction
     * supposes that the signature is stored within the same directory as 'file' and its name is '[file].sig'
     * takes care of the situation if files do not exist and reproduces the message error
     * verifies the internet connection and returns error message if not accessible
     * @param author author of the signature, to verify that it was really signed by him, not by someone else
     * @param file path to the file to verify
     * @return tuple with first = exit code, second = message
     */
    Tuple<Integer, String> verifyAndReturnMessage(String author, String file, String detachedSignature) throws LocalizedSignatureException;

    /**
     * Verify the file signature with Keybase: auto author deduction
     * supposes that the signature is stored within the same directory as 'file' and its name is '[file].sig'
     * takes care of the situation if files do not exist and reproduces the message error
     * verifies the internet connection and returns error message if not accessible
     * @param author author of the signature, to verify that it was really signed by him, not by someone else
     * @param file file to verify
     * @return tuple with first = exit code, second = message
     */
    Tuple<Integer, String> verifyAndReturnMessage(String author, File file, File detachedSignature) throws LocalizedSignatureException;

    /**
     * Verify the store signature using PGP
     * supposes that the signature is stored within the same directory as 'file' and its name is '[file].sig'
     * takes care of the situation if files do not exist and reproduces the message error
     * verifies the internet connection and returns error message if not accessible
     * @param fingerprint signature key fingerprint or null if store signature
     * @param file path to the file to verify
     * @return tuple with first = exit code, second = message
     */
    Tuple<Integer, String> verifyPGPAndReturnMessage(String fingerprint, String file, String detachedSignature) throws LocalizedSignatureException;

    /**
     * Verify the store signature using PGP
     * supposes that the signature is stored within the same directory as 'file' and its name is '[file].sig'
     * takes care of the situation if files do not exist and reproduces the message error
     * verifies the internet connection and returns error message if not accessible
     * @param fingerprint signature key fingerprint or null if store signature
     * @param file file to verify
     * @return tuple with first = exit code, second = message
     */
    Tuple<Integer, String> verifyPGPAndReturnMessage(String fingerprint, File file, File detachedSignature) throws LocalizedSignatureException;
}
