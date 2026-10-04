package com.unconscious.collective.qa.ui.pages;

public final class QuizViewPage {

    private QuizViewPage() {
    }

    public static String questionLocator() {
        return Locators.QUIZ_QUESTION;
    }

    public static String negativeButtonLocator() {
        return Locators.QUIZ_NEGATIVE_BUTTON;
    }

    public static String positiveButtonLocator() {
        return Locators.QUIZ_POSITIVE_BUTTON;
    }

    public static String submitButtonLocator() {
        return Locators.QUIZ_SUBMIT_BUTTON;
    }
}
