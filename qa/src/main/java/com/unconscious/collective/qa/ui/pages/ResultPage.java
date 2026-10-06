package com.unconscious.collective.qa.ui.pages;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public final class ResultPage extends BasePage {

    public ResultPage() {
        super();
    }

    public ResultPage waitForLoaded() {
        $(Locators.DASHBOARD_OCTANT_BADGE).shouldBe(visible);
        $(Locators.DASHBOARD_COORDINATE_LABEL).shouldBe(visible);
        return this;
    }

    public String octantBadge() {
        return $(Locators.DASHBOARD_OCTANT_BADGE).getText();
    }

    public String coordinateLabel() {
        return $(Locators.DASHBOARD_COORDINATE_LABEL).getText();
    }

    public String axisValue(String axis) {
        return $(Locators.AXIS_VALUE.replace("{axis}", axis)).getText();
    }

    public void checkAnswer() {
        //TODO
    }
}
