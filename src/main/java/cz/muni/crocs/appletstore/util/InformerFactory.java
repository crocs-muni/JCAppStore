package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.iface.Informable;

import java.security.InvalidParameterException;

public class InformerFactory {

    private static Informer informer = null;

    public static Informer getInformer() {
        if (informer == null)
            throw new InvalidParameterException("The informer was not initialized with proper context.");
        return informer;
    }

    public static Informer setInformer(Informable context) {
        if (informer == null)
            informer = new InformerImpl(context);
        return informer;
    }

    private InformerFactory() {}
}
