package com.bmuschko;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.gradle.GradleParser;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

@Value
@EqualsAndHashCode(callSuper = false)
public class GenerateGradleProject extends ScanningRecipe<GenerateGradleProject.Accumulator> {
    @Override
    public String getDisplayName() {
        return "Generate Gradle project";
    }

    @Override
    public String getDescription() {
        return "Generates Gradle project files if they do not exist.";
    }

    public static class Accumulator {
        boolean gradleProjectFound;
    }

    @Override
    public Accumulator getInitialValue(ExecutionContext ctx) {
        return new Accumulator();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Accumulator acc) {
        return new TreeVisitor<>() {
            @Override
            public @Nullable Tree preVisit(@NonNull Tree tree, ExecutionContext executionContext) {
                stopAfterPreVisit();
                SourceFile sourceFile = (SourceFile) requireNonNull(tree);

                if (sourceFile.getSourcePath().toString().endsWith("build.gradle") ||
                        sourceFile.getSourcePath().toString().endsWith("build.gradle.kts")) {
                    acc.gradleProjectFound = true;
                }

                return tree;
            }
        };
    }

    @Override
    public Collection<? extends SourceFile> generate(Accumulator acc, ExecutionContext ctx) {
        if (acc.gradleProjectFound) {
            return Collections.emptyList();
        }

        String buildFileContent = """
                plugins {
                    id 'java'
                }
                """;

        return List.of(GradleParser.builder().build()
                .parseInputs(singletonList(Parser.Input.fromString(Paths.get("build.gradle"), buildFileContent)), null, ctx)
                .findFirst().get());
    }
}
