package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.HtmlText;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ResourceBundle;

import net.miginfocom.swing.MigLayout;
import org.bouncycastle.util.encoders.Hex;

import static cz.muni.crocs.appletstore.InstallDialogWindow.HEXA_PATTERN;

/**
 * Dialog window panel for sending APDU commands
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class SendAPDUDialogWindow extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private Color wrong = new Color(0xA3383D);
    private JTextField apdu = new JTextField(70);
    //private JCheckBox inHex = new JCheckBox();

    /**
     * Create send APDU panel
     * @param predefinedCommand command to put in if pre-defined
     * @param additionalMsg additional information to show
     */
    public SendAPDUDialogWindow(String predefinedCommand, String additionalMsg) {
        super(new MigLayout("width 250px"));

        if (additionalMsg != null && !additionalMsg.isEmpty()) {
            add(new HtmlText("<p width=\"600px\">" + additionalMsg + "</p>"), "wrap");
        }
        apdu.setText(predefinedCommand);
        //todo has too much overhead with long strings
//        apdu.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                /*if (inHex.isSelected())*/ apdu.setForeground(hasValidData() ? Color.BLACK : wrong);
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                /*if (inHex.isSelected())*/ apdu.setForeground(hasValidData() ? Color.BLACK : wrong);
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                /*if (inHex.isSelected())*/ apdu.setForeground(hasValidData() ? Color.BLACK : wrong);
//            }
//        });
        add(apdu, "wrap");
        add(getHint("H_custom_command", "600"), "span 5, wrap");
        add(getHint("H_valid_command", "600"), "span 5, wrap");

//        inHex.setSelected(true);
//        inHex.setText(textSrc.getString("in_hex"));
//        add(inHex);
    }

    /**
     * Get the command provided/changed by user
     * @return command APDU in hexadecimal string
     */
    public String getCommand() {
//        if (inHex.isSelected()) {
//            return apdu.getText();
//        }
//        return Hex.toHexString(apdu.getText().getBytes()); //todo invalid, how is the byte[] encoded?
        return apdu.getText();
    }

    /**
     * Check if the command is valid
     * @return true if valid
     */
    public boolean hasValidData() {
        return validCommand(apdu);
    }

    private JLabel getHint(String langKey, String width) {
        JLabel hint = new HtmlText("<p width=\"" + width + "\">" + textSrc.getString(langKey) + "</p>", 10f);
        hint.setForeground(Color.DARK_GRAY);
        return hint;
    }

    private static boolean validCommand(JTextComponent field) {
        return validCommand(field.getText());
    }

    private static boolean validCommand(String command) {
        return validHex(command) && validAPDUaLength(command);
    }

    private static boolean validAPDUaLength(String command) {
        return command.length() > 7 && (command.length() == 8 || command.length() == 10 || validDataLength(command));
    }

    private static boolean validDataLength(String command) {
        //todo possible extended APDU has more digits on the LE field
        int dataLen = fromArray(Hex.decode(command.substring(8, 9)), 0, 1);
        int commandValidLength = 2 + 2 + 2 + 2 + 2 + dataLen;
        return command.length() == commandValidLength || command.length() == commandValidLength + 2;
    }

    //todo duplicate code
    private static int fromArray(byte[] array, int offset, int length) {
        int result = 0;
        int newOffset = offset;
        while (newOffset < offset + length) {
            result = (result << 8) + toUnsigned(array[newOffset++]);
        }
        return result;
    }
    //todo duplicate code
    private static int toUnsigned(byte b) {
        if ((b & 0x80) == 0x80) {
            return 128 + (b & 0x7F);
        }
        return b;
    }

    private static boolean validHex(String hex) {
        return hex.isEmpty() || (HEXA_PATTERN.matcher(hex.toLowerCase()).matches() && hex.length() % 2 == 0);
    }
}
