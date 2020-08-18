package cz.muni.crocs.appletstore.iface;

/**
 * On various events, perform various callbacks depending on the action state.
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

    /**
     * Empty event to pass
     * @return empty callback event
     */
    static OnEventCallBack<Void, Void> empty() {
        return new OnEventCallBack<Void, Void>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFail() {

            }

            @Override
            public Void onFinish() {
                return null;
            }

            @Override
            public Void onFinish(Void aVoid) {
                return null;
            }
        };
    }
}
