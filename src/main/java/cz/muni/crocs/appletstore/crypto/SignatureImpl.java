package cz.muni.crocs.appletstore.crypto;

import java.io.File;

public class SignatureImpl implements Signature {

    @Override
    public boolean verify(String file, String fileSignature, String keyFile, String method) throws LocalizedSignatureException {
        File code = new File(file);
        throwIfNotExists(code);
        File signature = new File(fileSignature);
        throwIfNotExists(signature);
        File pubkey = new File(keyFile);
        throwIfNotExists(pubkey);
        return verify(code, signature, pubkey, method);
    }

    private void throwIfNotExists(File f) throws LocalizedSignatureException {
        if (!f.exists())
            throw new LocalizedSignatureException("No such file: " + f.getAbsolutePath(), "no_file_signature");
    }

    @Override
    public boolean verify(File file, File fileSignature, File keyFile, String method) throws LocalizedSignatureException {
        switch (method) {
            case Signature.KEYBASE:
                return verifyKeyBase(file, fileSignature, keyFile);
            default:
                throw new LocalizedSignatureException("No such method supported: " + method, "wrong_signature_method");
        }
    }


    private boolean verifyKeyBase(File file, File fileSignature, File keyFile) {
        return false;
    }
}
