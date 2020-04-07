package cz.muni.crocs.appletstore.util;

@FunctionalInterface
public interface CallBack<T> {

    /**
     * Enables the callback abstract
     */
    T callBack();
}
