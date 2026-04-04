/* 这是UpdateChecker.java,负责检查更新 它在程序启动时调用,检测是否有新版本可用
  所以这个不是main class,main class是NBTEditor.java
阿巴阿巴*/

//由huangzuan制作,如果你想使用或修改这个代码,请遵守MIT协议,并保留原作者信息
//项目网址:https://github.com/huangzuan/NBTEditor/

//还有这个依赖json库,但是已经打包到项目里了,不用担心


import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import org.json.JSONObject;
import org.json.JSONArray;

public class UpdateChecker {

    private static final String CURRENT_VERSION = "v0.3.1.1";
    private static final String GITHUB_API = "https://api.github.com/repos/huangzuan/NBTEditor/releases/latest";
    

    public static void checkOnStart() {
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(GITHUB_API).openConnection();
                conn.setRequestProperty("User-Agent", "NBTEditor");
                conn.setRequestProperty("Accept", "application/vnd.github+json");
                conn.connect();
                conn.disconnect();
               

                if (conn.getResponseCode() != 200) return;

                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                }

                JSONObject release = new JSONObject(sb.toString());
                String latestTag = release.getString("tag_name");

                if (!latestTag.equalsIgnoreCase(CURRENT_VERSION)) {
                    JSONArray assets = release.getJSONArray("assets");
                    String zipUrl = null;
                    if (assets.length() > 0) {
                        for (int i = 0; i < assets.length(); i++) {
    JSONObject asset = assets.getJSONObject(i);
    String name = asset.getString("name");

    if (name.endsWith(".zip")) {
        zipUrl = asset.getString("browser_download_url");
        break;
    }
}
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
                e.printStackTrace();
            } 

        }).start();
    }
} 
