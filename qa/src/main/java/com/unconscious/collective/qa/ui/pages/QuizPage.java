package com.unconscious.collective.qa.ui.pages;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import java.time.Duration;

import org.openqa.selenium.WebDriver;

public final class QuizPage extends BasePage {

    private static final Duration QUESTION_TIMEOUT = Duration.ofSeconds(30);

    QuizPage() {
        super();
    }

    QuizPage(WebDriver driver) {
        super(driver);
    }

    public QuizPage waitForLoaded() {
        $(Locators.QUIZ_QUESTION).shouldBe(visible, QUESTION_TIMEOUT);
        $(Locators.QUIZ_POSITIVE_BUTTON).shouldBe(visible, QUESTION_TIMEOUT);
        return this;
    }

    public ResultPage answerAllPositive() {
        $(Locators.QUIZ_POSITIVE_BUTTON).isDisplayed();
        $(Locators.QUIZ_POSITIVE_BUTTON).click();
        return new ResultPage();
    }

    public String currentQuestionText() {
        return $(Locators.QUIZ_QUESTION).getText();
    }
}
