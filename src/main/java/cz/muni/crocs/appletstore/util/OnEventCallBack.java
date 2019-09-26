package cz.muni.crocs.appletstore.util;

/**
 * On various events, perform various callbacks depending on the action state.
 * @author Jiří Horák
 * @version 1.0
 */
public interface OnEventCallBack<Start, Fail, Finish> {

    /**
     * Called when the event started
     * @return value generated on the event start
     */
    Start onStart();

    /**
     * Called when the even failed
     * @return value generated on the event failure
     */
    Fail onFail();

    /**
     * Called when the even finished
     * @return value generated on the event finish
     */
    Finish onFinish();
}
