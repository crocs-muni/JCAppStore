package cz.muni.crocs.appletstore.crypto;

import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.util.Tuple;
import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * IF IMPLEMENTING DEFINE THE TRANSLATION STRINGS
 * Detect Keybase App if installed on the computer & perform signature verification
 * <p>
 * keybase id aiosa
 * keybase [pgp] sign -i file.cap --detached > file.cap.sig   || only me
 * keybase [pgp] verify -d file.cap.sig -i file.cap
 *
 * @author Jiří Horák
 */
public class KeyBase extends CmdTask {
//    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());
//
//    private static boolean verified = false;
//    private String keybase;
//
//    public KeyBase() throws LocalizedSignatureException {
//        keybase = OptionsFactory.getOptions().getOption(Options.KEY_KEYBASE_LOCATION);
//        if (!verified) {
//            if (keybase == null || keybase.isEmpty())
//                throw new LocalizedSignatureException("Keybase no path given.", "no_keybase_path");
//            if (!new File(keybase).exists())
//                throw new LocalizedSignatureException("Keybase not present.", "no_keybase");
//            verified = true;
//        }
//    }
//
//    boolean verifySignature(String author, File file, File signatureFile) throws LocalizedSignatureException {
//        if (SystemUtils.IS_OS_MAC) {
//            //todo run on mac
//            return true;
//        } else if (SystemUtils.IS_OS_WINDOWS) {
//            return new CmdTask().add("\"" + keybase + "\"").add("verify")
//                    .add("-d").add("\"" + signatureFile.getAbsolutePath() + "\"")
//                    .add("-i").add("\"" + file.getAbsolutePath() + "\"")
//                    .processToString().contains("Signed by " + author);
//        } else if (SystemUtils.IS_OS_UNIX) {
//            //todo run on unix
//            return true;
//        } else return false;
//    }
//
//    Tuple<String, String> verifySignature(String author, String filePath) throws LocalizedSignatureException {
//        return verifySignature(author, new File(filePath));
//    }
//
//
//    Tuple<String, String> verifySignature(String author, File file) throws LocalizedSignatureException {
//        if (!file.exists())
//            return new Tuple<>("verify_no_keybase.png", "H_no_file_keybase");
//        File sig = new File(file + ".sig");
//        if (!sig.exists())
//            return new Tuple<>("verify_no_keybase.png", "H_no_file_keybase");
//        if (verifySignature(author, file, sig)) {
//            return new Tuple<>("verify.png", textSrc.getString("H_verified") + "JCAppStore"); //todo get author
//        } else {
//            return new Tuple<>("not_verified.png", textSrc.getString("H_not_verified"));
//        }
//    }
}
