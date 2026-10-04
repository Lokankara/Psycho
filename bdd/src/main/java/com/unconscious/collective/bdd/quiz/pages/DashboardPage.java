package com.unconscious.collective.bdd.quiz.pages;

public final class DashboardPage {

    private DashboardPage() {
    }

    public static String titleLocator() {
        return Locators.DASHBOARD_TITLE;
    }

    public static String octantBadgeLocator() {
        return Locators.DASHBOARD_OCTANT_BADGE;
    }

    public static String coordinateLabelLocator() {
        return Locators.DASHBOARD_COORDINATE_LABEL;
    }

    public static String historyLinkLocator() {
        return Locators.DASHBOARD_HISTORY_LINK;
    }
}
