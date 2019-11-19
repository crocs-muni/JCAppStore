package cz.muni.crocs.appletstore.crypto;

import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.util.Tuple;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

public class SignatureImpl implements Signature {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    @Override
    public boolean verify(String author, String file, String fileSignature) throws LocalizedSignatureException {
        File code = new File(file);
        throwIfNotExists(code);
        File signature = new File(fileSignature);
        throwIfNotExists(signature);
        return verify(author, code, signature);
    }

    @Override
    public boolean verify(String author, File file, File fileSignature) throws LocalizedSignatureException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean verifyPGP(String author, String file, String fileSignature) throws LocalizedSignatureException {
        File code = new File(file);
        throwIfNotExists(code);
        File signature = new File(fileSignature);
        throwIfNotExists(signature);
        return verifyPGP(author, code, signature);
    }

    @Override
    public boolean verifyPGP(String author, File file, File fileSignature) throws LocalizedSignatureException {
        return new PGP().verifySignature(author, file, fileSignature);
    }

    @Override
    public Tuple<String, String> verifyAndReturnMessage(String author, String file) throws LocalizedSignatureException {
        return verifyAndReturnMessage(author, new File(file));
    }

    @Override
    public Tuple<String, String> verifyAndReturnMessage(String author, File file) throws LocalizedSignatureException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Tuple<String, String> verifyPGPAndReturnMessage(String author, String file) throws LocalizedSignatureException {
        return verifyPGPAndReturnMessage(author, new File(file));
    }

    @Override
    public Tuple<String, String> verifyPGPAndReturnMessage(String author, File file)  throws LocalizedSignatureException {
        return verifyPGPAndReturnMessage(author, file, getSignatureFileFromString(author, file.getAbsolutePath()));
    }

    @Override
    public Tuple<String, String> verifyAndReturnMessage(String author, String file, String detachedSignature) throws LocalizedSignatureException{
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Tuple<String, String> verifyAndReturnMessage(String author, File file, File detachedSignature) throws LocalizedSignatureException{
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Tuple<String, String> verifyPGPAndReturnMessage(String author, String file, String detachedSignature) throws LocalizedSignatureException {
        return verifyPGPAndReturnMessage(author, new File(file), new File(detachedSignature));
    }

    @Override
    public Tuple<String, String> verifyPGPAndReturnMessage(String author, File file, File detachedSignature) throws LocalizedSignatureException {
        return new PGP().verifySignatureAndGetErrorMsg(author, file, detachedSignature);
    }

    private void throwIfNotExists(File f) throws LocalizedSignatureException {
        if (!f.exists())
            throw new LocalizedSignatureException("No such file: " + f.getAbsolutePath(), "no_file_signature");
    }
}
