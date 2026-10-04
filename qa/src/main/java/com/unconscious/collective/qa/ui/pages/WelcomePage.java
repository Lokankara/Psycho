package com.unconscious.collective.qa.ui.pages;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.WebDriver;

public final class WelcomePage extends BasePage {

    WelcomePage() {
        super();
    }

    WelcomePage(WebDriver driver) {
        super(driver);
    }

    public QuizPage clickTab() {
        $(Locators.NAV_TEST_LINK).shouldBe(visible).click();
        return new QuizPage();
    }

    public boolean isLoaded() {
        return $(Locators.WELCOME_HEADING).exists();
    }

    public WelcomePage checkTab() {
        // TODO        $(cssTab).shouldBe(title);
        return this;
    }
}
