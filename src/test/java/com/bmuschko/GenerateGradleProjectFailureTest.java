package com.bmuschko;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;

public class GenerateGradleProjectFailureTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new GenerateGradleProjectFailure());
    }

    @Test
    void generatesGradleBuildFileForNonExistingGradleProject() {
        rewriteRun(
                buildGradle(
                        null,
                        """
                        plugins {
                            id 'java'
                        }

                        def defaultVersionCatalog = file("lib-versions.properties")
                        println(defaultVersionCatalog)
                        """
                )
        );
    }
}
