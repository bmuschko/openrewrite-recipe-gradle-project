package com.bmuschko;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.gradle.Assertions.buildGradle;

public class GenerateGradleProjectWithWrapperTest implements RewriteTest {
    @Test
    void canGenerateFromYaml() {
        rewriteRun(
                spec -> spec.recipeFromYaml(
                        """
                        type: specs.openrewrite.org/v1beta/recipe
                        name: com.bmuschko.GenerateGradleProjectWithWrapper
                        displayName: Generate Gradle project with wrapper
                        description: Generate a Gradle project with a wrapper.
                        recipeList:
                          - com.bmuschko.GenerateGradleProject
                          - org.openrewrite.gradle.UpdateGradleWrapper:
                              version: 8.12
                              addIfMissing: true
                        """, "com.bmuschko.GenerateGradleProjectWithWrapper"
                ).afterRecipe(run -> {
                    // The recipe should generate a build.gradle file and 4 Gradle wrapper files
                    assertThat(run.getChangeset().getAllResults()).hasSize(5);
                }),
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
