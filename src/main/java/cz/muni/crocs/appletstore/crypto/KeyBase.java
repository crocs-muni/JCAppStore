package cz.muni.crocs.appletstore.crypto;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.apache.commons.lang.SystemUtils;

import java.io.File;

/**
 * Detect Keybase App if installed on the computer & perform signature verification
 * @author Jiří Horák
 */
public class KeyBase extends CmdTask {
    private static boolean verified = false;
    private String keybase;

    public KeyBase() throws LocalizedSignatureException {
        String keybase = OptionsFactory.getOptions().getOption(Options.KEY_KEYBASE_LOCATION);
        if (!verified) {
            if (keybase.isEmpty())
                throw new LocalizedSignatureException("Keybase no path given.", "no_keybase_path");
            if (!new File(keybase).exists())
                throw new LocalizedSignatureException("Keybase not present.", "no_keybase");
            verified = true;
        }
    }

    public boolean verifySignature(File file) throws LocalizedSignatureException {
        return verifySignature(new SHA512().process(file));
    }

    private boolean verifySignature(String data) throws LocalizedSignatureException {
        if (SystemUtils.IS_OS_MAC) {
            //todo run on mac
            return true;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            String hashFromSignature = new CmdTask()
                    .add(keybase).add("verify")
                    .add("-i").add(data)
                    .process();
            return data.equals(hashFromSignature);
        } else if (SystemUtils.IS_OS_UNIX) {
            //todo run on unix
            return true;
        } else return false;
    }

}
