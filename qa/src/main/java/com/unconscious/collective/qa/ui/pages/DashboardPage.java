package com.unconscious.collective.qa.ui.pages;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.unconscious.collective.qa.ui.pages.Locators.DASHBOARD_OCTANT_BADGE;

import org.openqa.selenium.WebDriver;

public final class DashboardPage extends BasePage {

    public DashboardPage() {
    }

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public static String titleLocator() {
        return Locators.DASHBOARD_TITLE;
    }

    public static String coordinateLabelLocator() {
        return Locators.DASHBOARD_COORDINATE_LABEL;
    }

    public static String historyLinkLocator() {
        return Locators.DASHBOARD_HISTORY_LINK;
    }

    public int size() {
        return $$("div[data-testid='axis-bar']").size();
    }

    public String octantBadge() {
        return $(DASHBOARD_OCTANT_BADGE).shouldBe(visible).getText();
    }
}
