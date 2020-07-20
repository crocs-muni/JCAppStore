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
import cz.muni.crocs.appletstore.ui.HtmlText;
import cz.muni.crocs.appletstore.ui.Text;
import cz.muni.crocs.appletstore.ui.Title;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.*;

/**
 * Getting the free card memory action
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class JCAlgTestAction extends CardAbstractAction<Void, byte[]> {
    private static final Logger logger = LoggerFactory.getLogger(JCAlgTestAction.class);

    public JCAlgTestAction(OnEventCallBack<Void, byte[]> call) {
        super(call);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        CardManager manager = CardManagerFactory.getManager();
        if (manager.getCard() == null) {
            //todo  error
         return;
        }

        final TestForm form = getFormForCardName(manager.getCard().getName());
        if (form == null) return;

        execute(() -> {
            try {
                //perform() call verified the JCAlgTest presence
                JPanel result = Applets.JCALG_TEST.perform(mngr -> {
                    try {
                        Process p = runJcAlgTestClient(form, mngr);
                    } catch (LocalizedSignatureException localizedSignatureException) {
                        localizedSignatureException.printStackTrace();
                    }

                    //todo show results and send to MUNI

                    return null;
                }, textSrc.getString("jc_install_failure"));
            } catch (UnknownKeyException | LocalizedCardException ex) {
                logger.error("Failed to run JCAlgTest.", ex);
                InformerFactory.getInformer().showMessage(ex.getLocalizedMessage());
            }
            return null;
        }, "", "");
    }

    private Process runJcAlgTestClient(TestForm form, CardManager manager) throws LocalizedSignatureException {
        Options<String> opts = OptionsFactory.getOptions();

        if (!verifyJavaPresence(opts.getOption(Options.KEY_JAVA_EXECUTABLE))) return null;

        String cardName = form.getCardName() + ", " + manager.getCard().getId();

 //       String input = "( echo 1 && echo " + cardName + " ) | ";

//        CmdTask task;
//
//        //todo test on unix
//        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_UNIX) {
//            task = new CmdTask().add("bash").add("-c").add(input).add(opts.getOption(Options.KEY_JAVA_EXECUTABLE))
//                    .add("-jar").add(opts.getOption(Options.KEY_JCALGTEST_CLIENT_PATH));
//        } else if (SystemUtils.IS_OS_WINDOWS) {
//            task = new CmdTask().add(input).add(opts.getOption(Options.KEY_JAVA_EXECUTABLE))
//                    .add("-jar").add(opts.getOption(Options.KEY_JCALGTEST_CLIENT_PATH));
//        }

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

        //todo send
    }

    private TestForm getFormForCardName(String defaultName) {
        TestForm form = new TestForm(defaultName);
        if (JOptionPane.showOptionDialog(null, form, textSrc.getString("jcdia_title"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, new ImageIcon(),
                new String[]{textSrc.getString("jcdia_confirm"), textSrc.getString("cancel")},
                textSrc.getString("cancel")) == JOptionPane.YES_OPTION) {
            return form;
        }
        return null;
    }

    private boolean verifyJavaPresence(String javaExecutable) throws LocalizedSignatureException {
        String cmdPrefix = (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_UNIX) ? "bash -c" : "";
        Process p = new CmdTask().add(cmdPrefix).add(javaExecutable).add("--version").process(5);
        String output = CmdTask.toString(p);
        if (p.exitValue() != 0 && (output.contains("openjdk") || output.contains("Runtime Environment"))) {

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

    private static class TestForm extends JPanel {
        private static final String NOTICE_COLOR = "#ffcc99";

        private final JTextField cardName = new JTextField(30);
        private final JCheckBox sendResult = new JCheckBox();

        public TestForm(String cardNameAsDefined) {
            super(new MigLayout());

            add(new Title(textSrc.getString("jcdia_title"), new ImageIcon(
                    Config.IMAGE_DIR + "creditcard-exclamation.png"), 18f, SwingConstants.CENTER), "span 2, wrap");
            add(new HtmlText("<div style='width: 450px'>" + textSrc.getString("jcdia_intro") + "</div>"),
                    "span 2, wrap");

            add(new Text(textSrc.getString("jcdia_card_name")), "span 2, gaptop 10, wrap");
            cardName.setText(cardNameAsDefined);
            add(cardName, "span 2 , wrap");
            add(getHint("H_jcdia_card_name"), "span 2, wrap");

            sendResult.setSelected(true);
            add(sendResult);
            add(new Text(textSrc.getString("jcdia_send")), "wrap");
            add(getHint("H_jcdia_send"), "span 2, wrap");

            add(new HtmlText("<div style='padding: 8px; background: " + NOTICE_COLOR + "; width: 450px'>" +
                    textSrc.getString("jcdia_warn") + "</div>"), "span 2, gaptop 7, wrap");
        }

        public String getCardName() {
            return cardName.getText();
        }

        public boolean isShareTestResultsSelected() {
            return sendResult.isSelected();
        }

        private JLabel getHint(String langKey) {
            JLabel hint = new HtmlText("<p width=\"550\">" + textSrc.getString(langKey) + "</p>", 10f);
            hint.setForeground(Color.DARK_GRAY);
            return hint;
        }
    }
}
