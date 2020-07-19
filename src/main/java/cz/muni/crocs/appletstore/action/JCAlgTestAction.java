package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.action.applet.AppletBase;
import cz.muni.crocs.appletstore.action.applet.Applets;
import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import cz.muni.crocs.appletstore.card.UnknownKeyException;
import cz.muni.crocs.appletstore.crypto.CmdTask;
import cz.muni.crocs.appletstore.crypto.LocalizedSignatureException;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import cz.muni.crocs.appletstore.ui.FileChooser;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.apache.commons.lang.SystemUtils;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.*;

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
        if (CardManagerFactory.getManager().getCard() == null) {
            //todo  error
         return;
        }

        try {
            JPanel result = Applets.JCALG_TEST.perform(manager -> {
                //we _don't_ call the card explicitly, but now we know it is installed: run client

                Process p = runJcAlgTestClient();

                //todo show results and send to MUNI

                return null;
            });
        } catch (UnknownKeyException | LocalizedCardException ex) {
            ex.printStackTrace();
        }
    }

    private Process runJcAlgTestClient() throws LocalizedSignatureException {
        Options<String> opts = OptionsFactory.getOptions();
        CardManager manager = CardManagerFactory.getManager();


        if (!verifyJavaPresence(opts.getOption(Options.KEY_JAVA_EXECUTABLE))) return null;


        CmdTask task;

        String cardName = getFormForCardName(manager.getCard().getName()) + ", " + manager.getCard().getId();
        String input = "( echo 1 && echo " + cardName + " ) | ";

        //todo test on unix
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_UNIX) {
            task = new CmdTask().add("bash").add("-c").add(input).add(opts.getOption(Options.KEY_JAVA_EXECUTABLE))
                    .add("-jar").add(opts.getOption(Options.KEY_JCALGTEST_CLIENT_PATH));
        } else if (SystemUtils.IS_OS_WINDOWS) {
            task = new CmdTask().add(input).add(opts.getOption(Options.KEY_JAVA_EXECUTABLE))
                    .add("-jar").add(opts.getOption(Options.KEY_JCALGTEST_CLIENT_PATH));
        }

        ProcessBuilder builder = new ProcessBuilder( opts.getOption(Options.KEY_JAVA_EXECUTABLE), "-jar",
                opts.getOption(Options.KEY_JCALGTEST_CLIENT_PATH));
        builder.directory(Config.APP_DATA_DIR.getAbsoluteFile());
        builder.redirectErrorStream(true);

        try {
            Process process = builder.start();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            writer.write(String.format("1%n"));
            writer.write(String.format("%s%n", cardName));
            process.waitFor();
            return process;
        } catch (IOException | InterruptedException e) {
            //todo notify
            return null;
        }
    }

    //todo bigger form with more information (safety, duration, sent to MUNI etc.)
    private String getFormForCardName(String defaultName) {
        JTextField field = new JTextField(defaultName);
        if (JOptionPane.showOptionDialog(null, field, textSrc.getString("ask_for_card_specifier"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, new ImageIcon(Config.IMAGE_DIR + "creditcard.png"),
                new String[]{textSrc.getString("ok"), textSrc.getString("cancel")}, textSrc.getString("ok")) == JOptionPane.YES_OPTION) {
            return field.getText();
        }
        return null;
    }

    private boolean verifyJavaPresence(String javaExecutable) throws LocalizedSignatureException {
        //todo also verify whether the output contains some specific value (e.g. java might be different program...)

        String cmdPrefix = (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_UNIX) ? "bash -c" : "";
        if (new CmdTask().add(cmdPrefix).add(javaExecutable).add("--version").process(5).exitValue() != 0) {

            JFileChooser chooser = FileChooser.getSingleFile(new File(Config.APP_ROOT_DIR));
            chooser.setDialogTitle(textSrc.getString("java_executable"));

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File jre = chooser.getSelectedFile();
                if (!jre.exists()) {
                    InformerFactory.getInformer().showMessage(textSrc.getString("E_no_java_executable"));
                    return false;
                }
                return verifyJavaPresence(jre.getAbsolutePath());
            } else return false;
        }
        OptionsFactory.getOptions().addOption(Options.KEY_JAVA_EXECUTABLE,javaExecutable);
        return true;
    }
}
