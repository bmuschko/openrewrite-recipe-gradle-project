package com.bmuschko;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Parser;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.gradle.GradleParser;
import org.openrewrite.gradle.UpdateGradleWrapper;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
public class GenerateGradleProject extends ScanningRecipe<GenerateGradleProject.Accumulator> {
    @Getter
    @Option(displayName = "Generates a Gradle wrapper",
            description = "Generates a Gradle wrapper. Defaults to `true`.",
            required = false)
    @Nullable
    Boolean addWrapper;

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
            public @Nullable Tree preVisit(@NonNull Tree tree, ExecutionContext ctx) {
                stopAfterPreVisit();
                SourceFile sourceFile = (SourceFile) requireNonNull(tree);

                if (sourceFile.getSourcePath().getFileName().toString().equals("build.gradle") ||
                        sourceFile.getSourcePath().getFileName().toString().equals("build.gradle.kts")) {
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

        List<SourceFile> allSourceFiles = new ArrayList<>();

        if (shouldGenerateWrapperFiles()) {
            allSourceFiles.addAll(generateWrapperFiles(ctx));
        }

        SourceFile buildFile = generateBuildFile(ctx);
        allSourceFiles.add(buildFile);

        return allSourceFiles;
    }

    private boolean shouldGenerateWrapperFiles() {
        return addWrapper == null || Boolean.TRUE.equals(addWrapper);
    }

    private Collection<SourceFile> generateWrapperFiles(ExecutionContext ctx) {
        UpdateGradleWrapper.GradleWrapperState wrapperState = new UpdateGradleWrapper.GradleWrapperState();
        wrapperState.setGradleProject(true);
        return new UpdateGradleWrapper("8.12", "bin", true, null, null).generate(wrapperState, ctx);
    }

    private SourceFile generateBuildFile(ExecutionContext ctx) {
        String buildFileContent = """
                plugins {
                    id 'java'
                }
                """;

        return GradleParser.builder().build()
                .parseInputs(singletonList(Parser.Input.fromString(Paths.get("build.gradle"), buildFileContent)), null, ctx)
                .findFirst().get();
    }
}
