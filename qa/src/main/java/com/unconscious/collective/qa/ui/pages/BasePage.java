package com.unconscious.collective.qa.ui.pages;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import com.unconscious.collective.qa.Browser;
import com.unconscious.collective.qa.ui.DriverManager;
import org.openqa.selenium.WebDriver;

public class BasePage {

    String home = "/";

    protected final WebDriver driver;

    public BasePage(WebDriver driver) {
        this.driver = driver;
    }

    public BasePage() {
        this.driver = DriverManager.getDriver();
    }

    protected WebDriver getDriver() {
        return driver;
    }

    public QuizPage openQuiz() {
        return new QuizPage(driver);
    }

    public ResultPage openResult() {
        return new ResultPage();
    }

    public HistoryPage openHistory() {
        $(Locators.DASHBOARD_HISTORY_LINK).shouldBe(visible).click();
        return new HistoryPage(driver);
    }

    public WelcomePage clickWelcome() {
        $("").shouldBe(visible).click();
        return new WelcomePage(driver);
    }

    public String checkUrl() {
        return getDriver().getCurrentUrl();
    }

    public void clickHome() {
        $(home).shouldBe(visible).click();
    }

    public WelcomePage openBrowser() {
        Browser.open();
        return new WelcomePage(driver);
    }

    public DashboardPage openDashboardPage() {
        return new DashboardPage(driver);
    }
}
