package cz.muni.crocs.appletstore.util;

public class OptionsFactory {

    private static Options<String> opts = null;

    public static Options<String> getOptions() {
        if (opts == null)
            opts = new OptionsImpl();
        return opts;
    }

    private OptionsFactory() {}
}
