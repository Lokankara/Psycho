package com.unconscious.collective.qa.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;

@Disabled
class HealthCheckTest {

    @BeforeAll
    static void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void quizApiQuestionsIsAvailable() {
        RestAssured.port = 8080;

        given().accept("application/json")
                .when().get("/api/analysis/questions")
                .then()
                .statusCode(200)
                .body("size()", notNullValue());
    }

    @Test
    void quizApiAnalyzeIsAvailable() {
        RestAssured.port = 8080;

        given()
                .accept("application/json")
                .contentType("application/json")
                .body("{\"sessionId\":\"health-check\",\"answers\":[]}")
                .when()
                .post("/api/analysis/analyze")
                .then()
                .statusCode(200)
                .body("objectId", notNullValue());
    }

    @Test
    void quizApiResultsIsAvailable() {
        RestAssured.port = 8080;

        given()
                .accept("application/json")
                .contentType("application/json")
                .body("{\"sessionId\":\"health-check\",\"answers\":[]}")
                .when()
                .post("/api/analysis/results")
                .then()
                .statusCode(200)
                .body("objectId", notNullValue());
    }

    @Test
    void quizApiHistoryIsAvailable() {
        RestAssured.port = 8080;

        given()
                .accept("application/json")
                .when()
                .get("/api/analysis/history")
                .then()
                .statusCode(200);
    }

    @Test
    void quizApiRootAnalyzeIsAvailable() {
        RestAssured.port = 8080;

        given()
                .accept("application/json")
                .contentType("application/json")
                .body("{\"answers\":{}}")
                .when()
                .post("/api/analysis")
                .then()
                .statusCode(200)
                .body("objectId", notNullValue());
    }

    @Test
    void agileApiBoardIsAvailable() {
        RestAssured.port = 8081;

        given()
                .accept("application/json")
                .when()
                .get("/api/agile/board")
                .then()
                .statusCode(200)
                .body("columns", notNullValue());
    }

    @Test
    void agileApiBddExecutionIsAvailable() {
        RestAssured.port = 8081;

        given()
                .accept("application/json")
                .contentType("application/json")
                .body("{\"bddStory\":\"verify_coordinate_calculation.story\",\"status\":\"PASSED\"}")
                .when()
                .post("/api/agile/bdd-execution")
                .then()
                .statusCode(200)
                .body("storiesUpdated", notNullValue())
                .body("tasksUpdated", notNullValue());
    }

    @Test
    void agileApiMoveStoryIsAvailable() {
        RestAssured.port = 8081;

        given()
                .accept("application/json")
                .contentType("application/json")
                .body("{\"columnCode\":\"TO_DO\"}")
                .when()
                .patch("/api/agile/stories/1/column")
                .then()
                .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void agileApiMoveTaskIsAvailable() {
        RestAssured.port = 8081;

        given()
                .accept("application/json")
                .contentType("application/json")
                .body("{\"columnCode\":\"TO_DO\"}")
                .when()
                .patch("/api/agile/tasks/1/column")
                .then()
                .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void spaRootIsAvailable() {
        RestAssured.port = 8080;

        given()
                .accept("text/html")
                .when()
                .get("/")
                .then()
                .statusCode(200);
    }

    @Test
    void spaQuizIsAvailable() {
        RestAssured.port = 8080;

        given()
                .accept("text/html")
                .when()
                .get("/quiz")
                .then()
                .statusCode(200);
    }

    @Test
    void spaHistoryIsAvailable() {
        RestAssured.port = 8080;

        given()
                .accept("text/html")
                .when()
                .get("/history")
                .then()
                .statusCode(200);
    }

    @Test
    void spaAssessmentIsAvailable() {
        RestAssured.port = 8080;

        given()
                .accept("text/html")
                .when()
                .get("/assessment")
                .then()
                .statusCode(200);
    }

    @Test
    void spaBoardIsAvailable() {
        RestAssured.port = 8080;

        given()
                .accept("text/html")
                .when()
                .get("/board")
                .then()
                .statusCode(200);
    }

    @Test
    void spaResultIsAvailable() {
        RestAssured.port = 8080;

        given()
                .accept("text/html")
                .when()
                .get("/result")
                .then()
                .statusCode(200);
    }
}
