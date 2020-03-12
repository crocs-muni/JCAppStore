package cz.muni.crocs.appletstore.crypto;

import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.util.Tuple;
import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
PGP: RSA 4096
export public:  gpg --armor --export you@example.com > you.asc
sign file:      gpg --output file.sig --detach-sign file
verify:         if ! gpg --list-keys <keyID> do gpg --import key.asc else gpg --verify file.sig file
 */
public class PGP extends CmdTask {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private static boolean verified = false;
    private static String location;
    private boolean isWarn = false;
    private static Pattern pattern = Pattern.compile("Key fingerprint = ([0-9A-F ]+)");

    public PGP() throws LocalizedSignatureException {
        String fromSettings = OptionsFactory.getOptions().getOption(Options.KEY_PGP_LOCATION);
        if (!verified) {
            try {
                if (fromSettings == null || fromSettings.isEmpty()) {
                    location = "gpg";
                    if (!new CmdTask().add(location).add("--help").processToString().contains("Copyright")) {
                        //todo add image gnupg not present
                        throw new LocalizedSignatureException("GnuPG not present.", "no_pgp");
                    }
                } else {
                    location = fromSettings;
                    if (!new File(location).exists())
                        throw new LocalizedSignatureException("GnuPG not present.", "no_pgp");
                }
                verified = true;
            } catch (LocalizedSignatureException e) {
                throw new LocalizedSignatureException("GnuPG not present.", "no_pgp");
            }

        }
    }

    public static void invalidate() {
        verified = false;
    }

    boolean verifySignature(String author, File file, File signatureFile) throws LocalizedSignatureException {
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_UNIX) {
            String result = new CmdTask().add("bash").add("-c").add(location + " --verify \'"
                    + signatureFile.getAbsolutePath() + "\' \'" + file.getAbsolutePath() + "\'")
                    .processToString();
            isWarn = result.contains("WARNING");
            return result.contains("Good signature") && (author == null || result.contains(author));
        } else if (SystemUtils.IS_OS_WINDOWS) {
            String result = new CmdTask().add(location).add("--verify")
                    .add("\"" + signatureFile.getAbsolutePath() + "\"")
                    .add("\"" + file.getAbsolutePath() + "\"")
                    .processToString();
            isWarn = result.contains("WARNING");
            return result.contains("Good signature") && (author == null || result.contains(author));
        } else return false;
    }

    Tuple<String, String> verifySignatureAndGetErrorMsg(String author, File file, File signatureFile) throws LocalizedSignatureException {
        if (!file.exists() || !signatureFile.exists())
            return new Tuple<>("not_verified.png", textSrc.getString("H_no_file_pgp"));

        if (verifySignature(author, file, signatureFile)) {
            if (author == null) {
                return (isWarn) ? new Tuple<>("verify_trust.png", textSrc.getString("H_verified_no_author") +
                        textSrc.getString("H_verified_not_trusted"))
                        : new Tuple<>("verify_trust.png", textSrc.getString("H_verified_no_author"));
            } else {
                return (isWarn) ? new Tuple<>("verify_trust.png", textSrc.getString("H_verified") + author +
                        textSrc.getString("H_verified_not_trusted"))
                        : new Tuple<>("verify.png", textSrc.getString("H_verified") + author);
            }
        } else {
            return new Tuple<>("not_verified.png", textSrc.getString("H_not_verified"));
        }
    }

    //not tested
//    private boolean hasKeyInRing(String keyId, String signer) throws LocalizedSignatureException {
//        String[] result = new CmdTask().add(location).add("--list-keys")
//                .add("-d").processToString().split("\n");
//        for (int i = 0; i < result.length; i += 2) {
//            if (result[i].contains(keyId) && result[i + 1].contains(signer))
//                return true;
//        }
//        return false;
//    }

//    public String getKeyID(File key) throws LocalizedSignatureException {
//        if (!key.exists()) return null;
//        String res = new CmdTask().add(location).add("--with-fingerprint")
//                .add(key.getAbsolutePath()).processToString();
//        Matcher m = pattern.matcher(res);
//        if (m.find()) {
//            //first group is that of all match string, second the () enclosed
//            return m.group(1);
//        }
//        throw new LocalizedSignatureException("Failed to obtain key id.", "");
//    }
}
