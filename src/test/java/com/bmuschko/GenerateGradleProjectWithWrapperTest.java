package com.bmuschko;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.openrewrite.properties.tree.Properties;
import org.openrewrite.remote.Remote;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.text.PlainText;

import static com.bmuschko.RecipeRunUtil.result;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.util.GradleWrapper.*;
import static org.openrewrite.java.Assertions.java;

public class GenerateGradleProjectWithWrapperTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new GenerateGradleProject());
    }

    @Test
    void doesNotGenerateGradleBuildFileForExistingGradleProject() {
        rewriteRun(
                recipeSpec -> recipeSpec.afterRecipe(run -> {
                    AssertionsForClassTypes.assertThat(run.getChangeset().size()).isEqualTo(0);
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
                recipeSpec -> recipeSpec.afterRecipe(run -> {
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
                ),
                java(
                        """
                        package com.bmuschko;
                        
                        public class HelloWorld {
                            public static void main(String[] args) {
                                System.out.println("Hello, World!");
                            }
                        }
                        """,
                        spec -> spec.path("src/main/java/com/bmuschko/HelloWorld.java")
                )
        );
    }
}
