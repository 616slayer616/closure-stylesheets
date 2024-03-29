/*
 * Copyright 2015 Google Inc.
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

import com.google.common.collect.ImmutableMap;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssForLoopRuleNode;
import com.google.common.css.compiler.passes.testing.PassesTestBase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EvaluateCompileConstants}.
 */
class EvaluateCompileConstantsTest extends PassesTestBase {

    private static final ImmutableMap<String, Integer> CONSTANTS =
            ImmutableMap.of("FOO", 2, "BAR", 7, "BAZ", 3);

    @Override
    protected void runPass() {
        new CreateDefinitionNodes(tree.getMutatingVisitController(), errorManager).runPass();
        new CreateConstantReferences(tree.getMutatingVisitController()).runPass();
        new CreateForLoopNodes(tree.getMutatingVisitController(), errorManager).runPass();
        new EvaluateCompileConstants(tree.getMutatingVisitController(), CONSTANTS).runPass();
    }

    @Test
    void testLoopParametersReplacement() throws Exception {
        parseAndRun("@for $i from FOO to BAR step BAZ {}");
        assertThat(getFirstActualNode()).isInstanceOf(CssForLoopRuleNode.class);
        CssForLoopRuleNode loop = (CssForLoopRuleNode) getFirstActualNode();
        assertThat(loop.getFrom()).hasToString("2");
        assertThat(loop.getTo()).hasToString("7");
        assertThat(loop.getStep()).hasToString("3");
    }

    @Test
    void testValueInDefinitionReplacement() throws Exception {
        parseAndRun("@def X FOO;");
        assertThat(getFirstActualNode()).isInstanceOf(CssDefinitionNode.class);
        CssDefinitionNode definition = (CssDefinitionNode) getFirstActualNode();
        assertThat(definition.getChildren()).asList().hasSize(1);
        assertThat(definition.getChildAt(0)).hasToString("2");
    }

    @Test
    void testValueInArgumentReplacement() throws Exception {
        parseAndRun("@def X f(BAR);");
        assertThat(getFirstActualNode()).isInstanceOf(CssDefinitionNode.class);
        CssDefinitionNode definition = (CssDefinitionNode) getFirstActualNode();
        assertThat(definition.getChildren()).asList().hasSize(1);
        assertThat(definition.getChildAt(0)).hasToString("f(7)");
    }
}
