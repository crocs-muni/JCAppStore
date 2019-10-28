package cz.muni.crocs.appletstore.util;

/**
 * On various events, perform various callbacks depending on the action state.
 * @author Jiří Horák
 * @version 1.0
 */
public interface OnEventCallBack<Finish, FinArg> {

    /**
     * Called when the event started
     */
    void onStart();

    /**
     * Called when the even failed
     */
    void onFail();

    /**
     * Called when the even finished
     * @return value generated on the event finish
     */
    Finish onFinish();

    /**
     * Called when the even finished
     * @param arg generated on the event finish
     * @return otuput based on arg
     */
    Finish onFinish(FinArg arg);
}
