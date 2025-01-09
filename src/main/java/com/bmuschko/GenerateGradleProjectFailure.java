package com.bmuschko;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NonNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.gradle.GradleParser;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;

@Value
@EqualsAndHashCode(callSuper = false)
public class GenerateGradleProjectFailure extends ScanningRecipe<GenerateGradleProject.Accumulator> {
    @Override
    public String getDisplayName() {
        return "Generate Gradle project";
    }

    @Override
    public String getDescription() {
        return "Generates Gradle project files if they do not exist.";
    }

    public static class Accumulator {
    }

    @Override
    public GenerateGradleProject.Accumulator getInitialValue(ExecutionContext ctx) {
        return new GenerateGradleProject.Accumulator();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(GenerateGradleProject.Accumulator acc) {
        return new TreeVisitor<>() {
            @Override
            public Tree visit(@NonNull Tree tree, ExecutionContext ctx) {
                return tree;
            }
        };
    }

    @Override
    public Collection<? extends SourceFile> generate(GenerateGradleProject.Accumulator acc, ExecutionContext ctx) {
        SourceFile buildFile = generateBuildFile(ctx);
        return List.of(buildFile);
    }

    private SourceFile generateBuildFile(ExecutionContext ctx) {
        String buildFileContent = """
                plugins {
                    id 'java'
                }
                
                def defaultVersionCatalog = file("lib-versions.properties")
                println(defaultVersionCatalog)
                """;

        return GradleParser.builder().build()
                .parseInputs(singletonList(Parser.Input.fromString(Paths.get("build.gradle"), buildFileContent)), null, ctx)
                .findFirst().get();
    }
}

