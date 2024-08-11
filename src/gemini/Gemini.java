package gemini;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class Gemini {
    private URL url;

    private String basicPrompt = """
                Your role is a blogger.
                Understand the topic of the question content properly and write an article to get an answer based on the topic.
                
                Writing Language: Korean
                Blog Format: Subhead, Body, Summary
                
                Return String:
                [tS] Made to relate to the content as if not written by ai [tE],
                [cS] content [cE],
                [hS] hashtag1, ..., hashtag10 [hE]
                
                Return Example:
                [tS] This is a sample title [tE],
                [cS] This is a sample content. [cE],
                [hS] hashtag1, ..., hashtag10 [hE]

                question:
                [Q]
                
                a word of encouragement:
                Blog Format is important.
                Content is written in markdown format.
                Add emojis that fit in the middle.
                Make sure the content is written naturally and doesn't need to be modified manually.ompliance with the rules.
                Replace any placeholders like [ ] with appropriate content.
                Please write the content so I can upload it right away without having to modify it.
                """;

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

        String replacePrompt = this.basicPrompt.replace("[Q]", prompt);

        String json =  "{\"contents\":[{\"parts\":[{\"text\":\"" + replacePrompt + "\"}]}]}";

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        //connection.getResponseCode();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) if (responseLine.contains("\"text\"")) {
                String tres = responseLine.trim();
                System.out.println(tres.substring(tres.indexOf("[tS]") + 4,tres.indexOf("[tE]")).trim());
                System.out.println(tres.substring(tres.indexOf("[cS]") + 4,tres.indexOf("[cE]")).trim());
                System.out.println(tres.substring(tres.indexOf("[hS]") + 4,tres.indexOf("[hE]")).trim());
                break;
            }
        }
        //jsonRes.getString("")
    }
}