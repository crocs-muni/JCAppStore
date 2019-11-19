package cz.muni.crocs.appletstore.action;

public interface CardAction {

    /**
     * Fire the card action
     * this is only an envelope for mouseClicked() function
     * so that the action is assignable to JButton but it is not
     * necessary to call mouseClicked(null); (the MouseEvent value is ignored)
     */
    void start();

    /**
     * Start with delay
     * @param delay time to delay the start
     */
    void start(int delay);
}
