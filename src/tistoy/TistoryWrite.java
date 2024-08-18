package tistoy;

import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

public class TistoryWrite {

    WebDriver driver;
    WebDriverWait wait;

    public TistoryWrite() {
        // WebDriver 경로 설정
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
        this.driver = new ChromeDriver();
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(20));
        this.join();
    }

    public static String filterBMPCharacters(String input) {
        // Supplementary Plane 문자 제거
        return input.replaceAll("[\\uD800-\\uDFFF]", "");
    }

    private void join() {
        try {
            Properties prop = new Properties();
            try (FileInputStream fis = new FileInputStream("config.properties")) {
                prop.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Tistory 로그인 페이지로 이동
            driver.get("https://www.tistory.com/auth/login");

            // 카카오 계정으로 로그인 버튼 클릭 대기
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("link_kakao_id")));

            // 카카오 계정으로 로그인 버튼 클릭
            WebElement kakaoLoginButton = driver.findElement(By.className("link_kakao_id"));
            kakaoLoginButton.click();

            // 이메일 입력란 대기
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='loginId']")));

            // 로그인 정보 입력
            WebElement emailField = driver.findElement(By.cssSelector("input[name='loginId']"));
            WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
            WebElement loginButton = driver.findElement(By.cssSelector("button.submit"));

            emailField.sendKeys(prop.getProperty("tistory.id"));
            passwordField.sendKeys(prop.getProperty("tistory.password"));

            // 로그인 버튼 클릭
            loginButton.click();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void request(String[] info) throws InterruptedException {
        String  title = info[0],
                content = info[1];

        // 프로필 링크 나타날 때까지 대기
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.link_profile")));
        WebElement profileLink = driver.findElement(By.cssSelector("a.link_profile"));
        profileLink.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.link_edit")));
        WebElement writeButton = driver.findElement(By.cssSelector("a.link_edit"));
        writeButton.click();

        // 이어 작성 거절
        Thread.sleep(1000);
        try {
            Alert alert = driver.switchTo().alert();
            alert.dismiss();
        } catch (Exception e) { }

        // 기본 모드 버튼 클릭
        WebElement basicModeButton = driver.findElement(By.id("editor-mode-layer-btn-open"));
        basicModeButton.click();

        // 마크다운 모드 나타날 때까지 대기
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("editor-mode-markdown-text")));

        // 마크다운 모드 클릭
        WebElement markdownButton = driver.findElement(By.id("editor-mode-markdown-text"));
        markdownButton.click();

        try {
            Alert alert = driver.switchTo().alert();
            alert.accept(); // 경고창 수락
        } catch (Exception e) {
            // 경고창이 없으면 무시
        }


        String filteredTitle = filterBMPCharacters(title); // BMP 범위를 초과하는 문자를 제거합니다
        // 제목 필드에 값 입력
        WebElement titleField = wait.until(ExpectedConditions.elementToBeClickable(By.id("post-title-inp")));
        titleField.sendKeys(filteredTitle);

        WebElement editorContainer = wait.until(ExpectedConditions.elementToBeClickable(By.id("markdown-editor-container")));
        editorContainer.click();


        JavascriptExecutor js = (JavascriptExecutor) driver;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("content", content);
        String jsonString = jsonObject.toString();
        String script = "var cm = document.querySelectorAll('.CodeMirror')[1].CodeMirror;" +
                "cm.setValue(JSON.parse(arguments[0]).content);" +
                "setTimeout(function() { cm.refresh(); }, 1000);";
        js.executeScript(script, jsonString);


        Actions actions = new Actions(driver);
        editorContainer.click();
        actions.keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).perform();
        actions.keyDown(Keys.CONTROL).sendKeys("c").keyUp(Keys.CONTROL).perform();
        actions.keyDown(Keys.CONTROL).sendKeys("v").keyUp(Keys.CONTROL).perform();


        String[] tags = new String[info.length-2];
        for (int i = 2; i < info.length; i++) tags[i-2] = info[i];

        // 태그 입력 필드 찾기
        WebElement tagInputField = driver.findElement(By.id("tagText"));

        for (int i = 0; i < tags.length && i < 10; i++) {
            tagInputField.sendKeys(tags[i]);
            tagInputField.sendKeys(Keys.ENTER);
            Thread.sleep(250);
        }

        Thread.sleep(1000000);

        // 글 작성 완료
        WebElement checkButton = driver.findElement(By.id("publish-layer-btn"));
        checkButton.click();

        WebElement radioButton = driver.findElement(By.id("open20"));
        radioButton.click();

        WebElement successButton = driver.findElement(By.id("publish-btn"));
        successButton.click();


        System.out.println("블로그 글 작성 완료!");
    }

}
