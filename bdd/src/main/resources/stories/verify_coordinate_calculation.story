Narrative:
As a semantic coordinate engine
I want coordinate calculation to be deterministic
So that identical answer sets always yield identical coordinates and octants

Scenario: Identical positive answers produce identical coordinates
Given an identical set of quiz answers
When the assessment is scored twice
Then both runs return identical X, Y, Z coordinates and the same Octant
And both runs log an execution trace JSON file

Scenario: Balanced answers on an axis cancel to zero
Given a balanced set of answers for the Z axis
When the assessment is scored
Then the Z coordinate is 0.0 and the Octant is PROPHET_IDEOLOGUE

Scenario: Null pole choices are skipped without error
Given an assessment with a null pole choice on one question
When the assessment is scored
Then no exception is thrown and the coordinate is computed from answered questions only

Scenario: Duplicate question IDs use last non-null pole
Given a duplicate answer for the same question ID
When the assessment is scored
Then the last non-null pole is used and the coordinate count is not inflated
