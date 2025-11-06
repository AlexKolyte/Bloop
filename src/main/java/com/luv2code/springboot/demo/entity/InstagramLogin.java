package com.luv2code.springboot.demo.entity;

import com.luv2code.springboot.demo.controller.InstagramController;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

public class InstagramLogin {

    public static void main(String[] args) {
        String username = "ssashh_1";
        String password = "17042007Dd";

        // 1. Setup driver
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions()
                .addArguments(
                        "--headless=new",               // headless mode
                        "--disable-gpu",
                        "--window-size=1920,1080",
                        "--lang=en-GB"
                );
        WebDriver driver = new ChromeDriver(opts);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            // 2. Load login page
            driver.get("https://www.instagram.com/accounts/login/");

            // 3. Dismiss cookie banner if present
            try {
                By cookieBtn = By.xpath(
                        "//button[text()='Allow all cookies' or text()='Only allow essential cookies']"
                );
                WebElement c = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(cookieBtn));
                c.click();
                wait.until(ExpectedConditions.invisibilityOf(c));
            } catch (Exception ignored) { }

            // 4. Wait for the username field
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));

            // 5. Fill credentials
            driver.findElement(By.name("username")).sendKeys(username);
            driver.findElement(By.name("password")).sendKeys(password);

            // 6. Click Login
            WebElement loginBtn = wait.until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']"))
            );
            loginBtn.click();

            // 7. Dismiss any immediate “Not Now” dialog
            try {
                WebElement notNow = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//button[text()='Not Now']")));
                notNow.click();
            } catch (Exception ignored) { }

            // 8. Wait for your feed to load (search bar visible)
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("input[placeholder='Search']")
            ));

            // 9. Grab Instagram’s sessionid cookie
            Cookie session = driver.manage().getCookieNamed("sessionid");
            if (session != null) {
                System.out.println("sessionid=" + session.getValue());
                InstagramController.sessionId = session.getValue();
            } else {
                System.err.println("sessionid not found—login may have failed.");
            }
        }
        catch (TimeoutException e) {
            System.err.println("Timeout during login flow: " + e.getMessage());
            takeScreenshot(driver, "instagram-login-failure.png");
        }
        finally {
            driver.quit();
        }
    }

    private static void takeScreenshot(WebDriver driver, String fileName) {
        try {
            File shot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            File target = Paths.get(System.getProperty("user.dir"), fileName).toFile();
            org.apache.commons.io.FileUtils.copyFile(shot, target);
            System.out.println("Saved screenshot: " + target.getAbsolutePath());
        } catch (Exception ex) {
            System.err.println("Screenshot failed: " + ex.getMessage());
        }
    }
}
