/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.javascript.table;

import lombok.Value;
import org.openrewrite.*;
import org.openrewrite.internal.lang.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WIP data table analysis to accelerate implementing language parsing.
 * The analysis is likely generalizable to other languages, and might be moved to a common module after testing.
 */
@Incubating(since = "0.0")
public class ParseExceptionAnalysis extends DataTable<ParseExceptionAnalysis.Row> {

    public ParseExceptionAnalysis(Recipe recipe) {
        super(recipe, "Find and aggregate parsing errors",
                "Finds and aggregates parsing exceptions to fix the most common issues first.");
    }

    @Value
    public static class Row {
        @Column(displayName = "Source file extension",
                description = "The file extension of the source.")
        String fileExtension;

        @Column(displayName = "Node type",
                description = "The type of the node that caused a parsing exception.")
        String nodeType;

        @Column(displayName = "Exception count",
                description = "Count of exceptions of the Node `syntaxKind`.")
        int exceptionCount;
    }

    /**
     * Generate a parsing exception message from a given node type for analysis.
     * @param nodeType A unique name that represents the node type that caused the parsing exception.
     * @return analysis message.
     */
    public static String getAnalysisMessage(String nodeType) {
        return getAnalysisMessage(nodeType, null);
    }

    /**
     * Generate a parsing exception message from a given node type for analysis.
     * @param nodeType A unique name that represents the node type that caused the parsing exception.
     * @param sourceSnippet Optional source snippet to identify where the exception occurred.
     * @return analysis message.
     */
    public static String getAnalysisMessage(String nodeType, @Nullable String sourceSnippet) {
        return "Unable to parse node of type {{" + nodeType + (sourceSnippet != null ? "}} at :" + sourceSnippet : "}}");
    }

    public static String getNodeType(String message) {
        return message.substring(message.indexOf("{{") + 2, message.indexOf("}}"));
    }

    public static String getSourceSnippet(String message) {
        int start = message.indexOf("}} at :") + 7;
        return start > 6 ? message.substring(start) : "";
    }

    public static <S extends SourceFile> void findParserExceptions(List<S> sources, ParseExceptionAnalysis parseExceptionAnalysis, ExecutionContext ctx) {
        Map<String, Map<String, Integer>> counts = new HashMap<>();
        for (S source : sources) {
            new TreeVisitor<Tree, ExecutionContext>() {
                final String extension = source.getSourcePath().toString().substring(source.getSourcePath().toString().lastIndexOf(".") + 1);

                @Override
                public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
                    if (tree == null) {
                        return null;
                    }

                    Tree t = super.visit(tree, ctx);
                    if (t != null) {
                        ParseExceptionResult result = tree.getMarkers().findFirst(ParseExceptionResult.class).orElse(null);
                        if (result != null) {
                            Map<String, Integer> nodeTypeCounts = counts.computeIfAbsent(extension, k -> new HashMap<>());
                            nodeTypeCounts.merge(getNodeType(result.getMessage()), 1, Integer::sum);
                        }
                    }
                    return t;
                }
            }.visit(source, ctx);
        }
        for (Map.Entry<String, Map<String, Integer>> fileExtensionEntries : counts.entrySet()) {
            for (Map.Entry<String, Integer> nodeTypeCounts : fileExtensionEntries.getValue().entrySet()) {
                parseExceptionAnalysis.insertRow(ctx, new Row(fileExtensionEntries.getKey(), nodeTypeCounts.getKey(), nodeTypeCounts.getValue()));
            }
        }
    }
}