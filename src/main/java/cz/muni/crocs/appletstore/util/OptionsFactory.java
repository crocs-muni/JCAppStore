package cz.muni.crocs.appletstore.util;

/**
 * Factory patter for options
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class OptionsFactory {

    private static Options<String> opts = null;

    public static Options<String> getOptions() {
        if (opts == null)
            opts = new OptionsImpl();
        return opts;
    }

    private OptionsFactory() {}
}

