package cz.muni.crocs.appletstore.action;

import java.util.Timer;
import java.util.TimerTask;

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

    /**
     * Start with delay
     * @param delay time to delay the start
     */
    default void start(int delay) {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                start();
                t.cancel();
                t.purge();
            }
        }, delay);
    }
}
