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

import com.google.common.css.compiler.ast.CssForLoopRuleNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssLoopVariableNode;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CreateForLoopNodes}.
 */
class CreateForLoopNodesTest extends NewFunctionalTestBase {

    @Override
    protected void runPass() {
        CreateForLoopNodes pass = new CreateForLoopNodes(
                tree.getMutatingVisitController(), errorManager);
        pass.runPass();
    }

    @Test
    void testCreateLoopNodeWithNumbers() throws Exception {
        parseAndRun("@for $i from 1 to 3 step 2 {}");
        assertThat(getFirstActualNode()).isInstanceOf(CssForLoopRuleNode.class);
        CssForLoopRuleNode loop = (CssForLoopRuleNode) getFirstActualNode();
        assertThat(loop.getFrom()).hasToString("1");
        assertThat(loop.getTo()).hasToString("3");
        assertThat(loop.getStep()).hasToString("2");
    }

    @Test
    void testCreateLoopNodeWithVariables() throws Exception {
        parseAndRun("@for $i from $x to $y step $z {}");
        assertThat(getFirstActualNode()).isInstanceOf(CssForLoopRuleNode.class);
        CssForLoopRuleNode loop = (CssForLoopRuleNode) getFirstActualNode();
        assertThat(loop.getFrom()).isInstanceOf(CssLoopVariableNode.class);
        assertThat(loop.getFrom().getValue()).isEqualTo("$x");
        assertThat(loop.getTo()).isInstanceOf(CssLoopVariableNode.class);
        assertThat(loop.getTo().getValue()).isEqualTo("$y");
        assertThat(loop.getStep()).isInstanceOf(CssLoopVariableNode.class);
        assertThat(loop.getStep().getValue()).isEqualTo("$z");
    }

    @Test
    void testCreateLoopNodeWithConstants() throws Exception {
        parseAndRun("@for $i from X to Y step Z {}");
        assertThat(getFirstActualNode()).isInstanceOf(CssForLoopRuleNode.class);
        CssForLoopRuleNode loop = (CssForLoopRuleNode) getFirstActualNode();
        assertThat(loop.getFrom()).isInstanceOf(CssLiteralNode.class);
        assertThat(loop.getFrom().getValue()).isEqualTo("X");
        assertThat(loop.getTo()).isInstanceOf(CssLiteralNode.class);
        assertThat(loop.getTo().getValue()).isEqualTo("Y");
        assertThat(loop.getStep()).isInstanceOf(CssLiteralNode.class);
        assertThat(loop.getStep().getValue()).isEqualTo("Z");
    }

    @Test
    void testLoopWithoutBlock() throws Exception {
        parseAndRun("@for $i from 1 to 2;", "@for with no block");
    }

    @Test
    void testLoopWithoutAllParameters() throws Exception {
        parseAndRun("@for $i from 1 to {}", CreateForLoopNodes.SYNTAX_ERROR);
    }

    @Test
    void testLoopWithoutBadVariablePattern() throws Exception {
        parseAndRun("@for i from 1 to 2 {}", CreateForLoopNodes.SYNTAX_ERROR);
    }

    @Test
    void testLoopThatOverridesVariable() throws Exception {
        parseAndRun(linesToString(
                "@for $i from 1 to 2 {",
                "  @for $i from 4 to 6 {",
                "  }",
                "}"),
                CreateForLoopNodes.OVERRIDE_VARIABLE_NAME);
    }
}
