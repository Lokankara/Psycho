package com.unconscious.collective.qa;

import java.util.Arrays;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public final class Browser {
    private static final String BASE_URL = "https://collective-unconscious.onrender.com";

    private Browser() {
    }

    private static volatile ChromeDriver driver;

    public static synchronized void open() {
        if (driver != null) {
            return;
        }

        ChromeOptions options = new ChromeOptions();
        options.setAcceptInsecureCerts(true);
        options.setExperimentalOption("w3c", true);
        options.addArguments("--test-type");
        options.addArguments("--no-sandbox");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-print-preview");
        options.addArguments("--disable-search-engine-choice-screen");
        options.addArguments("--start-maximized");
        options.addArguments("--headless=new");

        String property = System.getProperty("qaa.chromeOptions", "");
        if (!property.isBlank()) {
            Arrays.stream(property.split(";"))
                    .map(String::trim)
                    .forEach(options::addArguments);
        }

        driver = new ChromeDriver(options);
        WebDriverRunner.setWebDriver(driver);
        Selenide.open(BASE_URL);
    }

    public static synchronized void close() {
        Selenide.closeWebDriver();
    }

    public static boolean isOpen() {
        return driver != null;
    }
}
