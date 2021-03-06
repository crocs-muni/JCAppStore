package cz.muni.crocs.appletstore.crypto;

import cz.muni.crocs.appletstore.util.Tuple;

import java.io.File;

/**
 * Verification implementation
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class SignatureImpl implements Signature {

    @Override
    public boolean verify(String author, String file, String fileSignature) throws LocalizedSignatureException {
        File code = new File(file);
        throwIfNotExists(code);
        File signature = new File(fileSignature);
        throwIfNotExists(signature);
        return verify(author, code, signature);
    }

    @Override
    public boolean verify(String author, File file, File fileSignature) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean verifyPGP(String fingerprint, String file, String fileSignature) throws LocalizedSignatureException {
        File code = new File(file);
        throwIfNotExists(code);
        File signature = new File(fileSignature);
        throwIfNotExists(signature);
        return verifyPGP(fingerprint, code, signature);
    }

    @Override
    public boolean verifyPGP(String author, File file, File fileSignature) throws LocalizedSignatureException {
        return new PGP().verifySignature(author, file, fileSignature) == 0;
    }

    @Override
    public Tuple<Integer, String> verifyAndReturnMessage(String author, String file) {
        return verifyAndReturnMessage(author, new File(file));
    }

    @Override
    public Tuple<Integer, String> verifyAndReturnMessage(String author, File file) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Tuple<Integer, String> verifyPGPAndReturnMessage(String file) throws LocalizedSignatureException {
        return verifyPGPAndReturnMessage(new File(file));
    }

    @Override
    public Tuple<Integer, String> verifyPGPAndReturnMessage(File file)  throws LocalizedSignatureException {
        return verifyPGPAndReturnMessage(null, file,
                getSignatureFileFromString("JCAppStore", file.getAbsolutePath()));
    }

    @Override
    public Tuple<Integer, String> verifyAndReturnMessage(String author, String file, String detachedSignature) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Tuple<Integer, String> verifyAndReturnMessage(String author, File file, File detachedSignature) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Tuple<Integer, String> verifyPGPAndReturnMessage(String fingerprint, String file, String detachedSignature)
            throws LocalizedSignatureException {
        return verifyPGPAndReturnMessage(fingerprint, new File(file), new File(detachedSignature));
    }

    @Override
    public Tuple<Integer, String> verifyPGPAndReturnMessage(String fingerprint, File file, File detachedSignature)
            throws LocalizedSignatureException {
        return new PGP().verifySignatureAndGetErrorMsg(fingerprint, file, detachedSignature);
    }

    private void throwIfNotExists(File f) throws LocalizedSignatureException {
        if (!f.exists())
            throw new LocalizedSignatureException("No such file: " + f.getAbsolutePath(), "no_file_signature");
    }
}
