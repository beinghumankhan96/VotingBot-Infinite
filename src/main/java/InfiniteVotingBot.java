package com.voting;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.util.List;

class InfiniteVotingBot {
    private static final String TARGET_URL = "https://belagaviartfestival.com/belagavi-art-festival-gallery/?cid=1252&cac=submission&ctx=page&cm=0&sid=4501";
    private static final String LOG_FILE = "voting-log.txt";
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static JavascriptExecutor js;
    private static FileWriter logWriter;
    private static int totalVotes = 0;

    public static void main(String[] args) {
        try {
            String batchSizeStr = System.getenv("BATCH_SIZE");
            String maxRuntimeStr = System.getenv("MAX_RUNTIME");
            int batchSize = batchSizeStr != null ? Integer.parseInt(batchSizeStr) : 150;
            long maxRuntimeMinutes = maxRuntimeStr != null ? parseMinutes(maxRuntimeStr) : 50;

            log("🚀 PRODUCTION VOTING BOT v3.0 - AJAX READY");
            log("🎯 Target: " + TARGET_URL);
            log("📊 Batch: " + batchSize + " | Runtime: " + maxRuntimeMinutes + "m");

            setupDriver();
            long endTime = System.currentTimeMillis() + (maxRuntimeMinutes * 60 * 1000);
            int batchCount = 0;

            while (System.currentTimeMillis() < endTime && totalVotes < (batchSize * 5)) {
                batchCount++;
                if (castVoteWithRetry(batchCount)) {
                    totalVotes++;
                }
                Thread.sleep(2000 + (int)(Math.random() * 3000)); // 2-5s human delay
            }

            log("🏁 FINISHED! Total Votes: " + totalVotes + " | Projection: " + (totalVotes * 24) + "/day");

        } catch (Exception e) {
            log("💥 FATAL: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private static void setupDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
            "--headless=new",
            "--no-sandbox", "--disable-dev-shm-usage",
            "--disable-gpu", "--disable-extensions",
            "--disable-plugins", "--disable-images",
            "--incognito", "--disable-web-security",
            "--window-size=1920,1080",
            "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        );

        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;
        log("✅ Chrome Driver Ready");
    }

    private static boolean castVoteWithRetry(int attempt) {
        int maxRetries = 3;
        for (int retry = 0; retry < maxRetries; retry++) {
            try {
                log("🔄 Vote #" + attempt + " (Retry " + (retry + 1) + "/" + maxRetries + ")");
                
                // Navigate & wait for page stability
                driver.get(TARGET_URL);
                wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));
                
                // Wait for dynamic content (AJAX)
                wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("button.totalcontest-button-vote")));
                
                // Scroll to button & make clickable
                WebElement voteButton = driver.findElement(By.cssSelector("button.totalcontest-button-vote"));
                js.executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", voteButton);
                wait.until(ExpectedConditions.elementToBeClickable(voteButton));
                
                // Try normal click first
                try {
                    voteButton.click();
                } catch (ElementClickInterceptedException e) {
                    // Force JS click for overlays/popups
                    js.executeScript("arguments[0].click(); arguments[0].dispatchEvent(new Event('click', {bubbles: true}));", voteButton);
                }
                
                // Wait for AJAX response (multiple success patterns)
                boolean success = wait.until(driver -> {
                    try {
                        // Check for success messages or button disabled state
                        List<WebElement> successIndicators = driver.findElements(By.xpath(
                            "//*[contains(text(), 'voted') or contains(text(), 'success') or contains(text(), 'Thank') or contains(text(), 'Thank you') or @disabled or contains(@class, 'disabled')]"));
                        
                        // Or check if button is disabled (common AJAX pattern)
                        WebElement buttonState = driver.findElement(By.cssSelector("button.totalcontest-button-vote"));
                        boolean isDisabled = buttonState.getAttribute("disabled") != null || 
                                          "true".equals(buttonState.getAttribute("disabled"));
                        
                        return !successIndicators.isEmpty() || isDisabled;
                    } catch (Exception e) {
                        return false;
                    }
                });
                
                if (success) {
                    log("✅ VOTE SUCCESS #" + totalVotes);
                    return true;
                }
                
            } catch (TimeoutException e) {
                log("⏰ Timeout - Page too slow (retrying...)");
            } catch (Exception e) {
                log("❌ Vote failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
            
            // Progressive backoff
            try { Thread.sleep(3000 + (retry * 2000)); } catch (InterruptedException ignored) {}
        }
        log("💥 Vote #" + attempt + " FAILED after " + maxRetries + " retries");
        return false;
    }

    private static long parseMinutes(String duration) {
        if (duration == null) return 50;
        if (duration.endsWith("m")) {
            return Long.parseLong(duration.replace("m", ""));
        }
        return 50;
    }

    private static void log(String message) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
            String logEntry = "[" + timestamp + "] " + message + "\n";
            System.out.print(logEntry);
            
            if (logWriter == null) {
                logWriter = new FileWriter(LOG_FILE, true);
            }
            logWriter.write(logEntry);
            logWriter.flush();
        } catch (Exception e) {
            System.err.println("LOG ERROR: " + e.getMessage());
        }
    }

    private static void cleanup() {
        if (driver != null) {
            try { driver.quit(); } catch (Exception ignored) {}
        }
        try {
            if (logWriter != null) {
                logWriter.close();
            }
        } catch (Exception ignored) {}
        log("🛑 CLEAN SHUTDOWN COMPLETE");
    }
}
