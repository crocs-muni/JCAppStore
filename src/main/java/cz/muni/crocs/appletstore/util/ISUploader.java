package cz.muni.crocs.appletstore.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cz.muni.crocs.appletstore.Config;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Uploading a content to MUNI information system
 *
 * Trascribed from python script made by Daniel Zaťovič
 *  into java (to remove python dependency)
 *
 *  with help of https://stackoverflow.com/questions/3324717/sending-http-post-request-in-java
 */
public class ISUploader {
    private final String uco;
    private final HashMap<String, String> headers = new HashMap<>();
    private final ArrayList<Tuple<String, String>> params = new ArrayList<>();

    public ISUploader(String userAgent, String uco) {
        this.uco = uco;
        headers.put("User-Agent", userAgent);
        params.add(new Tuple<>("vybos_vzorek_last", ""));
        params.add(new Tuple<>("vybos_vzorek", uco));
        params.add(new Tuple<>("vybos_hledej", "Vyhledat osobu"));
    }

    public ISUploader(String uco) {
        this("JCAppStore v" + Config.VERSION, uco);
    }

    public boolean upload(String filename) throws IOException {
        return upload(new File(filename));
    }

    public boolean upload(File file) throws IOException {
        if (file.length() > 1024 * 1024) throw new IOException("Invalid file: too big. Limit is 1 MB.");

        HttpURLConnection con = null;
        try {
            URL url = new URL("https://is.muni.cz/dok/depository_in");

            con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Cache-Control", "no-cache");

            //headers
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                con.addRequestProperty(entry.getKey(), entry.getValue());
            }

            //params
            StringBuilder paramsBuilder = new StringBuilder();
            for (Tuple<String, String> param : params) {
                if (paramsBuilder.length() != 0) paramsBuilder.append('&');
                paramsBuilder.append(URLEncoder.encode(param.first, "UTF-8"))
                        .append('=')
                        .append(URLEncoder.encode(param.second, "UTF-8"));
            }

            String boundary = UUID.randomUUID().toString();
            byte[] boundaryBytes = ("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8);
            byte[] finishBoundaryBytes = ("--" + boundary + "--").getBytes(StandardCharsets.UTF_8);
            con.setRequestProperty("Content-Type", "multipart/form-data; charset=UTF-8; boundary=" + boundary);
            con.setChunkedStreamingMode(0);

            try(OutputStream writer = con.getOutputStream()) {
                writer.write(paramsBuilder.toString().getBytes(StandardCharsets.UTF_8));

                writer.write(boundaryBytes);
                sendField(writer, "quco", uco);
                writer.write(boundaryBytes);
                sendField(writer, "vlsozav", "ajax");
                writer.write(boundaryBytes);
                sendField(writer, "ajax-upload", "ajax");
                writer.write(boundaryBytes);
                sendFile(writer, "FILE_1", file, file.getName());
                writer.write(boundaryBytes);
                sendField(writer, "A_NAZEV_1", file.getName());
                writer.write(boundaryBytes);
                sendField(writer, "A_POPIS_1", "");
                writer.write(boundaryBytes);
                sendField(writer, "TEXT_MAILU", "");

                writer.write(finishBoundaryBytes);
                writer.flush();
            }

            con.connect();
            try (InputStreamReader responseStream = new InputStreamReader(con.getInputStream())) {
                JsonElement response = new JsonParser().parse(responseStream);
                return response.getAsJsonObject().get("uspech").getAsInt() == 1;
            }

        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    private void sendFile(OutputStream out, String name, File input, String fileName) throws IOException {
        try (InputStream in = new FileInputStream(input)) {
            String o = "Content-Disposition: form-data; name=\"" + URLEncoder.encode(name,"UTF-8")
                    + "\"; filename=\"" + URLEncoder.encode(fileName,"UTF-8") + "\"\r\n\r\n";
            out.write(o.getBytes(StandardCharsets.UTF_8));
            byte[] buffer = new byte[2048];
            for (int n = 0; n >= 0; n = in.read(buffer))
                out.write(buffer, 0, n);
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }
    }

    private void sendField(OutputStream out, String name, String field) throws IOException {
        String o = "Content-Disposition: form-data; name=\""
                + URLEncoder.encode(name,"UTF-8") + "\"\r\n\r\n";
        out.write(o.getBytes(StandardCharsets.UTF_8));
        out.write(URLEncoder.encode(field,"UTF-8").getBytes(StandardCharsets.UTF_8));
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }
}
