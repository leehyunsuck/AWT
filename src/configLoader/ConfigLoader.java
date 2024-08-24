package configLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Scanner;
import java.util.function.Consumer;

public class ConfigLoader {
    private Properties properties;

    Consumer<String> logger = n -> System.out.println(this.prefix + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " " + n);
    private static String prefix = "[Config Logger] ";

    public ConfigLoader() {
        properties = new Properties();

        File configFile = new File("config.properties");

        if (!configFile.exists()) {
            try (FileWriter writer = new FileWriter(configFile)) {
                String currentDir = new File(".").getCanonicalPath().replace("\\", "/");
                writer.write("gemini.api.key=\n");
                writer.write("tistory.id=\n");
                writer.write("tistory.password=\n");
                writer.write("!https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-latest:generateContent?key=\n");
                writer.write("gemini.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=\n");
                writer.write("tistory.login.url=https://www.tistory.com/auth/login\n");
                writer.write("chromedriver.path=" + currentDir + "/chromedriver/chromedriver.exe\n");
                logger.accept("Created new config file with default settings");
                logger.accept("check config.properties files");
            } catch (IOException e) {
                logger.accept("Failed to create config file");
                logger.accept("input config.properties and Download chromedriver");
                Scanner scanner = new Scanner(System.in);
                scanner.next();
            }
            System.exit(0);
        }

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
            if (properties.getProperty("gemini.api.key") == null || properties.getProperty("tistory.id") == null || properties.getProperty("tistory.password") == null) {
                logger.accept("check config.properties files");
                System.exit(0);
            }

            String chromeDriverPathFromProperties = properties.getProperty("chromedriver.path");
            File chromeDriverFile = new File(chromeDriverPathFromProperties);
            if (!chromeDriverFile.exists()) {
                logger.accept("Chromedriver file not found at the specified path.");
                System.exit(0);
            }
            logger.accept("Load config file");
        } catch (IOException e) {
            logger.accept("Failed to load config file");
            System.exit(0);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
