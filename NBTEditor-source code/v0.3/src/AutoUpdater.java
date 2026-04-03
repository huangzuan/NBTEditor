import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.swing.JOptionPane;
public class AutoUpdater {

    public static void downloadAndUpdate(String zipUrl) {
        try {
            // 下载 zip 到临时目录
            Path tempZip = Files.createTempFile("NBTEditor_update", ".zip");
            try (InputStream in = new URL(zipUrl).openStream()) {
                Files.copy(in, tempZip, StandardCopyOption.REPLACE_EXISTING);
            }

            // 生成更新脚本
            String os = System.getProperty("os.name").toLowerCase();
            Path script = Files.createTempFile("update", os.contains("win") ? ".bat" : ".sh");
            BufferedWriter writer = Files.newBufferedWriter(script);

            if (os.contains("win")) {
                writer.write("@echo off\n");
                writer.write("timeout /t 1\n");  // 等待 1 秒
                writer.write("powershell -Command \"Expand-Archive -Force '" + tempZip.toAbsolutePath() + "' .\"\n");
                writer.write("start java -jar NBTEditor.jar\n");
            } else {
                writer.write("#!/bin/sh\n");
                writer.write("sleep 1\n");
                writer.write("unzip -o \"" + tempZip.toAbsolutePath() + "\" -d .\n");
                writer.write("java -jar NBTEditor.jar &\n");
            }
            writer.close();

            script.toFile().setExecutable(true);

            // 关闭当前程序并执行更新脚本
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    new ProcessBuilder(script.toAbsolutePath().toString()).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "自动更新失败！");
        }
    }
}