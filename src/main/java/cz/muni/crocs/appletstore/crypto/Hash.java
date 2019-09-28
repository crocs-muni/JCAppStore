package cz.muni.crocs.appletstore.crypto;

import java.io.File;

public interface Hash {

    /**
     * Return hash of given data
     * @param data data to generate hash from
     * @return hash of data
     */
    String process(String data) throws LocalizedSignatureException;

    /**
     * Return hash of given data
     * @param file file to generate hash from
     * @return hash of data
     */
    String process(File file) throws LocalizedSignatureException;
}
