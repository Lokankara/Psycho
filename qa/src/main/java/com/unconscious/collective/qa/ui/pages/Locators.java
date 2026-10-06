package com.unconscious.collective.qa.ui.pages;

public final class Locators {

    private Locators() {
    }

    public static final String QUIZ_QUESTION = "div[data-testid='quiz-question']";
    public static final String QUIZ_NEGATIVE_BUTTON = "button[data-pole='NEGATIVE']";
    public static final String QUIZ_POSITIVE_BUTTON = "button[data-pole='POSITIVE']";
    public static final String QUIZ_SUBMIT_BUTTON = "button[data-testid='quiz-submit']";

    public static final String DASHBOARD_TITLE = "[data-testid='dashboard-title']";
    public static final String DASHBOARD_OCTANT_BADGE = "span[data-testid='octant-badge']";
    public static final String DASHBOARD_COORDINATE_LABEL = "p[data-testid='coordinate-label']";
    public static final String DASHBOARD_HISTORY_LINK = "a[href='/history']";

    public static final String AXIS_BAR = "div[data-testid='axis-bar']";
    public static final String AXIS_X_BAR = "div[data-axis='X']";
    public static final String AXIS_Y_BAR = "div[data-axis='Y']";
    public static final String AXIS_Z_BAR = "div[data-axis='Z']";
    public static final String AXIS_VALUE = "div[data-testid='axis-value']";

    public static final String BOARD_CONTAINER = "section[data-testid='kanban-board']";
    public static final String BOARD_COLUMN = "section[aria-label$='column']";
    public static final String BOARD_CARD = "article[data-testid^='kanban-card']";
    public static final String BOARD_PI_SELECTOR = "select[data-testid='pi-selector']";
    public static final String BOARD_SPRINT_SELECTOR = "select[data-testid='sprint-selector']";

    public static final String NAV_TEST_LINK = "a[href='/quiz']";
    public static final String WELCOME_HEADING = "h1:has-text('Коллективное бессознательное')";
    public static final String HISTORY_TABLE = "table";
    public static final String HISTORY_DATE = "table tbody tr td:first-child";
    public static final String HISTORY_OCTANT = "table tbody tr td:nth-child(2)";
    public static final String HISTORY_COORDINATES = "table tbody tr td:nth-child(3)";
}
