import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import org.json.JSONObject;
import org.json.JSONArray;

public class UpdateChecker {

    private static final String CURRENT_VERSION = "v0.2";
    private static final String GITHUB_API = "https://api.github.com/repos/huangzuan/NBTEditor/releases/latest";

    public static void checkOnStart() {
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(GITHUB_API).openConnection();
                conn.setRequestProperty("Accept", "application/vnd.github+json");
                conn.connect();

                if (conn.getResponseCode() != 200) return;

                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                }

                JSONObject release = new JSONObject(sb.toString());
                String latestTag = release.getString("tag_name");

                if (!latestTag.equals(CURRENT_VERSION)) {
                    JSONArray assets = release.getJSONArray("assets");
                    String zipUrl = null;
                    if (assets.length() > 0) {
                        zipUrl = assets.getJSONObject(0).getString("browser_download_url");
                    }

                    String finalZipUrl = zipUrl;
                    SwingUtilities.invokeLater(() -> {
                        int option = JOptionPane.showConfirmDialog(null,
                                "发现新版本 " + latestTag + "，是否更新？",
                                "更新提示", JOptionPane.YES_NO_OPTION);
                        if (option == JOptionPane.YES_OPTION && finalZipUrl != null) {
                            AutoUpdater.downloadAndUpdate(finalZipUrl);
                        }
                    });
                }

            } catch (Exception e) {
                // 网络失败或 JSON 解析失败可以忽略
            }
        }).start();
    }
}