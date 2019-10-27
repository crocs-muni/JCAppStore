package cz.muni.crocs.appletstore.card.action;

import cz.muni.crocs.appletstore.util.OnEventCallBack;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import java.awt.event.MouseEvent;

public class GetFreeMemory extends CardAction {

    public GetFreeMemory(OnEventCallBack<Void, Void, Void> call) {
        super(call);
    }

    GetFreeMemory(CardExecutable executable) {
        super(new OnEventCallBack<Void, Void, Void>() {
            @Override
            public Void onStart() {
                return null;
            }

            @Override
            public Void onFail() {
                return null;
            }

            @Override
            public Void onFinish() {
                return null;
            }
        });
    }

    @Override
    public void mouseClicked(MouseEvent e) {


        if (!OptionsFactory.getOptions().keepJCMemory()) {

        }
    }
}
