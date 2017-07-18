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

package com.google.common.css.compiler.ast;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.css.SourceCode;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link CssSelectorNode}.
 *
 * @author dgajda@google.com (Damian Gajda)
 */
@RunWith(JUnit4.class)
public class CssSelectorNodeTest extends NewFunctionalTestBase {

  @Test
  public void testDeepCopy() {
    SourceCode sourceCode = new SourceCode("foo", null);
    SourceCodeLocation location =
        new SourceCodeLocation(sourceCode, 1, 1, 1, 2, 1, 1);
    CssSelectorNode node = new CssSelectorNode("a", location);
    node.setChunk("baz");
    CssClassSelectorNode refiner = new CssClassSelectorNode("c", location);
    node.getRefiners().addChildToBack(refiner);
    refiner.appendComment(new CssCommentNode("/* @noflip */", null));

    CssSelectorNode copy = node.deepCopy();
    assertThat(copy.getChunk()).isEqualTo(node.getChunk());
    assertThat(copy.getSelectorName()).isEqualTo(node.getSelectorName());
    assertThat(copy.getSourceCodeLocation()).isEqualTo(node.getSourceCodeLocation());
    assertThat(copy.getRefiners().getChildAt(0).getComments())
        .isEqualTo(node.getRefiners().getChildAt(0).getComments());
  }

  // Examples from http://www.w3.org/TR/CSS2/cascade.html#specificity

  @Test
  public void testSpecificity1() {
    testSpecificity("*", "0,0,0,0");
  }

  @Test
  public void testSpecificity2() {
    testSpecificity("li", "0,0,0,1");
  }

  @Test
  public void testSpecificity3() {
    testSpecificity("li:first-line", "0,0,0,2");
  }

  @Test
  public void testSpecificity4() {
    testSpecificity("ul li", "0,0,0,2");
  }

  @Test
  public void testSpecificity5() {
    testSpecificity("ul ol+li", "0,0,0,3");
  }

  @Test
  public void testSpecificity6() {
    testSpecificity("h1 + *[rel=up]", "0,0,1,1");
  }

  @Test
  public void testSpecificity7() {
    testSpecificity("ul ol li.red", "0,0,1,3");
  }

  @Test
  public void testSpecificity8() {
    testSpecificity("li.red.level", "0,0,2,1");
  }

  @Test
  public void testSpecificity9() {
    testSpecificity("#x34y", "0,1,0,0");
  }

  @Test
  public void testSpecificity10() {
    testSpecificity("#s12:not(FOO)", "0,1,0,1");
  }

  @Test
  public void testSpecificity11() {
    testSpecificity("*:not(li.red.level)", "0,0,2,1");
  }

  @Test
  public void testSpecificity12() {
    testSpecificity("#s12:not(#s45)", "0,2,0,0");
  }

  @Test
  public void testSpecificity13() {
    testSpecificity("#s12:not(#s45)", "0,2,0,0");
  }

  @Test
  public void testSpecificity14() {
    testSpecificity("#s12::after", "0,1,0,1");
  }

  private void testSpecificity(String selector, String expected) {
    try {
      parseAndRun(selector + " {}");
    } catch (GssParserException e) {
      Assert.fail(e.getMessage());
    }
    CssNode node = tree.getRoot().getBody().getChildAt(0);
    assertThat(node).isInstanceOf(CssRulesetNode.class);
    CssRulesetNode rulesetNode = (CssRulesetNode) node;
    CssSelectorNode selectorNode = rulesetNode.getSelectors().getChildAt(0);
    assertThat(selectorNode.getSpecificity().toString()).isEqualTo(expected);
  }
}
