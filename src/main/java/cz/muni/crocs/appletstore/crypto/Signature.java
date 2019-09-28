package cz.muni.crocs.appletstore.crypto;

import java.io.File;
import java.io.IOException;

/**
 * Use various means to verify the signatures of signed aplets
 *
 */
public interface Signature {

    String KEYBASE = "keybase";

    /**
     * Verify the file signature
     * @param file path to the file to verify
     * @param fileSignature path to the signature file of the file
     * @param keyFile path to the key to verify with
     * @param method method to use for signature verification
     * @return true if signature successful
     */
    boolean verify(String file, String fileSignature, String keyFile, String method) throws LocalizedSignatureException;

    /**
     * Verify the file signature
     * @param file file to verify
     * @param fileSignature signature of the file
     * @param keyFile key to verify with
     * @param method method to use for signature verification
     * @return true if signature successful
     */
    boolean verify(File file, File fileSignature, File keyFile, String method) throws LocalizedSignatureException;

}
