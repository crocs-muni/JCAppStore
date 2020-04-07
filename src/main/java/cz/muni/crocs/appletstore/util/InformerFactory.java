package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Informable;

import java.security.InvalidParameterException;

/**
 * Factory patter for informer
 *
 * @author Jiří Horák
 * @version 1.0
 */
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
