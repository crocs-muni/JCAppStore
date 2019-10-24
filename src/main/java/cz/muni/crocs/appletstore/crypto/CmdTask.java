package cz.muni.crocs.appletstore.crypto;

import cz.muni.crocs.appletstore.StoreItemInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Command Execution environment
 * @author Jiří Horák
 */
public class CmdTask {
    private static final Logger logger = LogManager.getLogger(CmdTask.class);

    protected ArrayList<String> process;

    public CmdTask() {
        process = new ArrayList<>();
    }

    public CmdTask add(String command) {
        process.add(command);
        return this;
    }

    /**
     * Process the command and return its processed instance
     * the caller is responsible for destroying the process instance
     * @return process that was executed
     * @throws LocalizedSignatureException on command failure
     */
    public Process process() throws LocalizedSignatureException {
        try {
            logger.info(process.stream().collect(Collectors.joining(" ", ">> ", "[ENTER]")));
            Process result = new ProcessBuilder(process).redirectErrorStream(true).start();
            result.waitFor();
            return result;
        } catch (IOException | InterruptedException e) {
            throw new LocalizedSignatureException("Failed to fire cmd from line.", "signature_aborted", e);
        }
    }

    /**
     * Process the command and return its processed instance
     * @return string with all command output
     * @throws LocalizedSignatureException on command failure
     */
    public String processToString() throws LocalizedSignatureException {
        return getStreamOutput(process());
    }

    private static String getStreamOutput(Process process) {
        String result = "";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ( (line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            result = builder.toString();
        } catch (Exception e) {
            logger.error("Couldn't read command output:" , e);
        }
        process.destroy();
        logger.info(result);
        return result;
    }
}
