package gemini;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

public class Gemini {
    private URL url;

    private String basicPrompt = """
                Your role is a blogger.
                Understand the topic of the question content properly and write an article to get an answer based on the topic.
                
                Writing Language: Korean
                Blog Format: Subhead, Body, Summary
                
                Return String:
                {
                    title : Made to relate to the content as if not written by ai,
                    content : content ,
                    hashtags : hashtag1 hashtag2 ... hashtag10
                }
                
                Return Example:
                {
                    title : This is a sample title,
                    content : This is a sample content.,
                    hashtags : hashtag1 hashtag2 ... hashtag10
                }

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

        // 받은 값 읽기
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            JSONObject jsonResponse = new JSONObject(response.toString());

            // text 부분 추출
            String text = jsonResponse.getJSONArray("candidates").getJSONObject(0)
                    .getJSONObject("content").getJSONArray("parts")
                    .getJSONObject(0).getString("text");

            // 필요 없는 부분 제거
            text = text.replace("```json", "").replace("```", "").trim();

            // 필요한 부분만 추출
            JSONObject content = new JSONObject(text);
            String title = content.getString("title");
            String contentText = content.getString("content");
            String[] hashtags = content.getString("hashtags").split(", ");

            System.out.println("Title: " + title);
            System.out.println("Content: " + contentText);
            System.out.print("Hashtags: ");
            Arrays.stream(hashtags).forEach(System.out::print);


        }
    }
}