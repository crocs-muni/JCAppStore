package cz.muni.crocs.appletstore.action;

import apdu4j.CommandAPDU;
import apdu4j.HexUtils;
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
 * Action to send custom APDU command
 *
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

    /**
     * Set applet info to send the command to
     * @param info AppletInfo that contains applet AID to send the command to
     */
    public void setInfo(AppletInfo info) {
        this.info = info;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (info.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile)
            return;

        final String apduCmd = getAPDU("", null);
        if (apduCmd == null) return;

        String error = parse(HexUtils.stringToBin(apduCmd));
        if (error != null) {
            InformerFactory.getInformer().showInfoToClose(error, Notice.Importance.SEVERE, 25000);
            return;
        }

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
        //todo sanity checks on the command
        return window.getCommand();
        //if (window.hasValidData()) return window.getCommand();
        //return getAPDU(window.getCommand(), textSrc.getString("E_custom_coomand_format"));
    }

    /**
     * Command APDU encoding options:
     *
     * case 1:  |CLA|INS|P1 |P2 |                                 len = 4
     * case 2s: |CLA|INS|P1 |P2 |LE |                             len = 5
     * case 3s: |CLA|INS|P1 |P2 |LC |...BODY...|                  len = 6..260
     * case 4s: |CLA|INS|P1 |P2 |LC |...BODY...|LE |              len = 7..261
     * case 2e: |CLA|INS|P1 |P2 |00 |LE1|LE2|                     len = 7
     * case 3e: |CLA|INS|P1 |P2 |00 |LC1|LC2|...BODY...|          len = 8..65542
     * case 4e: |CLA|INS|P1 |P2 |00 |LC1|LC2|...BODY...|LE1|LE2|  len =10..65544
     *
     * LE, LE1, LE2 may be 0x00.
     * LC must not be 0x00 and LC1|LC2 must not be 0x00|0x00
     *
     * @return null if ok, error message otherwise
     *
     * Copied out from CommandAPDU.java for better performance and message handling (translation).
     * Modified for translated parsing.
     *  @author  Andreas Sterbenz
     *  @author  JSR 268 Expert Group
     */
    private String parse(byte[] apdu) {
        if (apdu.length < 4) {
            return textSrc.getString("E_APDU_1");
        }
        if (apdu.length == 4) { // case 1
            return null;
        }
        int l1 = apdu[4] & 0xff;
        if (apdu.length == 5) { // case 2s
            return null;
        }
        if (l1 != 0) {
            if (apdu.length == 4 + 1 + l1) { // case 3s
                return null;
            } else if (apdu.length == 4 + 2 + l1) { // case 4s
                return null;
            } else {
               return textSrc.getString("E_APDU_PREFIX") + apdu.length
                                + textSrc.getString("E_APDU_SUFFIX") + l1;
            }
        }
        if (apdu.length < 7) {
            return textSrc.getString("E_APDU_PREFIX") + apdu.length
                            + textSrc.getString("E_APDU_SUFFIX") + l1;
        }
        int l2 = ((apdu[5] & 0xff) << 8) | (apdu[6] & 0xff);
        if (apdu.length == 7) { // case 2e
            return null;
        }
        if (l2 == 0) {
            return textSrc.getString("E_APDU_PREFIX") + apdu.length
                            + textSrc.getString("E_APDU_SUFFIX") + l2
                            + textSrc.getString("E_APDU_NOTE");
        }
        if (apdu.length == 4 + 3 + l2) { // case 3e
            return null;
        } else if (apdu.length == 4 + 5 + l2) { // case 4e
            return null;
        } else {
            return textSrc.getString("E_APDU_PREFIX") + apdu.length
                            + textSrc.getString("E_APDU_SUFFIX") + l2
                            + textSrc.getString("E_APDU_NOTE");
        }
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
            return TextField.getTextField(Hex.toHexString(apdu.getData()));
        }
    }
}
