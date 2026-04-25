import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.file.*;

public class AutoUpdater {

    public static void update(String url) {

        try {

            Path zip = AppPaths.getUpdateZip();
            Files.createDirectories(zip.getParent());

            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, zip, StandardCopyOption.REPLACE_EXISTING);
            }

            Path jarDir = AppPaths.getJarDir();

            ScriptManager.ensureScripts(jarDir);

            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                new ProcessBuilder(
                        "cmd",
                        jarDir.resolve("update.bat").toString(),
                        jarDir.toString(),
                        AppPaths.getAppData().toString()
                ).start();
            } else {
                new ProcessBuilder(
                        "sh",
                        jarDir.resolve("update.sh").toString(),
                        jarDir.toString(),
                        AppPaths.getAppData().toString()
                ).start();
            }

            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "更新失败");
        }
    }
}