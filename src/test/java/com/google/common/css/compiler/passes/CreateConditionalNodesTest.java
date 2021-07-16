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

import com.google.common.css.compiler.ast.*;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CreateConditionalNodes}.
 */
@SuppressWarnings("java:S2699")
class CreateConditionalNodesTest extends NewFunctionalTestBase {

    @Override
    protected void runPass() {
        CreateConditionalNodes pass = new CreateConditionalNodes(
                tree.getMutatingVisitController(), errorManager);
        pass.runPass();
    }

    @Test
    void testCreateSimpleConditionalBlockNode() throws Exception {
        parseAndRun("@if (!X){ a {b: c} } @else { d {e: f} }");
        assertThat(getFirstActualNode()).isInstanceOf(CssConditionalBlockNode.class);
        CssConditionalBlockNode condBlock =
                (CssConditionalBlockNode) getFirstActualNode();
        CssConditionalRuleNode condRuleIf = condBlock.getChildren().get(0);
        CssConditionalRuleNode condRuleElse = condBlock.getChildren().get(1);
        assertThat(condRuleIf.getName().getValue()).isEqualTo("if");
        assertThat(condRuleIf.getParametersCount()).isEqualTo(1);
        assertThat(condRuleIf.getBlock()).hasToString("[[a]{[b:[c]]}]");
        assertThat(condRuleElse.getName().getValue()).isEqualTo("else");
        assertThat(condRuleElse.getParametersCount()).isZero();
        assertThat(condRuleElse.getBlock()).hasToString("[[d]{[e:[f]]}]");
    }

    @Test
    void testCreateNestedConditionalBlockNode() throws Exception {
        parseAndRun("@if X {a {b: c} } @else { @if (Y) {d {e: f} } }");
        assertThat(getFirstActualNode()).isInstanceOf(CssConditionalBlockNode.class);
        CssConditionalBlockNode condBlock = (CssConditionalBlockNode) getFirstActualNode();
        assertThat(condBlock.getChildren()).asList().hasSize(2);
        CssConditionalRuleNode condRuleIf = condBlock.getChildren().get(0);
        CssConditionalRuleNode condRuleElse = condBlock.getChildren().get(1);
        assertThat(condRuleIf.getName().getValue()).isEqualTo("if");
        assertThat(condRuleIf.getParametersCount()).isEqualTo(1);
        assertThat(condRuleIf.getBlock()).hasToString("[[a]{[b:[c]]}]");
        assertThat(condRuleElse.getName().getValue()).isEqualTo("else");
        assertThat(condRuleElse.getParametersCount()).isZero();
        assertThat(condRuleElse.getBlock().getChildren()).asList().hasSize(1);
        CssNode child = condRuleElse.getBlock().getChildren().get(0);
        assertThat(child).isInstanceOf(CssConditionalBlockNode.class);
        CssConditionalBlockNode elseCondBlock = (CssConditionalBlockNode) child;
        assertThat(elseCondBlock.getChildren()).asList().hasSize(1);
        CssConditionalRuleNode elseCondRuleIf = elseCondBlock.getChildren().get(0);
        assertThat(elseCondRuleIf.getName().getValue()).isEqualTo("if");
        assertThat(elseCondRuleIf.getParametersCount()).isEqualTo(1);
        assertThat(elseCondRuleIf.getBlock()).hasToString("[[d]{[e:[f]]}]");
    }

    @Test
    void testCreateConditionalBlockNodeInRuleset() throws Exception {
        parseAndRun("a {@if X {b: c} @else {d: e} }");
        assertThat(getFirstActualNode()).isInstanceOf(CssRulesetNode.class);
        CssRulesetNode ruleset = (CssRulesetNode) getFirstActualNode();
        assertThat(ruleset).hasToString("[a]{[[@if[X]{[b:[c]]}, @else[]{[d:[e]]}]]}");
        CssDeclarationBlockNode declarationBlock = ruleset.getDeclarations();
        assertThat(declarationBlock.getChildren()).asList().hasSize(1);
        assertThat(declarationBlock.getChildAt(0)).isInstanceOf(CssConditionalBlockNode.class);
        CssConditionalBlockNode condBlock = (CssConditionalBlockNode) declarationBlock.getChildAt(0);
        assertThat(condBlock.getChildren()).asList().hasSize(2);
        CssConditionalRuleNode condRuleIf = condBlock.getChildren().get(0);
        CssConditionalRuleNode condRuleElse = condBlock.getChildren().get(1);
        assertThat(condRuleIf.getName().getValue()).isEqualTo("if");
        assertThat(condRuleIf.getParametersCount()).isEqualTo(1);
        assertThat(condRuleIf.getBlock()).hasToString("[b:[c]]");
        assertThat(condRuleElse.getName().getValue()).isEqualTo("else");
        assertThat(condRuleElse.getParametersCount()).isZero();
        assertThat(condRuleElse.getBlock()).hasToString("[d:[e]]");
    }

    @Test
    void testIfWithoutBlockError() throws Exception {
        parseAndRun("@if (X) ;", "@if without block");
    }

    @Test
    void testIfWithoutConditionError() throws Exception {
        parseAndRun("@if {a {b: c} }", "@if without condition");
    }

    @Test
    void testIfWithTooManyParametersError() throws Exception {
        parseAndRun("@if X Y {a {b: c}}", "@if with too many parameters");
    }

    @Test
    void testElseTooManyParametersError() throws Exception {
        parseAndRun("@if (X) {a {b: c}} @else (Y) {a {b: c}}", "@else with too many parameters");
    }

    @Test
    void testElseWithoutIfError() throws Exception {
        parseAndRun("@else {a {b: c}}", "@else without previous @if");
    }

    @Test
    void testElseIfAfterElseError() throws Exception {
        parseAndRun("@if (X) {a {b: c}} @else {a {b: c}} @elseif (Y) {a {b: c}}",
                "@elseif without previous @if");
    }

    @Test
    void testElseAfterRuleError() throws Exception {
        parseAndRun("@if (X && Y) {a {b: c}} a {b: c} @else {a {b: c}}",
                "@else without previous @if");
    }

    @Test
    void testNestedElseWithoutIfError() throws Exception {
        parseAndRun("@if X { @else {a {b: c}} }",
                "@else without previous @if");
    }
}
