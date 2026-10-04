package com.unconscious.collective.qa.ui.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.unconscious.collective.qa.BddSupport;
import com.unconscious.collective.qa.Browser;
import com.unconscious.collective.qa.ui.pages.BasePage;
import com.unconscious.collective.qa.ui.pages.DashboardPage;
import com.unconscious.collective.qa.ui.pages.HistoryPage;
import com.unconscious.collective.qa.ui.pages.ResultPage;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class QuizViewSteps {

    private BasePage page;

    @Given("the browser is open at the base URL")
    public void givenBrowserOpenAtBaseUrl() {
        BddSupport.verify("open browser at base URL", () -> page.openBrowser().checkTab().checkUrl());
    }

    @When("I click the home tab")
    public void whenClickHomeTab() {
        BddSupport.verify("click home tab", () -> page.clickHome());
    }

    @When("I answer all questions with the positive pole")
    public void whenAnswerAllQuestionsPositive() {
        BddSupport.verify("answer all questions positive", () -> page.openResult().checkAnswer());
    }

    @When("I open history")
    public void whenOpenHistory() {
        BddSupport.verify("open history",
                () -> assertEquals("history", page.openHistory().waitForLoaded().checkUrl()));
    }

    @Then("the result dashboard shows an octant badge and the axis bars")
    public void thenResultDashboardRendered() {
        DashboardPage dashboardPage = page.openDashboardPage();
        BddSupport.verify("result dashboard rendered", () -> {
            assertEquals(dashboardPage.octantBadge(), "Octant badge must show a value");
            assertEquals(3, dashboardPage.size(),
                    "Result page must render one axis bar per axis");
        });
    }

    @Then("the last history entry matches the quiz result")
    public void thenHistoryMatchesQuizResult() {
        BddSupport.verify("history matches quiz result", () -> {
            HistoryPage historyPage = page.openHistory();
            String historyOctant = historyPage.lastOctant();
            String historyCoords = historyPage.lastCoordinates();
            ResultPage resultPage = page.openResult();
            String quizOctant = resultPage.octantBadge();
            String quizCoords = resultPage.coordinateLabel();

            assertEquals(quizOctant, historyOctant, "History octant must equal the quiz result octant");
            assertEquals(quizCoords, historyCoords, "History coordinates must equal the quiz result coordinates");
        });
    }

    @AfterScenario
    public void afterScenario() {
        Browser.close();
    }
}
