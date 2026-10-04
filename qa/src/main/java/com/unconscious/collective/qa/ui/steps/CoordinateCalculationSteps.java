package com.unconscious.collective.qa.ui.steps;

import com.unconscious.collective.qa.BddSupport;
import com.unconscious.collective.qa.ui.pages.QuizPage;
import com.unconscious.collective.qa.ui.pages.ResultPage;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class CoordinateCalculationSteps {

    private QuizPage quizPage;
    private ResultPage resultPage;

    @Given("the deployed quiz page is open")
    public void givenDeployedQuizPageOpen() {
        BddSupport.verify("deployed quiz page is open", () -> {
            quizPage = quizPage.openQuiz();
        });
    }

    @When("every question is answered with the positive pole")
    public void whenEveryQuestionAnsweredPositive() {
        BddSupport.verify("every question answered positive", () -> {
            quizPage.waitForLoaded();
            resultPage = quizPage.answerAllPositive();
        });
    }

    @Then("the result dashboard shows an octant badge and the axis bars")
    public void thenResultDashboardRendered() {
        BddSupport.verify("result dashboard rendered", () -> {
            resultPage.waitForLoaded();
        });
    }
}
