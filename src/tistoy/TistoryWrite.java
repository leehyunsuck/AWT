package tistoy;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.function.Consumer;

public class TistoryWrite {

    WebDriver driver;
    WebDriverWait wait;

    Consumer<String> logger = n -> System.out.println(this.prefix + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " " + n);
    private static String prefix = "[Tistory Logger] ";

    public TistoryWrite() {
        // WebDriver 경로 설정
        logger.accept("WebDriver setup");
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            prop.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.setProperty("webdriver.chrome.driver", prop.getProperty("chromedriver.path"));

        // ChromeOptions 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // 헤드리스 모드 설정
        options.addArguments("--disable-gpu"); // GPU 비활성화 (헤드리스 모드에서 권장)
        options.addArguments("--window-size=1920,1080"); // 창 크기 설정 (헤드리스 모드에서 권장)


        this.driver = new ChromeDriver(options);
        logger.accept("WebDriver setup complete");

        logger.accept("WebDriver wait setup seconds(20)");
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(20));
        logger.accept("WebDriver wait setup complete");
        this.join();
    }

    public static String filterBMPCharacters(String input) {
        // Supplementary Plane 문자 제거
        return input.replaceAll("[\\uD800-\\uDFFF]", "");
    }

    private void join() {
        logger.accept("Access start");
        try {
            Properties prop = new Properties();
            try (FileInputStream fis = new FileInputStream("config.properties")) {
                prop.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }

            driver.get("https://www.tistory.com/auth/login");

            // 카카오 계정으로 로그인
            WebElement kakaoLoginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("link_kakao_id")));
            kakaoLoginButton.click();
            logger.accept("Kakao login");

            // 이메일 입력란 대기
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='loginId']")));

            // 로그인 정보 입력
            WebElement emailField = driver.findElement(By.cssSelector("input[name='loginId']"));
            WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
            WebElement loginButton = driver.findElement(By.cssSelector("button.submit"));

            emailField.sendKeys(prop.getProperty("tistory.id"));
            passwordField.sendKeys(prop.getProperty("tistory.password"));
            loginButton.click();
            logger.accept("Input information");

            // 2차 인증 절차
            try {
                WebElement kg = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(By.id("label-isRememberBrowser")));
                if (kg != null) {
                    logger.accept("Kakao authentication is activated");
                    // 2차 인증

                    WebDriverWait passWait = new WebDriverWait(driver, Duration.ofSeconds(90));
                    logger.accept("!! Read !!");
                    logger.accept("The two-step authentication verification has begun");
                    logger.accept("Please accept login within 90 seconds");
                    logger.accept("");
                    WebElement passButton = passWait.until(ExpectedConditions.elementToBeClickable(By.className("btn_agree")));
                    passButton.click();
                } else {
                    logger.accept("Kakao authentication is not activated");
                }
            } catch (TimeoutException e) {
                logger.accept("Kakao authentication is not activated");
            } catch (Exception e) {
                throw e;
            }

            logger.accept("Access agree");
        } catch (Exception e) {
            logger.accept("Error : " + e.getMessage());
            logger.accept("Restart");
            this.join();
        }
    }

    public void request(String[] info) throws InterruptedException {
        String  title = info[0],
                content = info[1];

        // 프로필 링크 나타날 때까지 대기
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.link_profile")));
        WebElement profileLink = driver.findElement(By.cssSelector("a.link_profile"));
        profileLink.click();
        logger.accept("Profile access");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.link_edit")));
        WebElement writeButton = driver.findElement(By.cssSelector("a.link_edit"));
        writeButton.click();
        logger.accept("Write access");

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

        logger.accept("Change input method");

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
        logger.accept("Input title success");

        WebElement editorContainer = wait.until(ExpectedConditions.elementToBeClickable(By.id("markdown-editor-container")));
        editorContainer.click();

        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = "var cm = document.querySelectorAll('.CodeMirror')[1].CodeMirror;" +
                "cm.setValue('[content]');".replace("[content]", content);
        js.executeScript(script);

        Actions actions = new Actions(driver);
        editorContainer.click();
        actions.keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).perform();
        actions.keyDown(Keys.CONTROL).sendKeys("c").keyUp(Keys.CONTROL).perform();
        actions.keyDown(Keys.CONTROL).sendKeys("v").keyUp(Keys.CONTROL).perform();
        logger.accept("Input content success");


        String[] tags = new String[info.length-2];
        for (int i = 2; i < info.length; i++) tags[i-2] = info[i];

        // 태그 입력 필드 찾기
        WebElement tagInputField = driver.findElement(By.id("tagText"));

        for (int i = 0; i < tags.length && i < 10; i++) {
            tagInputField.sendKeys(tags[i]);
            tagInputField.sendKeys(Keys.ENTER);
            Thread.sleep(250);
        }
        logger.accept("Input tag success");

        // 글 작성 완료
        WebElement checkButton = driver.findElement(By.id("publish-layer-btn"));
        checkButton.click();

        Thread.sleep(250);

        WebElement radioButton = driver.findElement(By.id("open20"));
        radioButton.click();

        Thread.sleep(250);

        WebElement successButton = driver.findElement(By.id("publish-btn"));
        successButton.click();

        Thread.sleep(250);

        logger.accept("Blog post upload successful");
        
        // url 받기 추가해야함
    }

}
