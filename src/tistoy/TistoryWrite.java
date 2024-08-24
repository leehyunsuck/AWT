package tistoy;

import configLoader.ConfigLoader;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class TistoryWrite {

    WebDriver driver;
    WebDriverWait wait;

    Consumer<String> logger = n -> System.out.println(this.prefix + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " " + n);
    private static String prefix = "[Tistory Logger] ";

    public TistoryWrite() {
        // WebDriver 경로 설정
        logger.accept("WebDriver setup");
        ConfigLoader configLoader = new ConfigLoader();
        System.setProperty("webdriver.chrome.driver", configLoader.getProperty("chromedriver.path"));

        // ChromeOptions 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // 헤드리스 모드 설정
        options.addArguments("--disable-gpu"); // GPU 비활성화 (헤드리스 모드에서 권장)
        options.addArguments("--window-size=1920,1080"); // 창 크기 설정 (헤드리스 모드에서 권장)

        this.driver = new ChromeDriver(options);
        logger.accept("WebDriver setup complete");

        logger.accept("WebDriver wait setup");
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(20));
        logger.accept("WebDriver wait setup complete");

        // 로그인까지 하는 기능
        this.join();
    }

    public static String filterBMPCharacters(String input) {
        return input.replaceAll("[\\uD800-\\uDFFF]", "");
    }

    // 로그인
    private void join() {
        logger.accept("Access start");
        try {
            ConfigLoader configLoader = new ConfigLoader();

            driver.get(configLoader.getProperty("tistory.login.url"));

            // 카카오 계정으로 로그인
            WebElement kakaoLoginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("link_kakao_id")));
            kakaoLoginButton.click();
            logger.accept("Move to kakao login");

            // 로그인 정보 입력
            WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='loginId']")));
            WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
            WebElement loginButton = driver.findElement(By.cssSelector("button.submit"));
            emailField.sendKeys(configLoader.getProperty("tistory.id"));
            passwordField.sendKeys(configLoader.getProperty("tistory.password"));
            loginButton.click();
            logger.accept("Input information");

            // 2차 인증 절차
            try {
                WebElement kg = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(By.id("label-isRememberBrowser")));

                if (kg != null) {   // 2차 인증 있음
                    logger.accept("Kakao authentication is activated");
                    WebDriverWait passWait = new WebDriverWait(driver, Duration.ofSeconds(90));
                    logger.accept("");
                    logger.accept("!! Read !!");
                    logger.accept("The two-step authentication verification has begun");
                    logger.accept("Please accept login within 90 seconds");
                    logger.accept("");
                    WebElement passButton = passWait.until(ExpectedConditions.elementToBeClickable(By.className("btn_agree")));
                    passButton.click();
                } else {            // 2차 인증 없음
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

    // 글 작성 요청
    public void request(String[] info) throws InterruptedException {
        String  title = info[0],
                content = info[1];
        String[] tags = new String[info.length-2];

        String filteredTitle = filterBMPCharacters(title); // BMP 범위를 초과하는 문자 제거

        for (int i = 2; i < info.length; i++) tags[i-2] = info[i];

        // 프로필 링크 나타날 때까지 대기
        WebElement profileLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.link_profile")));
        profileLink.click();
        logger.accept("Profile access");


        WebElement writeButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.link_edit")));
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

        // 마크다운 모드 클릭
        WebElement markdownButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("editor-mode-markdown-text")));
        markdownButton.click();
        logger.accept("Change input method");

        try {
            Alert alert = driver.switchTo().alert();
            alert.accept(); // 경고창 수락
        } catch (Exception e) {
            // 경고창이 없으면 무시
        }

        try {
            Actions actions = new Actions(driver);
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // 제목 입력
            WebElement titleField = wait.until(ExpectedConditions.elementToBeClickable(By.id("post-title-inp")));
            titleField.sendKeys(filteredTitle);
            logger.accept("Input title success");

            // 내용 입력
            WebElement editorContainer = wait.until(ExpectedConditions.elementToBeClickable(By.id("markdown-editor-container")));
            editorContainer.click();
            try {
                String script = "var cm = document.querySelectorAll('.CodeMirror')[1].CodeMirror;" +
                        "cm.setValue(arguments[0].replace(/\\\\n/g, '\\n').replace(/\\\\t/g, '\\t'));" +
                        "cm.refresh();";
                js.executeScript(script, content);
            } catch (Exception e) {
                throw e;
            }
            Thread.sleep(250);
            editorContainer.click();
            actions.keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).perform();
            actions.keyDown(Keys.CONTROL).sendKeys("c").keyUp(Keys.CONTROL).perform();
            actions.keyDown(Keys.CONTROL).sendKeys("v").keyUp(Keys.CONTROL).perform();
            logger.accept("Input content success");

            // 태그 입력
            WebElement tagInputField = driver.findElement(By.id("tagText"));
            for (int i = 0; i < tags.length && i < 10; i++) {
                tagInputField.sendKeys(tags[i]);
                tagInputField.sendKeys(Keys.ENTER);
                Thread.sleep(100);
            }
            logger.accept("Input tag success");

            // 글 작성 완료
            WebElement checkButton = driver.findElement(By.id("publish-layer-btn"));
            checkButton.click();

            WebElement radioButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("open20"))); //driver.findElement(By.id("open20"));
            radioButton.click();

            WebElement successButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("publish-btn")));
            successButton.click();

            Thread.sleep(250);
            try {
                Alert alert = driver.switchTo().alert();
                alert.accept(); // 경고창 수락
                logger.accept("You have exceeded the number you can start writing daily");
                System.out.println("[System] stop the program");
                System.exit(0);
            } catch (Exception e) {
                logger.accept("Blog post upload successful");
            }
        } catch (Exception e) {
            logger.accept("Error : " + e.getMessage());
            this.request(info);
        }

        WebElement linkElement = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("ul.list_post.list_post_type2 > li:first-child > div.post_cont > strong.tit_post.tit_ellip > a")));
        String hrefValue = linkElement.getAttribute("href");
        StringSelection stringSelection = new StringSelection(hrefValue);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
    }

}
