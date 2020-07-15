package cz.muni.crocs.appletstore.card;

import apdu4j.HexUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.iface.ProcessTrackable;
import cz.muni.crocs.appletstore.util.GitHubApiGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.ATR;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JCAlgTestResultsFinder implements ProcessTrackable {
    private static final Logger logger = LoggerFactory.getLogger(JCAlgTestResultsFinder.class);
    private static final Pattern atrPattern = Pattern.compile("[0-9a-fA-F]{2}(( |_|:|0x)[0-9a-fA-F]{2})*");


    private int progress = 0;
    private String message = "";
    private CardInstanceImpl card;

    public JCAlgTestResultsFinder(CardInstance of) {
        this.card = (CardInstanceImpl)of;
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public void updateProgress(int amount) {
        progress = amount;
    }

    @Override
    public int getMaximum() {
        return 100;
    }

    @Override
    public String getInfo() {
        return message;
    }

    @Override
    public void setLoaderMessage(String msg) {
        message = msg;
    }

    @Override
    public void run() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<JsonElement> future = executor.submit(() ->
                    GitHubApiGetter.getJsonContents(Config.JCALGTEST_RESULTS_DIR));
            JsonElement root = null;

            try {
                root = future.get(30, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                logger.warn("Timeout: could not obtain jcalgtest folder json content description.", e);
                future.cancel(true);
                return;
            } catch (InterruptedException | ExecutionException e) {
                logger.warn("Execution of getting jcalgtest folder json interrupted.", e);
                return;
            }

            if (root == null) return;
            ATR cardATR = card.getDetails().getAtr();
            if (cardATR == null) return;

            updateProgress(50);
            JsonArray files = root.getAsJsonArray();

            int i = 0;
            for (JsonElement f : files) {
                JsonObject file = f.getAsJsonObject();
                String filename = file.get("name").getAsString();
                Matcher match = atrPattern.matcher(filename);
                if (match.matches()) {
                    String foundATR = match.group().replaceAll(" |_|:|0x", "");

                    //download the file from GITHUB and load the test results
                    if (cardATR.equals(new ATR(HexUtils.hex2bin(foundATR)))) {
                        Future<Boolean> task = executor.submit(() ->
                                processFile(file.get("download_url").getAsString()));
                        try {
                            if (task.get(150, TimeUnit.SECONDS)) return;
                        } catch (TimeoutException e) {
                            logger.warn("Timeout: failed to load file: " + filename, e);
                            future.cancel(true);
                        } catch (InterruptedException | ExecutionException e) {
                            logger.warn("Execution of getting jcalgtest file interrupted: " + filename, e);
                        }
                    }
                }
                safeSetProgress(progress * (files.size() / ++i));
            }
        } finally {
            updateProgress(getMaximum());
            executor.shutdownNow();
        }
    }

    /**
     * Parse JCALgTest results CSV file
     * @param URL url from github API
     * @return true if processed correctly
     */
    private boolean processFile(String URL) {
        HashMap<String, HashMap<String, String>> data = new HashMap<>();

        try {
            java.net.URL url = new URL(URL);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                HashMap<String, String> currentData = new HashMap<>();
                String line;

                //header
                data.put("Header", currentData);
                reader.readLine(); //skip 1st line
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() <= 0) {
                        break;
                    }
                    String[] separated = line.split(";");
                    if (separated.length < 2) continue;
                    currentData.put(separated[0].trim(), separated[1].trim());
                }
                if (line == null) return false; //no body?

                currentData = new HashMap<>();
                do {
                    String[] separated;

                    line = line.trim();
                    if (line.length() <= 0) {
                        line = reader.readLine();
                        if (line == null) break;
                        separated = line.split(";");
                        data.put(separated[0].trim(), currentData);
                        currentData = new HashMap<>();
                    } else {
                        separated = line.split(";");
                    }

                    //skip 1-valued or less
                    if (separated.length < 2) continue;
                    currentData.put(separated[0].trim(), separated[1].trim());
                } while ((line = reader.readLine()) != null);
            }
        } catch(IOException ex) {
            logger.warn("Failed to load card test data.", ex);
        }

        String atr = data.get("Header").get("Card ATR");
        if (atr == null || atr.length() < 2 || !card.getDetails().getAtr().equals(new ATR(HexUtils.hex2bin(atr)))) {
            return false;
        }

        card.getCardMetadata().setJcAlgTestData(data);
        return true;
    }
}
