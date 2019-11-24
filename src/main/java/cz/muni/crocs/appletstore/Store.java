package cz.muni.crocs.appletstore;

public interface Store {

    enum State {
        UNINITIALIZED, NO_CONNECTION, WORKING, OK, INSTALLING, REBUILD, FAILED, TIMEOUT
    }

    /**
     * Message to give from process to the parent class
     * @param msg message to pass
     */
    void setProcessMessage(String msg);
}
