package cz.muni.crocs.appletstore.crypto;

import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.util.Tuple;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CMD Task extension to talk to GnuPG
 *
 * Used commands
 * export public:   gpg --armor --export you@example.com > you.asc
 * sign file:       gpg --output file.sig --detach-sign file
 * import & verify: if ! gpg --list-keys <keyID> do gpg --import key.asc else gpg --verify file.sig file
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class PGP extends CmdTask {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private static Logger logger = LoggerFactory.getLogger(CmdTask.class);

    private static boolean verified = false;
    private static String location;

    /**
     * Verifies whether GnuPG installed for the first time when run
     * @throws LocalizedSignatureException when no GnuPG installed or found
     * (e.g. 'gpg' not in $PATH and no custom path specified)
     */
    public PGP() throws LocalizedSignatureException {
        String fromSettings = OptionsFactory.getOptions().getOption(Options.KEY_PGP_LOCATION);
        if (!verified) {
            try {
                if (fromSettings == null || fromSettings.isEmpty()) {
                    location = "gpg";
                    if (new CmdTask().add(location).add("--version").process().exitValue() != 0) {
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

    /**
     * Invalidates the GnuPG installation - next time, gpg existence is verified again
     */
    public static void invalidate() {
        verified = false;
    }

    /**
     * Verify signature
     * @param fingerprint - key identifier or null
     * @param file file to verify
     * @param signatureFile detached signature file
     * @return 0 if ok, 1 if invalid, 2 if error occured
     */
    int verifySignature(String fingerprint, File file, File signatureFile) throws LocalizedSignatureException {
        int counter = 0;
        while (!Files.isReadable(file.toPath()) || !Files.isReadable(signatureFile.toPath())) {
            try {
                Thread.currentThread().wait(500);
                counter++;
                if (counter > 6) return 3;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        Process sig;
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_UNIX) {
            sig = new CmdTask().add("bash").add("-c").add(location + " --verify \'"
                    + signatureFile.getAbsolutePath() + "\' \'" + file.getAbsolutePath() + "\'")
                    .process();

        } else if (SystemUtils.IS_OS_WINDOWS) {
            sig = new CmdTask().add(location).add("--verify")
                    .add("\"" + signatureFile.getAbsolutePath() + "\"")
                    .add("\"" + file.getAbsolutePath() + "\"")
                    .process();
        } else return 2;
        String output = CmdTask.toString(sig);
        logger.info(output);
        output = output.replaceAll("\\s", "");

        if (fingerprint == null) {
            return (output.contains(OptionsFactory.getOptions().getOption(Options.KEY_STORE_FINGERPRINT)))
                    ? sig.exitValue() : 1;
        }
        return (output.contains(fingerprint.replaceAll("\\s", ""))) ? sig.exitValue() : 1;
    }

    /**
     * Just a wrapper that assesses int output from verifySignature()
     * @return tuple: first: forwards exit code value; second: string message to display
     */
    Tuple<Integer, String> verifySignatureAndGetErrorMsg(String fingerprint, File file, File signatureFile)
            throws LocalizedSignatureException {
        if (!file.exists() || !signatureFile.exists())
            return new Tuple<>(3, textSrc.getString("H_no_file_pgp"));
        int exitCode = verifySignature(fingerprint, file, signatureFile);
        switch (exitCode) {
            case 0: {
                if (fingerprint == null) {
                    return new Tuple<>(exitCode, textSrc.getString("H_verified_no_author"));
                /*TODO cannot find out missing trust set on the key, so the message will be
                   "succeeded" even though key not trusted*/
                 /*new Tuple<>("verify_trust.png", textSrc.getString("H_verified_no_author") +
                        textSrc.getString("H_verified_not_trusted"))*/
                } else {
                    return new Tuple<>(exitCode, textSrc.getString("H_verified_key") + fingerprint);
                    /*TODO cannot find out missing trust set on the key*/
                /*new Tuple<>("verify_trust.png", textSrc.getString("H_verified") + author +
                        textSrc.getString("H_verified_not_trusted")) */
                }
            }
            case 1: return new Tuple<>(exitCode, textSrc.getString("H_not_verified"));
            case 3: return new Tuple<>(exitCode, textSrc.getString("H_unable_to_read"));
            default: return new Tuple<>(exitCode, textSrc.getString("H_signature_failed"));
        }
    }
}
