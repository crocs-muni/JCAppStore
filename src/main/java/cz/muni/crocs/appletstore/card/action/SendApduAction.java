package cz.muni.crocs.appletstore.card.action;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import pro.javacard.gp.GPRegistryEntry;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class SendApduAction extends MouseAdapter {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private AppletInfo info;
    private final OnEventCallBack<Void, Void, Void> call;

    public SendApduAction(AppletInfo info, OnEventCallBack<Void, Void, Void> call) {
        this.call = call;
        this.info = info;
    }

    public void setInfo(AppletInfo info) {
        this.info = info;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (info.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile)
            return;
        int result = JOptionPane.showConfirmDialog(
                null,
                "TODO" /*todo create insert-apdu pane*/,
                textSrc.getString("send_APDU_to") + info.getName(),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                new ImageIcon(Config.IMAGE_DIR + "info.png"));

        switch (result) {
            case JOptionPane.NO_OPTION:
            case JOptionPane.CLOSED_OPTION:
                return;
            case JOptionPane.YES_OPTION: //continue
        }
        SwingUtilities.invokeLater(call::onStart);
        //CardManager.getInstance().sendRawAPDU();
        SwingUtilities.invokeLater(call::onFinish);
    }
}
