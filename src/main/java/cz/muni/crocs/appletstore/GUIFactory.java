package cz.muni.crocs.appletstore;

public class GUIFactory {

    private static GUIComponents components;

    public static GUIComponents Components() {
        if (components == null) components = new GUIComponentsImpl();
        return components;
    }
}
