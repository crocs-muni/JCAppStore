package cz.muni.crocs.appletstore.crypto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Jiří Horák
 */
public class CmdTask {
    protected ArrayList<String> process;

    public CmdTask() {
    }

    public CmdTask add(String command) {
        process.add(command);
        return this;
    }

    public String process() throws LocalizedSignatureException {
        Process proc = null;
        try {
            proc = new ProcessBuilder(process).start();
        } catch (IOException e) {
            throw new LocalizedSignatureException("Failed to fire cmd from line.", "signature_aborted", e);
        }
        return getStreamOutput(proc);
    };

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
            System.out.println("Couldn't read command output:" + e);
        }
        return result;
    }
}
