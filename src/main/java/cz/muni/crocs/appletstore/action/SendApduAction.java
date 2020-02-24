package cz.muni.crocs.appletstore.action;

import apdu4j.ResponseAPDU;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.SendAPDUDialogWindow;
import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.ui.HtmlText;
import cz.muni.crocs.appletstore.ui.Notice;
import cz.muni.crocs.appletstore.ui.TextField;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import jdk.nashorn.internal.scripts.JO;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.gp.GPRegistryEntry;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class SendApduAction extends MouseAdapter implements CardAction {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private static Logger logger = LoggerFactory.getLogger(SendApduAction.class);

    private OnEventCallBack<Void, ResponseAPDU> call;
    private AppletInfo info;

    public SendApduAction(AppletInfo info, OnEventCallBack<Void, Void> defaultCall) {
        this.call = new OnSendAPDUCallBack(defaultCall);
        this.info = info;
    }

    public void setInfo(AppletInfo info) {
        this.info = info;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (info.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile)
            return;

        final String apduCmd = getAPDU("", null);
        if (apduCmd == null) return;

        call.onStart();
        new Thread(() -> {
            ResponseAPDU response;
            try {
                response = CardManagerFactory.getManager().sendApdu(info.getAid().toString(), apduCmd);
            } catch (Exception ex) {
                logger.warn("Failed to send --select " +
                        info.getAid().toString() + " --apdu " + apduCmd, ex);
                //the command APDU transfer failed due to HW/communication issue (but not necessarily a wrong cmd)
                SwingUtilities.invokeLater(SendApduAction::showFailToSendCommandAPDU);
                SwingUtilities.invokeLater(call::onFail);
                return;
            }
            final ResponseAPDU finalResponse = response;
            SwingUtilities.invokeLater(() -> call.onFinish(finalResponse));
        }).start();
    }

    private String getAPDU(String initialCommand, String additionalMsg) {
        SendAPDUDialogWindow window = new SendAPDUDialogWindow(initialCommand, additionalMsg);
        switch (showDialog(textSrc.getString("send_APDU_to") + info.getName(), window)) {
            case JOptionPane.NO_OPTION:
            case JOptionPane.CLOSED_OPTION:
                return null;
            case JOptionPane.YES_OPTION: //continue
        }
        //todo validation does not work yet
        return window.getCommand();
        //if (window.hasValidData()) return window.getCommand();
        //return getAPDU(window.getCommand(), textSrc.getString("E_custom_coomand_format"));
    }

    private static int showDialog(String title, Object msg) {
        return JOptionPane.showOptionDialog(
                null,
                msg,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(Config.IMAGE_DIR + "info.png"),
                new String[]{textSrc.getString("send"), textSrc.getString("cancel")},
                "error");
    }

    private static void showFailToSendCommandAPDU() {
        InformerFactory.getInformer().showInfo(textSrc.getString("E_custom_command_HW"),
                Notice.Importance.SEVERE, Notice.CallBackIcon.CLOSE, null, 15000);
    }

    @Override
    public void start() {
        mouseClicked(null);
    }

    private static class OnSendAPDUCallBack implements OnEventCallBack<Void, ResponseAPDU> {
        private OnEventCallBack<Void, Void> defaultEvent;

        public OnSendAPDUCallBack(OnEventCallBack<Void, Void> defaultEvent) {
            this.defaultEvent = defaultEvent;
        }

        @Override
        public void onStart() {
            defaultEvent.onStart();
        }

        @Override
        public void onFail() {
            SendApduAction.showFailToSendCommandAPDU();
            defaultEvent.onFail();
        }

        @Override
        public Void onFinish() {
            throw new RuntimeException("Should not be called.");
        }

        @Override
        public Void onFinish(ResponseAPDU response) {
            if (response.getSW() == 0x9000) {
                showMessage(getSucceedReturnMsg(response), textSrc.getString("custom_command_ok"), "done.png");
            } else {
                showMessage(getErrorReturnMsg(response), textSrc.getString("custom_command_fail") +
                        Integer.toHexString(response.getSW()), "close.png");
            }
            return defaultEvent.onFinish();
        }

        private void showMessage(Object msg, String title, String image) {
            JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE,
                    new ImageIcon(Config.IMAGE_DIR + image));
        }

        private Object getErrorReturnMsg(ResponseAPDU apdu) {
            String error = textSrc.getString(SW.getErrorVerbose(apdu.getSW(), "custom_command_fail_generic"));
            return new HtmlText("<p width=\"300px\">" +  error + "</p>");
        }

        private Object getSucceedReturnMsg(ResponseAPDU apdu) {
            if (apdu.getData() == null || apdu.getData().length == 0) {
                return new HtmlText("<p width=\"300px\">" +  textSrc.getString("custom_command_no_data") + "</p>");
            }
            //todo add some more data like panel with text Return data:
            return TextField.getTextField(Hex.toHexString(apdu.getData()));
        }
    }
}
