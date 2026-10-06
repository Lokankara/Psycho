package com.unconscious.collective.qa;

public final class BddSupport {

    private BddSupport() {
    }

    public static void verify(String description, Runnable assertions) {
        try {
            assertions.run();
        } catch (AssertionError | RuntimeException failure) {
            Report.writeFailure(description, failure);
            ScreenShot.saveToResult(description);
            throw failure;
        }
    }
}
