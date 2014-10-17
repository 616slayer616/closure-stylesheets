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

import com.google.common.css.SourceCode;
import com.google.testing.util.MoreAsserts;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for error handling of {@link GssParser}.
 *
 */

public class GssParserErrorTest extends TestCase {

  private CssTree parse(String gss) throws GssParserException {
    GssParser parser = new GssParser(new SourceCode("test", gss));
    return parser.parse();
  }

  private void testError(String gss, int lineNumber, int indexInLine,
                         String line, String caret) {
    try {
      parse(gss);
      fail();
    } catch (GssParserException e) {
      assertEquals(
           "Parse error in test at line " + lineNumber +
           " column " + indexInLine + ":\n" +
           line + "\n" + caret + "\n",
           e.getMessage());
    }
  }

  public void test1() {
    testError("a { exu7y&&rgx: url('http://test.com') }", 1, 10,
              "a { exu7y&&rgx: url('http://test.com') }",
              "         ^");
  }

  public void test2() {
    testError(
        "a {\n" +
        "    exu7y&&rgx: url('http://test.com')\n" +
        "  }", 2, 10,
        "    exu7y&&rgx: url('http://test.com')",
        "         ^");
  }

  public void test3() {
    testError("a { b: c,,}", 1, 10,
              "a { b: c,,}",
              "         ^");
  }

  public void test4() {
    testError("a", 1, 1,
              "a",
              "^");
  }

  public void test5() {
    testError("a { b: c;", 1, 9,
              "a { b: c;",
              "        ^");
  }

  public void test6() {
    testError("{}", 1, 1,
              "{}",
              "^");
  }

  public void test7() {
    testError("\na { b: c,,}", 2, 10,
              "a { b: c,,}",
              "         ^");
  }

  public void testBadToken1() {
    // Should be > not <.
    testError(".foo .bar<td {}", 1, 10,
              ".foo .bar<td {}",
              "         ^");
  }

  public void testBadToken2() {
    testError("\n<td {}", 2, 1,
              "<td {}",
              "^");
  }

  public void testBadToken3() {
    testError("<td {}", 1, 1,
              "<td {}",
              "^");
  }

  public void testBadWebkitKeyframes1() {
    testError("@-webkit-keyframes bounce {\n" +
        "  0 {\n" +
        "    left: 0px;\n" +
        "  }\n" +
        "  100% {\n" +
        "    left: 200px;\n" +
        "  }\n" +
        "}\n", 2, 4,
        "  0 {",
        "   ^");
  }

  public void testBadWebkitKeyframes2() {
    testError("@-webkit-keyframes bounce {\n" +
        "  2.2 {\n" +
        "    left: 0px;\n" +
        "  }\n" +
        "  100% {\n" +
        "    left: 200px;\n" +
        "  }\n" +
        "}\n", 2, 6,
        "  2.2 {",
        "     ^");
  }

  public void testBadWebkitKeyframes3() {
    testError("@-webkit-keyframes foo;", 1, 23,
        "@-webkit-keyframes foo;",
        "                      ^");
  }

  public void testBadPseudoNth1() {
    testError("div :nth-child(#id) { }", 1, 16,
        "div :nth-child(#id) { }",
        "               ^");
  }

  public void testBadPseudoNth2() {
    testError("div :nth-child(.class) { }", 1, 16,
        "div :nth-child(.class) { }",
        "               ^");
  }

  public void testBadPseudoNot1() {
    testError("div :not() { }", 1, 10,
        "div :not() { }",
        "         ^");
  }

  public void testBadPseudoNot2() {
    // :not can only take a simple selector as an argument.
    testError("div :not(div p) { }", 1, 14,
        "div :not(div p) { }",
        "             ^");
  }

  public void testBadMixinDefinition() {
    testError("@defmixin name($var) {}", 1, 16,
        "@defmixin name($var) {}",
        "               ^");
  }

  public void testBadGradient() {
    testError("div {"
        + "d:-invalid-gradient(bottom left, red 20px, yellow, green,"
        + "blue 90%);"
        + "}",
        1, 72,
        "div {d:-invalid-gradient(bottom left, red 20px, yellow, green,"
        + "blue 90%);}",
        "                                                                "
        + "       ^");
  }

  /**
   * Tests for error handling below
   */

  private void testErrorHandling(String input, String expected, String... errors)
      throws GssParserException {
    GssParser parser = new GssParser(new SourceCode("test", input));
    CssTree tree = parser.parse(true);
    assertNotNull(tree);
    CssRootNode root = tree.getRoot();
    assertNotNull(root);
    assertEquals(expected, root.toString());
    List<String> handledErrors = new ArrayList<>(parser.getHandledErrors().size());
    for (GssParserException e : parser.getHandledErrors()) {
      handledErrors.add(e.getMessage());
    }
    MoreAsserts.assertContentsInOrder(handledErrors, (Object[]) errors);
  }

  public void testDeclarationErrorHandling() throws GssParserException {
    testErrorHandling("a { b: c,,; d: e }", "[[a]{[d:[e]]}]",
        "Parse error in test at line 1 column 10:\n"
        + "a { b: c,,; d: e }\n"
        + "         ^\n");
    testErrorHandling("a { b: c: d; e: f }", "[[a]{[e:[f]]}]",
        "Parse error in test at line 1 column 10:\n"
        + "a { b: c: d; e: f }\n"
        + "         ^\n");
    testErrorHandling("a { b: c; @at d: e; f: g }", "[[a]{[b:[c], f:[g]]}]",
        "Parse error in test at line 1 column 17:\n"
        + "a { b: c; @at d: e; f: g }\n"
        + "                ^\n");
  }

  public void testSelectorErrorHandling() throws GssParserException {
    testErrorHandling("a>>b { b: c } d { e: f }", "[[d]{[e:[f]]}]",
        "Parse error in test at line 1 column 2:\n"
        + "a>>b { b: c } d { e: f }\n"
        + " ^\n");
    testErrorHandling("a @ b { c: d } e {}", "[[e]{[]}]",
        "Parse error in test at line 1 column 3:\n"
        + "a @ b { c: d } e {}\n"
        + "  ^\n");
    // No error; braces within quoted string are correctly parsed
    testErrorHandling("a{b:\"{,}\"}", "[[a]{[b:[\"{,}\"]]}]");
  }

  public void testAtRuleErrorHandling() throws GssParserException {
    testErrorHandling("@a b (,,); c { d: e }", "[[c]{[d:[e]]}]",
        "Parse error in test at line 1 column 7:\n"
        + "@a b (,,); c { d: e }\n"
        + "      ^\n");
    testErrorHandling("@a { b,,{} c { d:: e; f: g } } h { i: j }",
        "[@a[]{[[c]{[f:[g]]}]}, [h]{[i:[j]]}]",
        "Parse error in test at line 1 column 8:\n"
        + "@a { b,,{} c { d:: e; f: g } } h { i: j }\n"
        + "       ^\n",
        "Parse error in test at line 1 column 18:\n"
        + "@a { b,,{} c { d:: e; f: g } } h { i: j }\n"
        + "                 ^\n");
  }

  public void testMatcingBraces() throws GssParserException {
    // Inner closed block ignored
    testErrorHandling("a{ b{} } c{}", "[[a]{[]}, [c]{[]}]",
        "Parse error in test at line 1 column 5:\n"
        + "a{ b{} } c{}\n"
        + "    ^\n");
    // Inner nested blocks ignored as well
    testErrorHandling("a{([b])} c{}", "[[c]{[]}]",
        "Parse error in test at line 1 column 3:\n"
        + "a{([b])} c{}\n"
        + "  ^\n");
    // Unmatched left brace consume until EOF
    testErrorHandling("a{([b)]} c{}", "[]",
        "Parse error in test at line 1 column 3:\n"
        + "a{([b)]} c{}\n"
        + "  ^\n");
    // Unmatched right brace ignored
    testErrorHandling("a{ (}) } b{}", "[[b]{[]}]",
        "Parse error in test at line 1 column 4:\n"
        + "a{ (}) } b{}\n"
        + "   ^\n");
  }
}
