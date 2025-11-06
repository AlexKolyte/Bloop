package com.luv2code.springboot.demo.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InstagramSessionProvider {

    @Value("${instagram.username}")
    private String igUser;
    @Value("${instagram.password}")
    private String igPass;

    private String sessionId;

    public void init() throws InterruptedException {
        ChromeOptions opts = new ChromeOptions()
                .addArguments("--headless","--disable-gpu","--window-size=1920,1080");
        WebDriverManager.chromedriver().setup();
        ChromeDriver driver = new ChromeDriver(opts);
        try {
            driver.get("https://www.instagram.com/accounts/login/");
            Thread.sleep(3000);
            driver.findElement(By.name("username")).sendKeys(igUser);
            driver.findElement(By.name("password")).sendKeys(igPass);
            driver.findElement(By.xpath("//button[@type='submit']")).click();
            Thread.sleep(5000);

            Cookie cookie = driver.manage().getCookieNamed("sessionid");
            if (cookie == null) {
                throw new IllegalStateException("Login failed, no sessionid cookie");
            }
            this.sessionId = cookie.getValue();
            System.out.println("Obtained sessionid: " + sessionId);

        } finally {
            driver.quit();
        }
    }

    public String getSessionId() {
        return sessionId;
    }
}
