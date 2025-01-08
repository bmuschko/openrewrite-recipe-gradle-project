package com.bmuschko;

import org.openrewrite.RecipeRun;
import org.openrewrite.Result;
import org.openrewrite.SourceFile;

import java.util.Objects;

public class RecipeRunUtil {
    private RecipeRunUtil() {}

    public static  <S extends SourceFile> S result(RecipeRun run, Class<S> clazz, String endsWith) {
        return run.getChangeset().getAllResults().stream()
                .map(Result::getAfter)
                .filter(Objects::nonNull)
                .filter(r -> r.getSourcePath().endsWith(endsWith))
                .findFirst()
                .map(clazz::cast)
                .orElseThrow();
    }
}
