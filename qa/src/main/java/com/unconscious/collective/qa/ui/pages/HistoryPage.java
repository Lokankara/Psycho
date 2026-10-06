package com.unconscious.collective.qa.ui.pages;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import org.openqa.selenium.WebDriver;

public class HistoryPage extends BasePage {

    public HistoryPage(WebDriver driver) {
        super(driver);
    }

    public HistoryPage waitForLoaded() {
        $(Locators.HISTORY_TABLE).shouldBe(visible);
        return this;
    }

    public String lastOctant() {
        return $$(Locators.HISTORY_OCTANT).last().getText();
    }

    public String lastCoordinates() {
        return $$(Locators.HISTORY_COORDINATES).last().getText();
    }

    public String lastCreatedAt() {
        return $$(Locators.HISTORY_DATE).last().getText();
    }

}
