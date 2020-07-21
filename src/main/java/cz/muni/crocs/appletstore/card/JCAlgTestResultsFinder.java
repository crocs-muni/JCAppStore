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
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JCAlgTestResultsFinder implements ProcessTrackable {
    private static final Logger logger = LoggerFactory.getLogger(JCAlgTestResultsFinder.class);

    public static final Pattern ATR_PATTERN = Pattern.compile("[0-9a-fA-F]{2}(( |_|:|0x)[0-9a-fA-F]{2}){2,}");

    private int progress = 0;
    private String message = "";
    private final CardInstanceImpl card;

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
        logger.info("Running a JCAlgtest Results Finder: CARD " + card.getId());
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

            ATR cardATR = card.getCardATR();
            if (cardATR == null) {
                logger.warn("Unknown ATR.");
                return;
            }

            updateProgress(50);
            JsonArray files = root.getAsJsonArray();

            int i = 0;
            for (JsonElement f : files) {
                JsonObject file = f.getAsJsonObject();
                String filename = file.get("name").getAsString();
                Matcher match = ATR_PATTERN.matcher(filename);
                if (match.find()) {
                    String foundATR = match.group().replaceAll(" |_|:|0x", "");

                    //download the file from GITHUB and load the test results
                    if (cardATR.equals(new ATR(HexUtils.hex2bin(foundATR)))) {
                        logger.info("Found file: " + filename);
                        Future<Boolean> task = executor.submit(() ->
                                processURL(file.get("download_url").getAsString()));
                        try {
                            if (task.get(150, TimeUnit.SECONDS)) {
                                logger.info("Found results.");
                                return;
                            }
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
     * Parse the output from a file
     * @param file file that contains the jcalgtest algorithm support results
     * @return true when successfully completed
     */
    public static boolean parseFile(File file) throws LocalizedCardException {
        CardInstanceImpl card = (CardInstanceImpl)CardManagerFactory.getManager().getCard();
        HashMap<String, HashMap<String, String>> data = new HashMap<>();

        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                if (!parseFromStreamReader(reader, data)) return false;
            }
        } catch(IOException ex) {
            throw new LocalizedCardException("Unable to load data from the file.", "E_jcdia_parse_file", ex);
        }

        if (!validSDK(data, card)) return false;
        save(data, card);
        return true;
    }

    /**
     * Parse JCALgTest results CSV file
     * @param URL url from github API
     * @return true if processed correctly
     */
    private boolean processURL(String URL) {
        HashMap<String, HashMap<String, String>> data = new HashMap<>();

        try {
            java.net.URL url = new URL(URL);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                if (!parseFromStreamReader(reader, data)) return false;
            }
        } catch(IOException ex) {
            logger.warn("Failed to load card test data.", ex);
            return false;
        }

        if (!validSDK(data, card)) return false;
        save(data, card);
        return true;
    }

    private static boolean parseFromStreamReader(BufferedReader reader, HashMap<String, HashMap<String, String>> output)
            throws IOException {
        HashMap<String, String> currentData = new HashMap<>();
        String line;

        //header
        output.put("Header", currentData);
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
        if (line == null) return false; //no body

        do {
            String[] separated;
            line = line.trim();
            if (line.length() <= 0) {
                line = reader.readLine();
                if (line == null) break;
                separated = line.split(";");
                currentData = new HashMap<>();
                output.put(separated[0].trim(), currentData);
            } else {
                separated = line.split(";");
            }

            //skip 1-valued or less
            if (separated.length < 2) continue;
            currentData.put(separated[0].trim(), separated[1].trim());
        } while ((line = reader.readLine()) != null);
        return true;
    }

    private static boolean validSDK(HashMap<String, HashMap<String, String>> data, CardInstanceImpl card) {
        String atr = data.get("Header").get("Card ATR");
        return atr != null && atr.length() >= 2 && card.getDetails().getAtr().equals(
                new ATR(HexUtils.hex2bin(atr.replaceAll(" |_|:|0x", ""))));
    }

    private static void save(HashMap<String, HashMap<String, String>> data, CardInstanceImpl card) {
        card.getCardMetadata().setJCData(data);
        try {
            card.saveInfoData();
            logger.info("JCAlgtest data saved.");
        } catch (LocalizedCardException e) {
            logger.error("Failed to save smart card metadata after successful jcalgtest database match.", e);
        }
    }
}
