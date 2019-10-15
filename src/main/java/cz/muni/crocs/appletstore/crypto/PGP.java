package cz.muni.crocs.appletstore.crypto;

import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.util.Tuple;
import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

/*
PGP: RSA 4096
export public:  gpg --armor --export you@example.com > you.asc
sign file:      gpg --output file.sig --detach-sign file
verify:         if ! gpg --list-keys <keyID> do gpg --import key.asc else gpg --verify file.sig
 */
public class PGP extends CmdTask {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private static boolean verified = false;
    private String location;

    public PGP() throws LocalizedSignatureException {
        location = OptionsFactory.getOptions().getOption(Options.KEY_PGP_LOCATION);
        if (!verified) {
            if (location == null || location.isEmpty()) {
                location = getPGPPath();
                OptionsFactory.getOptions().addOption(Options.KEY_PGP_LOCATION, location);
            }
            if (!new File(location).exists())
                throw new LocalizedSignatureException("Keybase not present.", "no_pgp");
            verified = true;
        }
    }

    boolean verifySignature(String author, File file, File signatureFile) throws LocalizedSignatureException {
        if (!hasKeyInRing()) {
            //todo ask to import
        }


        if (SystemUtils.IS_OS_MAC) {
            //todo run on mac
            return true;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            String result = new CmdTask().add("\"" + location + "\"").add("verify")
                    .add("-d").add("\"" + signatureFile.getAbsolutePath() + "\"")
                    .add("-i").add("\"" + file.getAbsolutePath() + "\"")
                    .processToString();

            return new CmdTask().add("\"" + keybase + "\"").add("verify")
                    .add("-d").add("\"" + signatureFile.getAbsolutePath() + "\"")
                    .add("-i").add("\"" + file.getAbsolutePath() + "\"")
                    .processToString().contains("Signed by " + author);
        } else if (SystemUtils.IS_OS_UNIX) {
            //todo run on unix
            return true;
        } else return false;
    }

    Tuple<String, String> verifySignature(String author, String filePath) throws LocalizedSignatureException {
        return verifySignature(author, new File(filePath));
    }


    Tuple<String, String> verifySignature(String author, File file) throws LocalizedSignatureException {
        if (!file.exists())
            return new Tuple<>("verify_no_keybase.png", "H_no_file_keybase");
        File sig = new File(file + ".sig");
        if (!sig.exists())
            return new Tuple<>("verify_no_keybase.png", "H_no_file_keybase");
        if (verifySignature(author, file, sig)) {
            return new Tuple<>("verify.png", textSrc.getString("H_verified") + "JCAppStore"); //todo get author
        } else {
            return new Tuple<>("not_verified.png", textSrc.getString("H_not_verified"));
        }
    }

    private boolean hasKeyInRing(String keyId, String signer) throws LocalizedSignatureException {
        String[] result = new CmdTask().add("\"" + location + "\"").add("--list-keys")
                .add("-d").processToString().split("\n");
        for (int i = 0; i < result.length; i += 2) {
            if (result[i].contains(keyId) && result[i+1].contains(signer))
                return true;
        }
        return false;
    }

    private static String getPGPPath() throws LocalizedSignatureException {
        String env = System.getenv("PATH");
        if (env == null || env.isEmpty()) throw new LocalizedSignatureException("GnuPG no path given.", "no_pgp_path");
        String[] paths = env.split(System.getProperty("path.separator"));
        for (String path : paths) {
            if (path.contains("GnuPG"))
                return path;
        }
        throw new LocalizedSignatureException("GnuPG no path given.", "no_pgp_path");
    }
}
