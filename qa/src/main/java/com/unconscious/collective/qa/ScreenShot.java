package com.unconscious.collective.qa;

import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ScreenShot {

    public static final Path SCREENSHOT_DIR = Path.of("..", "logs", "e2e", "screenshots");

    private static final Logger LOG = LoggerFactory.getLogger(ScreenShot.class);

    public static void saveToResult(String description) {
        //TODO SAVE to folder
    }

    public void takeScreenshot(String screenshotName) {
        if (!WebDriverRunner.hasWebDriverStarted()) {
            LOG.error("Screenshot was not created");
            return;
        }
        File screenshotFile = getScreenshotFile(normalizeFileName(screenshotName));
        takeFullPageScreenshot(screenshotFile);
    }

    private void takeFullPageScreenshot(File screenshotFile) {
        WebDriver webDriver = WebDriverRunner.getWebDriver();
        if (!(webDriver instanceof TakesScreenshot screenshots)) {
            LOG.error("Screenshot was not created");
            return;
        }
        File captured = screenshots.getScreenshotAs(OutputType.FILE);
        try {
            Files.createDirectories(screenshotFile.toPath().getParent());
            Files.copy(captured.toPath(), screenshotFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOG.info("Screenshot written to {}", screenshotFile);
        } catch (IOException copyFailure) {
            LOG.warn("Unable to write screenshot to {}: {}", screenshotFile, copyFailure.getMessage());
        }
    }

    private File getScreenshotFile(String normalizedName) {
        return SCREENSHOT_DIR.resolve(normalizedName + ".png").toFile();
    }

    private String normalizeFileName(String screenshotName) {
        return screenshotName.replaceAll("[^a-zA-Z0-9._-]+", "_");
    }
}
