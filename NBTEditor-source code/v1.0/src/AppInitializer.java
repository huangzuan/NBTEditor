import java.nio.file.*;

public class AppInitializer {

    public static void init() {

        try {

            Path app = AppPaths.getAppData();
            Files.createDirectories(app);

            Path downloads = AppPaths.getDownloads();
            Files.createDirectories(downloads);

            Path logs = app.resolve("logs");
            Files.createDirectories(logs);

            ScriptManager.ensureScripts(AppPaths.getJarDir());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}