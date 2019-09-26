package cz.muni.crocs.appletstore;

public interface ProcessModifiable<State> {

    /**
     * Set state to the class that is modifiable by process
     * @param state state to set, depending of the process result
     */
    public void setState(State state);

    /**
     * Message to give from process to the parent class
     * @param msg message to pass
     */
    public void setProcessMessage(String msg);
}
