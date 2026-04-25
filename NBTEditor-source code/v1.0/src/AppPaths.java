import java.nio.file.*;

public class AppPaths {

    // ===== 应用数据目录 =====
    public static Path getAppData() {
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");

        if (os.contains("win")) {
            return Paths.get(System.getenv("APPDATA"), "NBTEditor");
        }

        if (os.contains("mac")) {
            return Paths.get(home, "Library/Application Support", "NBTEditor");
        }

        return Paths.get(home, ".local/share", "NBTEditor");
    }

    // ===== downloads 目录 =====
    public static Path getDownloads() {
        return getAppData().resolve("downloads");
    }

    // ===== logs 目录（可选）=====
    public static Path getLogs() {
        return getAppData().resolve("logs");
    }

    // ===== update.zip 路径 =====
    public static Path getUpdateZip() {
        return getDownloads().resolve("update.zip");
    }

    // ===== 当前 JAR 所在目录 =====
    public static Path getJarDir() {
        try {
            return Paths.get(
                    AppPaths.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getParent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}