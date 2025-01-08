package com.bmuschko;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.openrewrite.gradle.Assertions.buildGradle;

public class GenerateGradleProjectWithoutWrapperTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new GenerateGradleProject(false));
    }

    @Test
    void doesNotGenerateGradleBuildFileForExistingGradleProject() {
        rewriteRun(
                recipeSpec -> recipeSpec.afterRecipe(run -> {
                    assertThat(run.getChangeset().size()).isEqualTo(0);
                }),
                buildGradle(
                        """
                        plugins {
                            id 'java'
                        }
                        """
                )
        );
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
                        """
                )
        );
    }
}
