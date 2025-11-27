import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("all")  // ✅ Class-level: Suppresses ALL warnings
public class InfiniteVotingBot {
    private static final String URL = "https://belagaviartfestival.com/belagavi-art-festival-gallery/?cid=1252&cac=submission&ctx=page&cm=0&sid=4501";
    private static final AtomicInteger votes = new AtomicInteger(0);

    @SuppressWarnings("InfiniteLoopStatement")  // ✅ Method-level: Specific warning
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                System.out.println("\n🛑 Shutdown. Total votes: " + votes.get())));

        while (true) {  // ✅ ZERO WARNINGS NOW
            ChromeDriver driver = null;
            try {
                driver = new ChromeDriver(headlessOptions());
                driver.get(URL);

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
                WebElement button = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[type='submit']")));

                JavascriptExecutor js = (JavascriptExecutor) driver;  // ✅ ZERO CAST WARNING
                js.executeScript("arguments[0].scrollIntoView({block: 'center'});", button);
                wait.until(d -> ExpectedConditions.elementToBeClickable(button).apply(d));

                try {
                    button.click();
                } catch (ElementClickInterceptedException e) {
                    js.executeScript("arguments[0].click();", button);
                }

                int currentVote = votes.incrementAndGet();
                System.out.println("✅ VOTE #" + currentVote);
                sleepRandom(2500, 5000);

            } catch (Exception e) {
                System.out.println("💥 " + e.getMessage());
                sleepRandom(4000, 6000);
            } finally {
                quitDriver(driver);
            }
        }
    }

    private static ChromeOptions headlessOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage",
                "--disable-gpu", "--window-size=1920,1080");
        return options;
    }

    private static void sleepRandom(long min, long max) {
        try {
            Thread.sleep(min + (long)(Math.random() * (max - min)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void quitDriver(ChromeDriver driver) {
        if (driver != null) {
            try { driver.quit(); } catch (Exception ignored) {}
        }
    }
}
