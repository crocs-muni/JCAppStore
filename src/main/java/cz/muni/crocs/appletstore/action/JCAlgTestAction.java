package cz.muni.crocs.appletstore.action;

import algtestprocess.SupportTable;
import apdu4j.HexUtils;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.util.LocalizedException;
import cz.muni.crocs.appletstore.action.applet.Applets;
import cz.muni.crocs.appletstore.card.*;
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
import cz.muni.crocs.appletstore.util.URLAdapter;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.ATR;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            InformerFactory.getInformer().showMessage(textSrc.getString("no_card"));
            return;
        }

        final TestForm form = getFormForCardName(manager.getCard().getName());
        if (form == null) return;

        execute(() -> {
            AtomicReference<Process> p = new AtomicReference<>();
            try {
                Applets.JCALG_TEST.perform(mngr -> { //perform() call verifies the JCAlgTest presence
                    try {
                        p.set(runJcAlgTestClient(form));
                    } catch (LocalizedSignatureException esig) {
                        throw LocalizedException.from(esig);
                    }

                    File testResults = getResultsFile(); //call first to delete JCAlgTest logs

                    //todo show results and send to MUNI

                    propagateTestResults(testResults);
                    try {
                        URLAdapter.browse(getHTMLTestResults());
                    } catch (IOException | URISyntaxException ex) {
                        throw new LocalizedCardException("Unable to show the tset results.", "E_jcdia_show", ex);
                    }

                    return null;
                }, textSrc.getString("jc_install_failure"));
            } catch (LocalizedException ex) {
                logger.error("Failed to run JCAlgTest.", ex);
                InformerFactory.getInformer().showMessage(ex.getLocalizedMessage());
            } finally {
                if (p.get() != null) p.get().destroy();
            }
            return null;
        }, "", "");
    }

    private Process runJcAlgTestClient(TestForm form)
            throws LocalizedSignatureException, LocalizedCardException {
        Options<String> opts = OptionsFactory.getOptions();

        if (!verifyJavaPresence(opts.getOption(Options.KEY_JAVA_EXECUTABLE))) {
            throw new LocalizedCardException("No java found.", "E_no_java");
        }

        String cardName = form.getCardName().replaceAll("'", "");
        CmdTask task = new CmdTask().add(opts.getOption(Options.KEY_JAVA_EXECUTABLE)).add("-jar")
                .add(absolutePath(opts.getOption(Options.KEY_JCALGTEST_CLIENT_PATH))).cwd(Config.APP_TEST_DIR).log(true);

        try {
            Process process = task.processUnblocked();
            BufferedOutputStream stdin = new BufferedOutputStream(process.getOutputStream());
            Thread.sleep(50);
            stdin.write(String.format("1%n").getBytes());
            stdin.flush();
            Thread.sleep(1000);
            stdin.write(String.format("%s%n", cardName).getBytes());
            stdin.flush();

            process.waitFor();
            return process;
        } catch (IOException | InterruptedException e) {
            throw new LocalizedCardException("Failed to run JCAlgTest client.", "E_jcdia_fire_client", e);
        }
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

    private boolean verifyJavaPresence(String javaExecutable) throws LocalizedSignatureException, LocalizedCardException {
        Process p = new CmdTask().add(javaExecutable).add("--version").process(5);
        try {
            String output = CmdTask.toString(p);
            if (p.exitValue() != 0 && (output.contains("openjdk") || output.contains("Runtime Environment"))) {

                JFileChooser chooser = FileChooser.getSingleFile(new File(Config.APP_ROOT_DIR));
                chooser.setDialogTitle(textSrc.getString("java_executable"));

                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File jre = chooser.getSelectedFile();
                    if (!jre.exists()) throw new LocalizedCardException("Invalid file.", "E_no_java_executable");

                    return verifyJavaPresence(jre.getAbsolutePath());
                } else return false;
            }
            OptionsFactory.getOptions().addOption(Options.KEY_JAVA_EXECUTABLE, javaExecutable);
            return true;
        } finally {
            if (p != null) p.destroy();
        }
    }

    private File getResultsFile() throws LocalizedCardException {
        File results = scanForResultsFile();
        if (results == null || !results.exists()) {
            JFileChooser chooser = FileChooser.getSingleFile(Config.APP_TEST_DIR,
                    textSrc.getString("jcdia_algtest_files"), "csv");
            chooser.setDialogTitle(textSrc.getString("jcdia_algtest_files"));

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File chosed = chooser.getSelectedFile();
                if (!chosed.exists()) throw new LocalizedCardException("Invalid file.", "E_jcdia_parse_file");
                return chosed;
            } else throw new LocalizedCardException("Invalid file.", "E_jcdia_parse_file");
        }
        return results;
    }

    //also deletes log files...
    private File scanForResultsFile() {
        ATR cardATR = CardManagerFactory.getManager().getCard().getCardATR();
        try (Stream<Path> walk = Files.walk(Paths.get(Config.APP_TEST_DIR.getAbsolutePath()), 1)) {
            List<Path> result = walk.filter(f -> {
                try {
                    File current = f.toFile();
                    if (current.isDirectory()) return false;
                    if (current.getName().endsWith(".csv")) {
                        return true;
                    } else Files.delete(f);
                    return false;
                } catch (IOException e) {
                    return false;
                }
            }).collect(Collectors.toList());
            for (Path path : result) {
                Matcher match = JCAlgTestResultsFinder.ATR_PATTERN.matcher(path.getFileName().toString());
                if (match.find()) {
                    String foundATR = match.group().replaceAll(" |_|:|0x", "");
                    if (cardATR.equals(new ATR(HexUtils.hex2bin(foundATR)))) return path.toFile();
                }
            }
        } catch (IOException e) {
            logger.warn("Scanning for jcalgtest results failed.", e);
            return null;
        }
        return null;
    }

    private boolean propagateTestResults(File results) throws LocalizedCardException {
        return CardManagerFactory.getManager().loadJCAlgTestDependencies(results, true);
    }

    private File getHTMLTestResults() throws IOException {
        String basePath = Config.APP_DATA_DIR.getAbsolutePath();
        String fileName = basePath + Config.S + "AlgTest_html_table.html";
        SupportTable.generateHTMLTable(Config.APP_DATA_DIR.getAbsolutePath() + Config.S);
        return new File(fileName);
    }

    private void shareTestResults(File results) {
        //todo
    }

    private static String absolutePath(String relative) {
        return new File(relative).getAbsolutePath();
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
            add(new HtmlText("<div style='width: 450px'>" + textSrc.getString("jcdia_hint_progress") + "</div>"),
                    "span 2, wrap");
        }

        public String getCardName() {
            return cardName.getText();
        }

        public boolean isShareTestResultsSelected() {
            return sendResult.isSelected();
        }

        private JLabel getHint(String langKey) {
            JLabel hint = new HtmlText("<p width=\"550\">" + textSrc.getString(langKey) + "</p>", 11f);
            hint.setForeground(Color.DARK_GRAY);
            return hint;
        }
    }
}
