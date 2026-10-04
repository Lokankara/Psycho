Narrative:
As a user of the quiz application
I want to complete the quiz and verify my result in history
So that I can trust the scoring is persisted correctly

Scenario: Full quiz flow with history assertion
Given the browser is open at the base URL
When I click the test tab
And I answer all questions with the positive pole
And I open history
Then the last history entry matches the quiz result
