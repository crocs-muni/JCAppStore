package cz.muni.crocs.appletstore.action;


/**
 * Card action abstraction. These actions are invoked from buttons or manually in code and creates a layer
 * between GUI and card manager
 */
public interface CardAction {

    /**
     * Fire the card action
     * this is only an envelope for mouseClicked() function
     * so that the action is assignable to JButton but it is not
     * necessary to call mouseClicked(null); (the MouseEvent value is ignored)
     */
    void start();
}
