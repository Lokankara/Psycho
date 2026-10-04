package com.unconscious.collective.qa;

import java.util.List;

import com.unconscious.collective.qa.ui.steps.CoordinateCalculationSteps;
import com.unconscious.collective.qa.ui.steps.QuizViewSteps;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InstanceStepsFactory;

public class StoryRunner extends JUnitStories {

    private static final String STORY_PATH = "stories/verify_coordinate_calculation.story";

    public StoryRunner() {
        MostUsefulConfiguration configuration = new MostUsefulConfiguration();
        configuration.useStoryLoader(new LoadFromClasspath());
        configuration.useStoryReporterBuilder(new StoryReporterBuilder().withDefaultFormats());
        useConfiguration(configuration);
        useStepsFactory(new InstanceStepsFactory(
                configuration,
                new TestHooks(),
                new CoordinateCalculationSteps(),
                new QuizViewSteps()));
        configuredEmbedder().embedderControls().doGenerateViewAfterStories(false);
    }

    @Override
    public List<String> storyPaths() {
        return List.of(STORY_PATH);
    }
}
