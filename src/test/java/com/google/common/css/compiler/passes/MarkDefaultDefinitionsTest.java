/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.css.compiler.passes;

import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.FunctionalTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MarkDefaultDefinitions}.
 *
 * @author oana@google.com (Oana Florescu)
 */
class MarkDefaultDefinitionsTest extends FunctionalTestBase {

    @ParameterizedTest
    @ValueSource(strings = {
            "/* @default */ @def COLOR red;",
            "/* @default */ @def PADDING 2px 3px 5px 1px;",
            "@def PADDING /* @default */ 2px 3px 5px 1px;"
    })
    void testMarkDefaultDefinitions(String value) {
        parseAndBuildTree(value);
        runPass();

        CssDefinitionNode definition =
                (CssDefinitionNode) tree.getRoot().getBody().getChildren().get(0);

        // Check each of the value nodes in the definition.
        for (CssValueNode node : definition.getParameters()) {
            assertThat(node.getIsDefault()).isTrue();
        }
    }

    @Test
    void testMarkDefaultDefinitions() {
        parseAndBuildTree(
                "@def PADDING 2px 3px 5px 1px;");
        runPass();

        CssDefinitionNode definition =
                (CssDefinitionNode) tree.getRoot().getBody().getChildren().get(0);

        // Check each of the value nodes in the definitions.
        for (CssValueNode node : definition.getParameters()) {
            assertThat(node.getIsDefault()).isFalse();
        }
    }

    @Override
    protected void runPass() {
        MarkDefaultDefinitions pass = new MarkDefaultDefinitions(
                tree.getVisitController());
        pass.runPass();
    }
}
