package com.bmuschko;

import org.junit.jupiter.api.Test;
import org.openrewrite.properties.tree.Properties;
import org.openrewrite.remote.Remote;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.text.PlainText;

import static com.bmuschko.RecipeRunUtil.result;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.util.GradleWrapper.*;

public class GenerateGradleProjectYamlTest implements RewriteTest {
    @Test
    void canGenerateFromYamlWithoutWrapper() {
        rewriteRun(
                spec -> spec.recipeFromYaml(
                        """
                        type: specs.openrewrite.org/v1beta/recipe
                        name: com.bmuschko.GenerateGradleProjectWithoutWrapper
                        displayName: Generate Gradle project without wrapper
                        description: Generate a Gradle project without a wrapper.
                        recipeList:
                          - com.bmuschko.GenerateGradleProject:
                              addWrapper: false
                        """, "com.bmuschko.GenerateGradleProjectWithoutWrapper"
                ),
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

    @Test
    void canGenerateFromYamlWithWrapper() {
        rewriteRun(
                spec -> spec.recipeFromYaml(
                        """
                        type: specs.openrewrite.org/v1beta/recipe
                        name: com.bmuschko.GenerateGradleProjectWithWrapper
                        displayName: Generate Gradle project without wrapper
                        description: Generate a Gradle project without a wrapper.
                        recipeList:
                          - com.bmuschko.GenerateGradleProject
                        """, "com.bmuschko.GenerateGradleProjectWithWrapper"
                ).afterRecipe(run -> {
                    assertThat(run.getChangeset().size()).isEqualTo(5);

                    var gradleSh = result(run, PlainText.class, "gradlew");
                    assertThat(gradleSh.getSourcePath()).isEqualTo(WRAPPER_SCRIPT_LOCATION);

                    var gradleBat = result(run, PlainText.class, "gradlew.bat");
                    assertThat(gradleBat.getSourcePath()).isEqualTo(WRAPPER_BATCH_LOCATION);

                    var gradleWrapperProperties = result(run, Properties.File.class, "gradle-wrapper.properties");
                    assertThat(gradleWrapperProperties.getSourcePath()).isEqualTo(WRAPPER_PROPERTIES_LOCATION);

                    var gradleWrapperJar = result(run, Remote.class, "gradle-wrapper.jar");
                    assertThat(gradleWrapperJar.getSourcePath()).isEqualTo(WRAPPER_JAR_LOCATION);
                    assertThat(gradleWrapperJar.getSourcePath().toFile().length()).isGreaterThan(0);
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
