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

/*
PGP: RSA 4096
export public:  gpg --armor --export you@example.com > you.asc
sign file:      gpg --output file.sig --detach-sign file
verify:         if ! gpg --list-keys <keyID> do gpg --import key.asc else gpg --verify file.sig file
 */
public class PGP extends CmdTask {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private static Logger logger = LoggerFactory.getLogger(CmdTask.class);

    private static boolean verified = false;
    private static String location;

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

    public static void invalidate() {
        verified = false;
    }

    /**
     * Verify signature
     * @param author author - signature key owner identifier as showin in PGP or null
     * @param file file to verify
     * @param signatureFile detached signature file
     * @return 0 if ok, 1 if invalid, 2 if error occured
     */
    int verifySignature(String author, File file, File signatureFile) throws LocalizedSignatureException {
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

        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_UNIX) {
            Process sig = new CmdTask().add("bash").add("-c").add(location + " --verify \'"
                    + signatureFile.getAbsolutePath() + "\' \'" + file.getAbsolutePath() + "\'")
                    .process();
            String output = CmdTask.toString(sig);
            logger.info(output);
            return (author == null || output.contains(author)) ? sig.exitValue() : 1;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            Process sig = new CmdTask().add(location).add("--verify")
                    .add("\"" + signatureFile.getAbsolutePath() + "\"")
                    .add("\"" + file.getAbsolutePath() + "\"")
                    .process();
            String output = CmdTask.toString(sig);
            logger.info(output);
            return (author == null || output.contains(author)) ? sig.exitValue() : 1;
        } else return 2;
    }

    Tuple<Integer, String> verifySignatureAndGetErrorMsg(String author, File file, File signatureFile) throws LocalizedSignatureException {
        if (!file.exists() || !signatureFile.exists())
            return new Tuple<>(3, textSrc.getString("H_no_file_pgp"));
        int exitCode = verifySignature(author, file, signatureFile);
        switch (exitCode) {
            case 0: {
                if (author == null) {
                    return new Tuple<>(exitCode, textSrc.getString("H_verified_no_author"));
                /*TODO cannot find out missing trust set on the key, so the message will be
                   "succeeded" even though key not trusted*/
                 /*new Tuple<>("verify_trust.png", textSrc.getString("H_verified_no_author") +
                        textSrc.getString("H_verified_not_trusted"))*/
                } else {
                    return new Tuple<>(exitCode, textSrc.getString("H_verified") + author);
                    /*TODO cannot find out missing trust set on the key*/
                /*new Tuple<>("verify_trust.png", textSrc.getString("H_verified") + author +
                        textSrc.getString("H_verified_not_trusted")) */
                }
            }
            case 1: {
                return new Tuple<>(exitCode, textSrc.getString("H_not_verified"));
            }
            case 3: {
                return new Tuple<>(exitCode, textSrc.getString("H_unable_to_read"));
            }
            default: {
                return new Tuple<>(exitCode, textSrc.getString("H_signature_failed"));

            }
        }
    }
}
