package cz.muni.crocs.appletstore.crypto;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.util.Tuple;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

public class SignatureImpl implements Signature {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    @Override
    public boolean verify(String author, String file, String fileSignature) throws LocalizedSignatureException {
        File code = new File(file);
        throwIfNotExists(code);
        File signature = new File(fileSignature);
        throwIfNotExists(signature);
        return verify(author, code, signature);
    }

    @Override
    public boolean verify(String author, File file, File fileSignature) throws LocalizedSignatureException {
        return new KeyBase().verifySignature(author, file, fileSignature);
    }

    @Override
    public boolean verifyPGP(String author, String keyURL, String file, String fileSignature) throws LocalizedSignatureException {
        File code = new File(file);
        throwIfNotExists(code);
        File signature = new File(fileSignature);
        throwIfNotExists(signature);
        return verifyPGP(author, keyURL, code, signature);
    }

    @Override
    public boolean verifyPGP(String author, String keyURL, File file, File fileSignature) throws LocalizedSignatureException {
        //todo implement
        return false;
    }

    @Override
    public Tuple<String, String> verifyAndReturnMessage(String author, String file) {
        return verifyAndReturnMessage(author, new File(file));
    }

    @Override
    public Tuple<String, String> verifyAndReturnMessage(String author, File file) {
        Tuple<String, String> conn = verifyConnectionOrKeyPresence(textSrc.getString("E_no_net_keybase"), author);
        if (conn != null) return conn;

        try {
            String keybase = OptionsFactory.getOptions().getOption(Options.KEY_KEYBASE_LOCATION);
            if (keybase == null || keybase.isEmpty()) {
                return new Tuple<>("not_verified.png", textSrc.getString("no_keybase_path"));
            }
            return new KeyBase().verifySignature(author, file.getAbsolutePath());
        } catch (LocalizedSignatureException e) {
            e.printStackTrace();
            return new Tuple<>("not_verified.png", textSrc.getString("H_verify_failed")
                    + (OptionsFactory.getOptions().getOption(Options.KEY_ERROR_MODE).equals("verbose") ?
                    e.getLocalizedMessage() : e.getLocalizedMessageWithoutCause()));
        }
    }

    @Override
    public Tuple<String, String> verifyPGPAndReturnMessage(String author, String keyURL, String file) {
        return verifyPGPAndReturnMessage(author, keyURL, new File(file));
    }

    @Override
    public Tuple<String, String> verifyPGPAndReturnMessage(String author, String keyURL, File file) {
        //todo implement
        return null;
    }

    private void throwIfNotExists(File f) throws LocalizedSignatureException {
        if (!f.exists())
            throw new LocalizedSignatureException("No such file: " + f.getAbsolutePath(), "no_file_signature");
    }

    private Tuple<String, String> verifyConnectionOrKeyPresence(String errorMsg, String author /*null if not saved as key*/) {
        if (author != null) {
            //if the file with author's key exist, do not require internet
            if (new File(Config.APP_KEY_DIR + Config.S + author + ".asc").exists())
                return null;
        }

        try {
            //todo ugly get the host that we need to connect to
            if (!CmdInternetConnection.isAvailable("https://www.google.com"))
                return new Tuple<>("wifi_off_black.png", errorMsg);
        } catch (LocalizedSignatureException e) {
            return new Tuple<>("wifi_off_black.png", errorMsg);
        }
        return null;
    }
}
