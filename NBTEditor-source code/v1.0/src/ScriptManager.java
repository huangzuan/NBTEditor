import java.nio.file.*;

public class ScriptManager {

    public static void ensureScripts(Path jarDir) {

        String os = System.getProperty("os.name").toLowerCase();

        try {

            if (os.contains("win")) {

                Path bat = jarDir.resolve("update.bat");

                if (!Files.exists(bat)) {
                    Files.writeString(bat, windows());
                }

            } else {

                Path sh = jarDir.resolve("update.sh");

                if (!Files.exists(sh)) {
                    Files.writeString(sh, unix());
                    sh.toFile().setExecutable(true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String windows() {
        return """
@echo off
set APP_DIR=%~1
set DATA_DIR=%~2

cd /d %APP_DIR%

timeout /t 2

:wait
tasklist | find "java.exe" > nul
if %errorlevel% == 0 (
    timeout /t 1
    goto wait
)

powershell -Command "Expand-Archive -Force %DATA_DIR%\\downloads\\update.zip ."

start java -jar NBTEditor.jar
exit
""";
    }

    private static String unix() {
        return """
#!/bin/sh

APP_DIR="$1"
DATA_DIR="$2"

cd "$APP_DIR"

sleep 2

while pgrep -f "NBTEditor.jar" > /dev/null; do
    sleep 1
done

unzip -o "$DATA_DIR/downloads/update.zip" -d .

java -jar NBTEditor.jar &
""";
    }
}