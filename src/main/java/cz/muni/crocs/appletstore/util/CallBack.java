package cz.muni.crocs.appletstore.util;

/**
 * @author Jiří Horák
 * @version 1.0
 */
@FunctionalInterface
public interface CallBack<T> {

    /**
     * Enables the callback abstract
     */
    T callBack();
}
