package cz.muni.crocs.appletstore.crypto;

import cz.muni.crocs.appletstore.card.LocalizedCardException;
import org.apache.commons.lang.SystemUtils;

public class CmdInternetConnection {

    public static boolean isAvailable(String host) throws LocalizedSignatureException {
        Process process;
        if ((SystemUtils.IS_OS_MAC) || (SystemUtils.IS_OS_UNIX)) {
            process = new CmdTask().add("ping").add("-c").add("1").add(host).process();
        } else if (SystemUtils.IS_OS_WINDOWS) {
            process = new CmdTask().add("ping").add("-n").add("1").add(host).process();
        } else return false;
        return process.exitValue() == 0;
    }
}
