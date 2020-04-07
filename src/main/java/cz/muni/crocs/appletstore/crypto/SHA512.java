package cz.muni.crocs.appletstore.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.io.*;
import java.security.*;

/**
 * SHA512 wrapper, not used (but might be in future, kept therefore)
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class SHA512 implements Hash {

    private static boolean providerSet = false;

    private void checkProvider() throws LocalizedSignatureException {
        if (!providerSet) {
            Security.addProvider(new BouncyCastleProvider());
            providerSet = true;
        }
    }

    private MessageDigest getDigest() throws LocalizedSignatureException {
        checkProvider();
        try {
            return MessageDigest.getInstance("SHA-512", "BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new LocalizedSignatureException("Could not generate hash.", "signature_aborted", e);
        }
    }

    @Override
    public String process(String data) throws LocalizedSignatureException {
        MessageDigest md = getDigest();
        return Hex.toHexString(md.digest(data.getBytes()));
    }

    @Override
    public String process(File file) throws LocalizedSignatureException {
        MessageDigest md = getDigest();
        try (DigestInputStream stream = new DigestInputStream(new FileInputStream(file), md)) {
            stream.on(true);
            int length = 512;
            byte[] data = new byte[length];
            while (stream.read(data, 0, length) != -1);
        } catch (IOException e) {
            throw new LocalizedSignatureException("Failed to open the file.", "signature_aborted", e);
        }
        return Hex.toHexString(md.digest());
    }
}
