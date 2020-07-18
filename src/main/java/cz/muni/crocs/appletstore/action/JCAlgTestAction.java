package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.iface.OnEventCallBack;

import java.awt.event.MouseEvent;

/**
 * Getting the free card memory action
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class JCAlgTestAction extends CardAbstractAction<Void, byte[]> {

    public JCAlgTestAction(OnEventCallBack<Void, byte[]> call) {
        super(call);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        //todo first install the JCAlgTest if not present (ask user, if metadata - check whether SDK does not clash)

        //todo include client app

        //todo then somehow pass the arguments to cmd client app

        //todo show results and send to MUNI


    }
}
