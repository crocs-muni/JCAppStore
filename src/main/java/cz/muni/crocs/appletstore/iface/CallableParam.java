package cz.muni.crocs.appletstore.iface;

@FunctionalInterface
public interface CallableParam<TRet, TArg> {

    /**
     * Enables the callback abstract
     */
    TRet callBack(TArg param);
}
