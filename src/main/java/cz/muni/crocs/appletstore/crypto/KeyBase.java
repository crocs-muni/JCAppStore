package cz.muni.crocs.appletstore.crypto;

import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.util.Tuple;
import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Detect Keybase App if installed on the computer & perform signature verification
 * <p>
 * keybase id aiosa
 * keybase [pgp] sign -i file.cap --detached > file.cap.sig   || only me
 * keybase [pgp] verify -d file.cap.sig -i file.cap
 *
 * @author Jiří Horák
 */
public class KeyBase extends CmdTask {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private static boolean verified = false;
    private String keybase;

    public KeyBase() throws LocalizedSignatureException {
        keybase = OptionsFactory.getOptions().getOption(Options.KEY_KEYBASE_LOCATION);
        if (!verified) {
            if (keybase.isEmpty())
                throw new LocalizedSignatureException("Keybase no path given.", "no_keybase_path");
            if (!new File(keybase).exists())
                throw new LocalizedSignatureException("Keybase not present.", "no_keybase");
            verified = true;
        }
    }

    public boolean verifySignature(File file, File signatureFile) throws LocalizedSignatureException {
        if (!(file.exists() && signatureFile.exists())) {
            //todo consider throw to send cause to user
            return false;
        }

        if (SystemUtils.IS_OS_MAC) {
            //todo run on mac
            return true;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return new CmdTask().add("\"" + keybase + "\"").add("verify")
                    .add("-d").add("\"" + signatureFile.getAbsolutePath() + "\"")
                    .add("-i").add("\"" + file.getAbsolutePath() + "\"")
                    .processToString().startsWith("Signed by aiosa");
            //todo now works only for aiosa user
        } else if (SystemUtils.IS_OS_UNIX) {
            //todo run on unix
            return true;
        } else return false;
    }

    /**
     * Gui-friendly version of the verify signature implementation, not much portable
     *
     * @param filePath path to file to verify the signature from, expects the signature to be in
     *                 the same folder as filepath and be in form [filepath].sig
     * @return image name for icon and description - designed for GUI JLabel / HintLabel
     */
    public Tuple<String, String> verifySignature(String filePath) throws LocalizedSignatureException {
        String keybase = OptionsFactory.getOptions().getOption(Options.KEY_KEYBASE_LOCATION);
        if (keybase == null || keybase.isEmpty())
            return new Tuple<>("verify_no_keybase.png", "H_no_keybase");

        return verifySignature(new File(filePath));
    }

    /**
     * Gui-friendly version of the verify signature implementation, not much portable
     *
     * @param file file to verify the signature from, expects the signature to be in
     *             the same folder as filepath and be in form [filepath].sig
     * @return image name for icon and description - designed for GUI JLabel / HintLabel
     */
    public Tuple<String, String> verifySignature(File file) throws LocalizedSignatureException {
        if (verifySignature(file, new File(file + ".sig"))) {
            return new Tuple<>("verify.png", textSrc.getString("H_verified") + "JCAppStore");
        } else {
            return new Tuple<>("not_verified.png", textSrc.getString("H_not_verified"));
        }
    }
}
