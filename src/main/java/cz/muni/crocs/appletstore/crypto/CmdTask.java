package cz.muni.crocs.appletstore.crypto;

import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Command Execution environment
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CmdTask {
    private static final Logger logger = LogManager.getLogger(CmdTask.class);

    protected ArrayList<String> process;
    private File workingDirectory;
    private boolean redirect = false;
    private boolean started = false;

    public CmdTask() throws LocalizedSignatureException {
        process = new ArrayList<>();
        workingDirectory = new File(".");

        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_UNIX) {
            process.add("bash");
            process.add("-c");
        } else if (SystemUtils.IS_OS_WINDOWS) {
            //no action needed
        } else {
            throw new LocalizedSignatureException("Unsupported OS for command execution.", "E_support_cmd");
        }
    }

    public CmdTask add(String command) {
        if (started || command == null) return this;
        process.add(command);
        return this;
    }

    public CmdTask cwd(File cwd) {
        workingDirectory = cwd;
        return this;
    }

    public CmdTask log(boolean isLogged) {
        if (!started) redirect = isLogged;
        return this;
    }

    /**
     * Process the command and return its processed instance
     * the caller is responsible for destroying the process instance
     * blocking call, timeouts after 10 seconds
     *
     * @return process that was executed
     * @throws LocalizedSignatureException on command failure
     */
    public Process process() throws LocalizedSignatureException {
        return process(10);
    }

    /**
     * Process the command and return its processed instance
     * the caller is responsible for destroying the process instance. Blocking call.
     *
     * @param timeoutSec command timeout
     * @return process that was executed
     * @throws LocalizedSignatureException on command failure
     */
    public Process process(int timeoutSec) throws LocalizedSignatureException {
        if (started) throw new LocalizedSignatureException("The process was already executed.");
        try {
            Process result = processUnblocked();
            result.waitFor(timeoutSec, TimeUnit.SECONDS);
            return result;
        } catch (InterruptedException e) {
            //todo add image pgp failure
            throw new LocalizedSignatureException("Failed to fire cmd from line.", "E_operation_cmd", e);
        }
    }

    /**
     * Process the command and return its processed instance
     * the caller is responsible for destroying the process instance
     * no timeout is set - unblocking call.
     *
     * @return process that was executed
     * @throws LocalizedSignatureException on command failure
     */
    public Process processUnblocked() throws LocalizedSignatureException {
        if (started) throw new LocalizedSignatureException("The process was already executed.");

        try {
            logger.info(process.stream().collect(Collectors.joining(" ", ">> ", " [EXEC]")));
            Process p = new ProcessBuilder(process).redirectErrorStream(true)
                    .directory(workingDirectory.getAbsoluteFile()).start();
            if (redirect) {
                //todo probably not working when disabled JFRAME
                redirectToLogger(p.getInputStream());
            }
            started = true;
            return p;
        } catch (IOException e) {
            //todo add image pgp failure
            throw new LocalizedSignatureException("Failed to fire cmd from line.", "E_operation_cmd", e);
        }
    }

    /**
     * Process the command and return its processed instance
     * @return string with all command output
     * @throws LocalizedSignatureException on command failure
     */
    public String processToString() throws LocalizedSignatureException {
        return toString(process());
    }

    /**
     * Parse command output
     * @param process to get the output from
     * @return process output as returned, empty string if process stream closed
     */
    public static String toString(Process process) {
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
            logger.info("Note: this error is not serious, probably just closed stream, this is used for " +
                    "logger to display more info only.");
        }
        process.destroy();
        logger.debug(result);
        return result;
    }

    //from: https://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki
    private static CompletableFuture<Boolean> redirectToLogger(final InputStream inputStream) {
        return CompletableFuture.supplyAsync(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line = null;
                while((line = bufferedReader.readLine()) != null) {
                    CmdTask.logger.log(Level.INFO, line);
                }
                return true;
            } catch (IOException e) {
                return false;
            }
        });
    }
}
