/* 这是AutoUpdater.java,负责下载更新并替换当前程序 它被UpdateChecker.java调用,当检测到新版本时会弹出提示框,用户同意后就会执行这个类的downloadAndUpdate方法
  所以这个不是main class,main class是NBTEditor.java
所以这一行该干嘛*/

//由huangzuan制作,如果你想使用或修改这个代码,请遵守MIT协议,并保留原作者信息
//项目网址:https://github.com/huangzuan/NBTEditor/


import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.file.*;

public class AutoUpdater {

    public static void downloadAndUpdate(String zipUrl) {
        try {
            // 下载到临时文件
            Path tempZip = Files.createTempFile("NBTEditor_update", ".zip");

            try (InputStream in = new URL(zipUrl).openStream()) {
                Files.copy(in, tempZip, StandardCopyOption.REPLACE_EXISTING);
            }

            String os = System.getProperty("os.name").toLowerCase();

            // 当前 jar 路径
            String jarPath = new File(
                    AutoUpdater.class.getProtectionDomain()
                            .getCodeSource().getLocation().toURI()
            ).getPath();

            // 脚本路径
            Path script = Files.createTempFile("update", os.contains("win") ? ".bat" : ".sh");

            try (BufferedWriter writer = Files.newBufferedWriter(script)) {

                if (os.contains("win")) {
                    writer.write("@echo off\n");
                    writer.write("timeout /t 2\n");

                    // 解压
                    writer.write("powershell -Command \"Expand-Archive -Force '" + tempZip + "' .\"\n");

                    // 启动新版本
                    writer.write("start java -jar \"" + jarPath + "\"\n");

                } else {
                    writer.write("#!/bin/sh\n");
                    writer.write("sleep 2\n");

                    // 解压
                    writer.write("unzip -o \"" + tempZip + "\" -d .\n");

                    // 赋权限（Mac/Linux）
                    writer.write("chmod +x \"" + jarPath + "\"\n");

                    // 启动
                    writer.write("java -jar \"" + jarPath + "\" &\n");
                }
            }

            script.toFile().setExecutable(true);

            // 🚀 直接启动更新脚本（不用 shutdown hook）
            new ProcessBuilder(script.toAbsolutePath().toString()).start();

            // 退出当前程序
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "自动更新失败！");
        }
    }
}