package cz.muni.crocs.appletstore.action.applet;

/**
 * We need only one instance - applets have package-private constructors.
 */
public class Applets {

    public static JCAlgTest JCALG_TEST = new JCAlgTest();
    public static JCMemory JCMEMORY = new JCMemory();

}
