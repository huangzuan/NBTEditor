import javax.swing.*;
import java.io.*;
import java.net.*;
import org.json.*;

public class UpdateChecker {

    private static final String VERSION = "v1.0";
    private static final String API =
            "https://api.github.com/repos/huangzuan/NBTEditor/releases/latest";

    public static void check() {

        new Thread(() -> {
            try {

                HttpURLConnection conn =
                        (HttpURLConnection) new URL(API).openConnection();

                conn.setRequestProperty("User-Agent", "NBTEditor");
                conn.connect();

                if (conn.getResponseCode() != 200) return;

                StringBuilder sb = new StringBuilder();

                try (BufferedReader br =
                             new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                }

                JSONObject json = new JSONObject(sb.toString());
                String latest = json.getString("tag_name");

                if (!latest.equalsIgnoreCase(VERSION)) {

                    JSONArray arr = json.getJSONArray("assets");
                    String url = null;

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject a = arr.getJSONObject(i);
                        if (a.getString("name").endsWith(".zip")) {
                            url = a.getString("browser_download_url");
                            break;
                        }
                    }

                    String finalUrl = url;

                    SwingUtilities.invokeLater(() -> {
                        int r = JOptionPane.showConfirmDialog(
                                null,
                                "发现新版本 " + latest,
                                "Update",
                                JOptionPane.YES_NO_OPTION
                        );

                        if (r == JOptionPane.YES_OPTION && finalUrl != null) {
                            AutoUpdater.update(finalUrl);
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}