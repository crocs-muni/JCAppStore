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
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private static boolean verified = false;
    private String location;
    private boolean isWarn = false;
    private static Pattern pattern = Pattern.compile("Key fingerprint = ([0-9A-F ]+)");

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
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_UNIX) {
            //todo run on mac
            return true;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            String result = new CmdTask().add(location).add("verify")
                    .add("-d").add("\"" + signatureFile.getAbsolutePath() + "\"")
                    .add("-i").add("\"" + file.getAbsolutePath() + "\"")
                    .processToString();
            isWarn = result.contains("WARNING");
            return result.contains("Good signature") && result.contains(author);
        } else return false;
    }

    Tuple<String, String> verifySignature(String author, String filePath) throws LocalizedSignatureException {
        return verifySignature(author, new File(filePath));
    }


    Tuple<String, String> verifySignature(String author, File file) throws LocalizedSignatureException {
        if (!file.exists())
            return new Tuple<>("verify_no_keybase.png", textSrc.getString("H_no_file_pgp"));
        File sig = new File(file + ".sig");
        if (!sig.exists())
            return new Tuple<>("verify_no_keybase.png", textSrc.getString("H_no_file_pgp"));
        if (verifySignature(author, file, sig)) {
            return (isWarn) ?
                    new Tuple<>("verify_trust.png", textSrc.getString("H_verified_not_trusted") + author)
                    : null;
        } else {
            return new Tuple<>("not_verified.png", textSrc.getString("H_not_verified"));
        }
    }

    //not tested
    private boolean hasKeyInRing(String keyId, String signer) throws LocalizedSignatureException {
        String[] result = new CmdTask().add(location).add("--list-keys")
                .add("-d").processToString().split("\n");
        for (int i = 0; i < result.length; i += 2) {
            if (result[i].contains(keyId) && result[i + 1].contains(signer))
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

    public String getKeyID(File key) throws LocalizedSignatureException {
        if (!key.exists()) return null;
        String res = new CmdTask().add(location).add("--with-fingerprint")
                .add(key.getAbsolutePath()).processToString();
        Matcher m = pattern.matcher(res);
        if (m.find()) {
            //first group is that of all match string, second the () enclosed
            return m.group(1);
        }
        throw new LocalizedSignatureException("Failed to obtain key id.", "");
    }

    public boolean importKey(File key) throws LocalizedSignatureException {
        Process p = new CmdTask().add(location).add("--import")
                .add(key.getAbsolutePath()).process();
        int res = p.exitValue();
        p.destroy();
        return res == 0;
    }

    public Tuple<String, String> importKeyAndGetErrorMessage(File key) {
        try {
            if (importKey(key)) {
                return null;
            }
        } catch (LocalizedSignatureException e) {
            e.printStackTrace();
        }
        return new Tuple<>("no_key.png", textSrc.getString("key_import_fail"));
    }

    public boolean setKeyTrust(String key, int level) throws LocalizedSignatureException {
        level = Math.max(level, 1);
        level = Math.min(level, 5);

        Process p;
        if (SystemUtils.IS_OS_WINDOWS) {
            p = new CmdTask().add("(").add("echo").add("trust")
                    .add("&echo").add(Integer.toString(level))
                    .add("&echo").add("y")
                    .add("&echo").add("quit").add(")")
                    .add("|")
                    .add(location)
                    .add("--command-fd")
                    .add("0").add("--edit-key").add(key).process();

        } else {
            p = new CmdTask().add("echo").add("-e")
                    .add("\"" + level + "\ny\n\"").add("|")
                    .add(location).add("--homedir")
                    .add(".").add("--command-fd").add("0")
                    .add("--expert").add("--edit-key").add(key).process();
        }
        int res = p.exitValue();
        p.destroy();
        return res == 0;
    }

    public Tuple<String, String> setKeyTrustAndGetErroMessage(String key, int level) {
        try {
            if (setKeyTrust(key, level)) {
                return null;
            }
        } catch (LocalizedSignatureException e) {
            e.printStackTrace();
        }
        return new Tuple<>("key_no_trust.png", textSrc.getString("key_trust_not_set"));
    }
}
