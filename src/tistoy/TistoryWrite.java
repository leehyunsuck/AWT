package tistoy;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
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

    public void request(String title, String content, String hashtags) throws InterruptedException {

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

        // 제목, 내용, 해쉬태그 입력
        WebElement titleField = driver.findElement(By.id("post-title-inp"));
        titleField.sendKeys(title);

        WebElement editorContainer = wait.until(ExpectedConditions.elementToBeClickable(By.id("markdown-editor-container")));
        editorContainer.click();

        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = "var cm = document.querySelectorAll('.CodeMirror')[1].CodeMirror;" +
                "cm.setValue('[content]');".replace("[content]", content);
        js.executeScript(script);


        String[] tags = {"태그1", "태그2", "태그3", "태그4", "태그5", "태그6", "태그7", "태그8", "태그9", "태그10"};

        // 태그 입력 필드 찾기
        WebElement tagInputField = driver.findElement(By.id("tagText"));

        // 배열을 통해 태그 입력
        for (String tag : tags) {
            tagInputField.sendKeys(tag);
            tagInputField.sendKeys(Keys.ENTER);
            Thread.sleep(500);  // 태그가 추가되는 시간을 고려한 잠시 대기
        }

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
