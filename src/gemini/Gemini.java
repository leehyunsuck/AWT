package gemini;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class Gemini {
    private URL url;

    public Gemini() throws Exception {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            prop.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.url = new URL(prop.getProperty("gemini.url.flash") + prop.getProperty("gemini.api.key"));
    }

    public Gemini(String model, String apiKey) throws Exception {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            prop.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.url = switch (model) {
            case "pro" -> new URL(prop.getProperty("gemini.url.pro") + apiKey);
            case "flash" -> new URL(prop.getProperty("gemini.url.flash") + apiKey);
            default -> throw new Exception("Invalid model");
        };
    }

    public void request(String prompt) throws IOException {
        HttpURLConnection connection =  (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");

        String json =  "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt + "\"}]}]}";

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        System.out.println(connection.getResponseCode());

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
                System.out.println(responseLine);
            }
        }
    }
}

/*

curl \
  -H 'Content-Type: application/json' \
  -d '{"contents":[{"parts":[{"text":"Explain how AI works"}]}]}' \
  -X POST 'https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=YOUR_API_KEY'

 */