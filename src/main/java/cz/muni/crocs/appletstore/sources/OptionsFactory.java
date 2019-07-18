package cz.muni.crocs.appletstore.sources;

public class OptionsFactory {

    private static Options<String> opts = null;

    public static Options<String> getOptions() {
        if (opts == null)
            opts = new OptionImpl();
        return opts;
    }

    private OptionsFactory() {}
}
