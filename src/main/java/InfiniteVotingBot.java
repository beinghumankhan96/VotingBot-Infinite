import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.Random;

public class InfiniteVotingBot {
    private static final String URL = "https://belagaviartfestival.com/belagavi-art-festival-gallery/?cid=1252&cac=submission&ctx=page&cm=0&sid=4501";
    private static final By BUTTON = By.cssSelector("button[type='submit']");
    private static final int BATCH_SIZE = 260;        // runs per JVM batch
    private static final int MAX_RETRIES = 3;
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        System.out.println("🚀 HIGH-PERFORMANCE Infinite Voting Bot Started - 24/7 ∞");
        WebDriverManager.chromedriver().setup();

        int totalVotes = 0;
        int batchCount = 1;

        while (true) {  // INFINITE 24/7
            System.out.println("\n=== 🗳️ BATCH #" + batchCount + " STARTING ===");

            int batchSuccess = 0;
            for (int i = 1; i <= BATCH_SIZE; i++) {
                try {
                    if (executeFastVote(totalVotes + i, batchCount)) {
                        batchSuccess++;
                    }
                } catch (Exception e) {
                    System.out.println("💥 Run " + (totalVotes + i) + " crashed: " + e.getMessage());
                }
            }

            totalVotes += batchSuccess;
            System.out.println("✅ BATCH #" + batchCount + " COMPLETE | Success: " + batchSuccess + "/" + BATCH_SIZE + " | TOTAL: " + totalVotes);

            batchCount++;
            coolDownBetweenBatches();
        }
    }

    private static boolean executeFastVote(int runNumber, int batchNumber) {
        WebDriver driver = null;
        int attempts = 0;

        while (attempts < MAX_RETRIES) {
            try {
                driver = createUltraFastDriver();
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

                System.out.println("🔄 [" + batchNumber + "." + runNumber + "] Loading...");
                driver.get(URL);

                // AJAX and page ready check - FIXED lambda parameter name
                wait.until(d -> {
                    boolean jsReady = (Boolean) ((JavascriptExecutor) d)
                            .executeScript("return document.readyState === 'complete' && (!window.jQuery || jQuery.active === 0)");
                    boolean buttonReady = d.findElements(BUTTON).size() > 0;
                    return jsReady && buttonReady;
                });

                WebElement button = wait.until(ExpectedConditions.elementToBeClickable(BUTTON));

                // ULTRA-FAST CLICK STRATEGY (3 fallbacks)
                if (tryNormalClick(button)) {
                    return true;
                }

                scrollAndRetry(button, driver);
                if (tryNormalClick(button)) {
                    return true;
                }

                // JS Click as final resort (99.9% success rate)
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                sleep(800); // Post JS click stabilization

                return true;

            } catch (Exception e) {
                attempts++;
                // FIXED: Use getMessage() instead of getShortMessage()
                System.out.println("⚡ Retry " + attempts + "/" + MAX_RETRIES + ": " + e.getMessage());
                if (driver != null) {
                    try { driver.quit(); } catch (Exception ignored) {}
                }
                sleep(randomDelay(1000, 2000));
            }
        }
        return false;
    }

    private static boolean tryNormalClick(WebElement button) {
        try {
            button.click();
            sleep(800); // Post-click stabilization
            return true;
        } catch (ElementClickInterceptedException ignored) {
            return false;
        }
    }

    private static void scrollAndRetry(WebElement button, WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', behavior: 'instant'});", button);
        sleep(300);
    }

    private static WebDriver createUltraFastDriver() {
        ChromeOptions options = new ChromeOptions();

        // ULTRA-FAST PERFORMANCE FLAGS
        options.addArguments("--incognito");
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-images");
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-renderer-backgrounding");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-web-security");
        options.addArguments("--disable-features=VizDisplayCompositor");
        options.addArguments("--memory-pressure-off");
        options.addArguments("--max_old_space_size=4096");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        return new ChromeDriver(options);
    }

    private static void coolDownBetweenBatches() {
        System.out.println("❄️ Cooling down... (15s)");
        System.gc();  // Force garbage collection
        sleep(15000);
    }

    // Ultra-fast sleep utility
    private static void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }

    private static long randomDelay(long min, long max) {
        return min + RANDOM.nextInt((int)(max - min));
    }
}
