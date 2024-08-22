package gemini;

import configLoader.ConfigLoader;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class Gemini {

    Consumer<String> logger = n -> System.out.println(this.prefix + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " " + n);

    private URL url;

    private static String prefix = "[Gemini Logger] ";

    private String basicPrompt = """
                role : blogger
                goal : write an article based on the topic of the question content
                writing language: korean
                blog Format: Subhead, Body, Summary
                
                Return Example:
                {
                    title : Made to relate to the content as if not written by ai,
                    content : Escaped Markdown Format Content. Make it as clean and look good as possible. (Don't use emojis)
                    hashtags : hashtag1 hashtag2 ... hashtag10 (total 10 hashtags, no more, no less)
                }

                question:
                [Q]
                
                rule : 
                {
                    Add emojis that fit in the middle,
                    Make sure the content is written naturally and doesn't need to be modified manually,
                }
               
                Escape special characters: Ensure all special characters in strings are properly escaped (\\n as \\\\n, " as \\", \\ as \\\\).
                Grammar and style: Ensure content is grammatically correct and written naturally.
                Headers and delimiters: Use clear headers and delimiters to separate different parts of the response.
                Exception handling: Properly handle exceptions and provide meaningful error messages.            
                Testing: Conduct tests for various scenarios and edge cases.
                
                Important: Always escape backslashes in file paths. For example, write "C:\\\\Program Files\\\\Minecraft" instead of "C:\\Program Files\\Minecraft".
                """;

    public Gemini() throws Exception {
        ConfigLoader configLoader = new ConfigLoader();
        this.url = new URL(configLoader.getProperty("gemini.url") + configLoader.getProperty("gemini.api.key"));
    }

    private String preprocessJsonString(String jsonString) {
        jsonString = jsonString.replaceAll("```json\\s*", "").replaceAll("\\s*```\\s*$", "");
        return jsonString.replaceAll("(?<!\\\\)\\\\(?![\"\\\\])", "\\\\\\\\");
    }

    public String[] request(String prompt) throws IOException {
        String[] result;
        try {
            logger.accept("Request setting");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");

            // 이스케이프 처리
            String replacePrompt = this.basicPrompt.replace("[Q]", prompt);
            String escapedPrompt = replacePrompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");

            // JSON 문자열 생성
            String json = String.format("{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}", escapedPrompt);

            logger.accept("Request start");
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            logger.accept("Response Code : " + connection.getResponseCode());

            // 받은 값 읽기
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;

                logger.accept("White space removal in progress");
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                logger.accept("White space removal complete");

                logger.accept("JSON parsing start");
                JSONObject jsonResponse = new JSONObject(response.toString());

                // "candidates" 키로 응답 파싱
                JSONObject candidate = jsonResponse.getJSONArray("candidates").getJSONObject(0);
                JSONObject content = candidate.getJSONObject("content");
                String text = content.getJSONArray("parts").getJSONObject(0).getString("text");

                // 전처리 수행
                text = preprocessJsonString(text);

                // JSON에서 중첩된 JSON 문자열 파싱
                JSONObject contentJson = new JSONObject(text);
                logger.accept("JSON parsing complete");

                logger.accept("Return value setting start");
                String title = contentJson.optString("title", "").replaceAll("[\\p{So}\\p{C}]", "").trim();
                String contentText = contentJson.optString("content", "").trim();
                String[] hashtags = contentJson.optString("hashtags", "").split(",\\s*");

                if (hashtags.length == 1) hashtags = contentJson.optString("hashtags", "").replace("#", "").split(" ");

                result = new String[hashtags.length + 2];
                result[0] = title;
                result[1] = contentText;

                for (int i = 2; i < result.length; i++) result[i] = hashtags[i - 2];

                logger.accept("Return success");
            }
        } catch (Exception e) {
            logger.accept("Error : " + e.getMessage());
            logger.accept("Restart");
            return this.request(prompt);
        }
        return result;
    }
}