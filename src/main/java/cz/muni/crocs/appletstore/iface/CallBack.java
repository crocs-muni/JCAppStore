package cz.muni.crocs.appletstore.iface;

@FunctionalInterface
public interface CallBack<T> {

    /**
     * Enables the callback abstract
     */
    T callBack();
}
