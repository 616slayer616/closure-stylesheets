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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssTree.RulesetNodesToRemove;
import com.google.common.css.compiler.ast.VisitController;
import com.google.common.css.compiler.passes.testing.PassesTestBase;
import java.util.Iterator;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link MarkRemovableRulesetNodes}.
 *
 * @author oana@google.com (Oana Florescu)
 */
@RunWith(JUnit4.class)
public class MarkRemovableRulesetNodesTest extends PassesTestBase {

  @Test
  public void testRunPass() {
    VisitController visitController = mock(VisitController.class);
    tree = mock(CssTree.class);
    when(tree.getVisitController()).thenReturn(visitController);

    MarkRemovableRulesetNodes pass = new MarkRemovableRulesetNodes(tree);
    visitController.startVisit(pass);

    pass.runPass();
  }

  @Test
  public void testMarkRemovableRulesetNode1() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE {",
              "  border: 2px;",
              "}",
              ".CSS_RULE {",
              "  border: 3px;",
              "}"), null, null);
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(1);

    CssRulesetNode rule = rules.getRulesetNodes().iterator().next();
    checkRuleset("[[.CSS_RULE]{[border:[[2px]];]}]", rule);
  }

  @Test
  public void testMarkRemovableRulesetNode2() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE {",
              "  display: inline-box;",
              "}",
              ".CSS_RULE {",
              "  display: -moz-inline-box;",
              "}"), null, null);
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).isEmpty();
  }

  @Test
  public void testMarkRemovableRulesetNode3() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE_1 {",
              "  border: 2px;",
              "}",
              ".CSS_RULE_2 {",
              "  margin: 3px;",
              "}"),
        Sets.newHashSet("CSS_RULE_1"),
        "CSS_");
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(1);

    CssRulesetNode rule = rules.getRulesetNodes().iterator().next();
    checkRuleset("[[.CSS_RULE_2]{[margin:[[3px]];]}]", rule);
  }

  @Test
  public void testMarkRemovableRulesetNode4() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE_1 .CSS_RULE_3 {",
              "  border: 2px;",
              "}",
              ".CSS_RULE_2 {",
              "  margin: 3px;",
              "}"),
        Sets.newHashSet("CSS_RULE_1"),
        "CSS_");
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(2);

    Iterator<CssRulesetNode> iter = rules.getRulesetNodes().iterator();
    CssRulesetNode rule = iter.next();
    checkRuleset("[[.CSS_RULE_2]{[margin:[[3px]];]}]", rule);
    rule = iter.next();
    checkRuleset("[[.CSS_RULE_1 .CSS_RULE_3]{[border:[[2px]];]}]", rule);
  }

  @Test
  public void testMarkRemovableRulesetNode5() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE_1 .CSS_RULE_3 {",
              "  border: 2px;",
              "}",
              ".CSS_RULE_2 {",
              "  margin: 3px;",
              "}"),
        Sets.newHashSet("CSS_RULE_1", "CSS_RULE_3"),
        "CSS_");
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(1);

    CssRulesetNode rule = rules.getRulesetNodes().iterator().next();
    checkRuleset("[[.CSS_RULE_2]{[margin:[[3px]];]}]", rule);
  }

  @Test
  public void testMarkRemovableRulesetNode6() {
    collectRemovableRulesetNodes(
        linesToString(
              "div {",
              "  border: 2px;",
              "}",
              ".CSS_RULE_2 {",
              "  margin: 3px;",
              "}"),
        Sets.newHashSet("CSS_RULE_2"),
        "CSS_");
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).isEmpty();
  }

  @Test
  public void testMarkRemovableRulesetNode7() {
    collectRemovableRulesetNodes(
        linesToString(
              "div .CSS_RULE_1 {",
              "  border: 2px;",
              "}",
              ".CSS_RULE_2 {",
              "  margin: 3px;",
              "}"),
        Sets.newHashSet("CSS_RULE_2"),
        "CSS_");
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(1);

    CssRulesetNode rule = rules.getRulesetNodes().iterator().next();
    checkRuleset("[[div .CSS_RULE_1]{[border:[[2px]];]}]", rule);
  }

  @Test
  public void testMarkRemovableRulesetNode8() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE_1 div {",
              "  border: 2px;",
              "}",
              ".CSS_RULE_2 {",
              "  margin: 3px;",
              "}"),
        Sets.newHashSet("CSS_RULE_2"),
        "CSS_");
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(1);

    CssRulesetNode rule = rules.getRulesetNodes().iterator().next();
    checkRuleset("[[.CSS_RULE_1 div]{[border:[[2px]];]}]", rule);
  }

  @Test
  public void testMarkRemovableRulesetNode9() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE {",
              "  border: 2px;",
              "}",
              ".CSS_RULE {",
              "  border: 3px;",
              "}"),
        Sets.<String>newHashSet(),
        null);
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(1);

    CssRulesetNode rule = rules.getRulesetNodes().iterator().next();
    checkRuleset("[[.CSS_RULE]{[border:[[2px]];]}]", rule);
  }

  @Test
  public void testMarkRemovableRulesetNode10() {
    collectRemovableRulesetNodes(
        linesToString(
              "div {",
              "  border: 2px;",
              "}",
              "html.CSS_RULE {",
              "  border: 3px;",
              "}"),
        Sets.newHashSet(".CSS_RULE_1"),
        "CSS_");
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(1);

    CssRulesetNode rule = rules.getRulesetNodes().iterator().next();
    checkRuleset("[[html.CSS_RULE]{[border:[[3px]];]}]", rule);
  }

  @Test
  public void testMarkRemovableRulesetNode11() {
    collectRemovableRulesetNodes(
        linesToString(
              "selector1 {",
              "  *height : 11px;",
              "}",
              "selector2 {",
              "  *height : 12px;",
              "}",
              "selector1 {",
              "  *height : 13px;",
              "}"),
        Sets.newHashSet("selector1", "selector2"),
        null);
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(1);

    CssRulesetNode rule = rules.getRulesetNodes().iterator().next();
    checkRuleset("[[selector1]{[*height:[[11px]];]}]", rule);
  }

  @Test
  public void testMarkRemovableRulesetNode12() {
    collectRemovableRulesetNodes(
        linesToString(
              "selector1 {",
              "  *foo : 11px;",
              "}",
              "selector1 {",
              "  foo : 13px;",
              "}"),
        Sets.newHashSet("selector1"),
        null);
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).isEmpty();
  }

  @Test
  public void testMarkRemovableRulesetNode13() {
    collectRemovableRulesetNodes(
        linesToString(
              "selector1 {",
              "  foo : 11px;",
              "}",
              "selector1 {",
              "  *foo : 13px;",
              "}",
              "selector2 {",
              "  *foo : 15px;",
              "}"),
        Sets.newHashSet("selector1", "selector2"),
        null);
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).isEmpty();
  }

  @Test
  public void testMarkRemovableRulesetNode14() {
    collectRemovableRulesetNodes(
        linesToString(
              "selector {",
              "  foo : 10px;",
              "}",
              "selector {",
              "  *foo : 11px;", // to be erased (second)
              "}",
              "selector {",
              "  *bar : 12px;",
              "}",
              "selector {",
              "  *foo : 13px;",
              "}",
              "selector {",
              "  bar : 14px;", // to be erased (first)
              "}",
              "selector {",
              "  bar : 15px;",
              "}"),
        Sets.newHashSet("selector"),
        null);
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(2);

    Iterator<CssRulesetNode> iter = rules.getRulesetNodes().iterator();
    CssRulesetNode rule1 = iter.next();
    checkRuleset("[[selector]{[bar:[[14px]];]}]", rule1);

    CssRulesetNode rule2 = iter.next();
    checkRuleset("[[selector]{[*foo:[[11px]];]}]", rule2);
  }

  @Test
  public void testMarkRemovableRulesetNode15() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE {",
              "  cusrsor: pointer;",
              "}",
              ".CSS_RULE {",
              "  cursor: hand;",
              "}"), null, null);
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).isEmpty();
  }

  @Test
  public void testMarkRemovableRulesetNode16() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE {",
              "  width: 12px;",
              "  display: inline;",
              "}",
              ".CSS_RULE {",
              "  width: 10px;",
              "}"), null, null);
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).isEmpty();
  }

  @Test
  public void testMarkRemovableRulesetNodeImportant() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE {",
              "  font: 12px blue !important;",
              "}",
              ".CSS_RULE {",
              "  font: 10px;",
              "}"), null, null);

    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(1);
    CssRulesetNode rule1 = rules.getRulesetNodes().iterator().next();
    checkRuleset("[[.CSS_RULE]{[font:[[10px]];]}]", rule1);
  }

  @Test
  public void testMarkRemovableRulesetNodeComponents() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_C1-CSS_E {",
              "  border: 2px;",
              "}",
              ".CSS_C2-CSS_E {",
              "  margin: 3px;",
              "}"),
        Sets.newHashSet("CSS_C1", "CSS_E"),
        "CSS_");
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(1);

    CssRulesetNode rule = rules.getRulesetNodes().iterator().next();
    checkRuleset("[[.CSS_C2-CSS_E]{[margin:[[3px]];]}]", rule);
  }

  @Test
  public void testMarkRemovableRulesetNodeShorthand() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE {",
              "  border-color: red;",
              "}",
              ".CSS_RULE {",
              "  border: 3px;",
              "}"), null, null);
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(1);

    CssRulesetNode rule = rules.getRulesetNodes().iterator().next();
    checkRuleset("[[.CSS_RULE]{[border-color:[[red]];]}]", rule);
  }

  @Test
  public void testMarkRemovableRulesetNodeShorthandWithImportant() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE {",
              "  border-color: red !important;",
              "}",
              ".CSS_RULE {",
              "  border: 3px;",
              "}"), null, null);
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).isEmpty();
  }

  @Test
  public void testMarkRemovableRulesetNodeShorthandWithAllImportant() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE {",
              "  border-color: red !important;",
              "}",
              ".CSS_RULE {",
              "  border: 3px !important;",
              "}"), null, null);
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(1);

    CssRulesetNode rule = rules.getRulesetNodes().iterator().next();
    checkRuleset("[[.CSS_RULE]{[border-color:[[red][!important]];]}]", rule);
  }

  @Test
  public void testMarkRemovableRulesetNodeDontBarfOnNonRuleset() {
    collectRemovableRulesetNodes(
        linesToString(
              "@media print {}",
              ".CSS_RULE {",
              "  border: 2px;",
              "}",
              ".CSS_RULE {",
              "  border: 3px;",
              "}"), null, null);
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(1);

    CssRulesetNode rule = rules.getRulesetNodes().iterator().next();
    checkRuleset("[[.CSS_RULE]{[border:[[2px]];]}]", rule);
  }

  @Test
  public void testMarkRemovableRulesetNodeAlternate1() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE {",
              "  border: 2px;",
              "}",
              ".CSS_RULE {",
              "  /* @alternate */ border: 3px;",
              "}"),
        Sets.<String>newHashSet(),
        null);
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();
    assertThat(rules.getRulesetNodes()).isEmpty();
  }

  @Test
  public void testMarkRemovableRulesetNodeAlternate2() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE {",
              "  border: 0;",
              "}",
              ".CSS_RULE {",
              "  /* @alternate */ border: 1px;",
              "}",
              // The previous two are superseded by the next non-alternate.
              ".CSS_RULE {",
              "  border: 2px;",
              "}"),
        Sets.<String>newHashSet(),
        null);
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(2);

    Iterator<CssRulesetNode> iterator = rules.getRulesetNodes().iterator();
    CssRulesetNode alternateRule = iterator.next();
    assertThat(alternateRule.getDeclarations().numChildren()).isEqualTo(1);
    CssDeclarationNode alternateDeclaration =
        (CssDeclarationNode) alternateRule.getDeclarations().getChildAt(0);
    checkRuleset("[[.CSS_RULE]{[[/* @alternate */]border:[[1px]];]}]",
        alternateRule);
    assertThat(alternateDeclaration.hasComment("/* @alternate */")).isTrue();
    checkRuleset("[[.CSS_RULE]{[border:[[0]];]}]", iterator.next());
  }

  @Test
  public void testMarkRemovableRulesetNodeAlternate3() {
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE {",
              "  border: 1px;",
              "}",
              // The rule before this is superseded.
              ".CSS_RULE {",
              "  border: 2px;",
              "}",
              ".CSS_RULE {",
              "  /* @alternate */ border: 3px;",
              "}"),
        Sets.<String>newHashSet(),
        null);
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(1);
    checkRuleset("[[.CSS_RULE]{[border:[[1px]];]}]",
        rules.getRulesetNodes().iterator().next());
  }

  @Test
  public void testMarkRemovableRulesetNodeDeadCode() {
    // The "display" property is typically guarded by
    // SkippingTreeProperty.canModify because it causes layout in IE. For
    // the purposes of dead code removal this is irrelevant so make sure that
    // we can remove it anyway.
    collectRemovableRulesetNodes(
        linesToString(
              ".CSS_RULE {",
              "  display: 1px;",
              "}"),
        Sets.<String>newHashSet(".some_other_thing"),
        "");
    final RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();

    assertThat(rules.getRulesetNodes()).hasSize(1);
    checkRuleset("[[.CSS_RULE]{[display:[[1px]];]}]",
        rules.getRulesetNodes().iterator().next());
  }

  private void collectRemovableRulesetNodes(String source,
      Set<String> referredRules, String prefix) {
    parseAndBuildTree(source);
    MarkRemovableRulesetNodes pass = new MarkRemovableRulesetNodes(tree, true);
    pass.setReferencedRules(referredRules, prefix);
    pass.runPass();
  }
}
