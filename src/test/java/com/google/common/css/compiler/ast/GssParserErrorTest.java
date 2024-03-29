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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for error handling of {@link GssParser}.
 *
 * @author fbenz@google.com (Florian Benz)
 */


class GssParserErrorTest {

    private void testError(String gss, int lineNumber, int indexInLine,
                           String line, String caret) {
        try {
            parse(gss);
            Assertions.fail();
        } catch (GssParserException e) {
            assertThat(e)
                    .hasMessage(
                            "Parse error in test at line "
                                    + lineNumber
                                    + " column "
                                    + indexInLine
                                    + ":\n"
                                    + line
                                    + "\n"
                                    + caret
                                    + "\n");
        }
    }

    @Test
    void test1() {
        testError("a { exu7y&&rgx: url('https://test.com') }", 1, 10,
                "a { exu7y&&rgx: url('https://test.com') }",
                "         ^");
    }

    @Test
    void test2() {
        testError(
                "a {\n" +
                        "    exu7y&&rgx: url('https://test.com')\n" +
                        "  }", 2, 10,
                "    exu7y&&rgx: url('https://test.com')",
                "         ^");
    }

    @Test
    void test3() {
        testError("a { b: c,,}", 1, 10,
                "a { b: c,,}",
                "         ^");
    }

    @Test
    void test4() {
        testError("a", 1, 1,
                "a",
                "^");
    }

    @Test
    void test5() {
        testError("a { b: c;", 1, 9,
                "a { b: c;",
                "        ^");
    }

    @Test
    void test6() {
        testError("{}", 1, 1,
                "{}",
                "^");
    }

    @Test
    void test7() {
        testError("\na { b: c,,}", 2, 10,
                "a { b: c,,}",
                "         ^");
    }

    @Test
    void testBadToken1() {
        // Should be > not <.
        testError(".foo .bar<td {}", 1, 10,
                ".foo .bar<td {}",
                "         ^");
    }

    @Test
    void testBadToken2() {
        testError("\n<td {}", 2, 1,
                "<td {}",
                "^");
    }

    @Test
    void testBadToken3() {
        testError("<td {}", 1, 1,
                "<td {}",
                "^");
    }

    @Test
    void testBadWebkitKeyframes1() {
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

    @Test
    void testBadWebkitKeyframes2() {
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

    @Test
    void testBadWebkitKeyframes3() {
        testError("@-webkit-keyframes foo;", 1, 23,
                "@-webkit-keyframes foo;",
                "                      ^");
    }

    @Test
    void testBadPseudoNth1() {
        testError("div :nth-child(#id) { }", 1, 16,
                "div :nth-child(#id) { }",
                "               ^");
    }

    @Test
    void testBadPseudoNth2() {
        testError("div :nth-child(.class) { }", 1, 16,
                "div :nth-child(.class) { }",
                "               ^");
    }

    @Test
    void testBadPseudoNot1() {
        testError("div :not() { }", 1, 10,
                "div :not() { }",
                "         ^");
    }

    @Test
    void testBadPseudoNot2() {
        // :not can only take a simple selector as an argument.
        testError("div :not(div p) { }", 1, 14,
                "div :not(div p) { }",
                "             ^");
    }

    @Test
    void testBadMixinDefinition() {
        testError("@defmixin name($#%$var) {}", 1, 16,
                "@defmixin name($#%$var) {}",
                "               ^");
    }

    @Test
    void testBadGradient() {
        testError("div {"
                        + "d:-invalid-gradient(bottom left, red 20px, yellow, green,"
                        + "blue 90%);"
                        + "}",
                1, 72,
                "div {d:-invalid-gradient(bottom left, red 20px, yellow, green,blue 90%);}",
                "                                                                       ^");
    }

    @Test
    void testInvalidSpaceInArgumentList() {
        // The parser marks the error at the semicolon because this is the token immediately following
        // the last successfully-consumed production. This is not ideal because the error occurs within
        // the argument list, but we validate the argument list after it is successfully parsed by the
        // grammar.
        testError("div { transform:rotate(180 deg); }",
                1, 32,
                "div { transform:rotate(180 deg); }",
                "                               ^");
    }

    /**
     * Tests for error handling below
     */

    private void testErrorHandling(String input, String expected, String... errors)
            throws GssParserException {
        List<GssParserException> handledErrors = new ArrayList<>();
        CssTree tree = parse(input, true, handledErrors);
        List<String> errorMessages = new ArrayList<>();
        for (GssParserException e : handledErrors) {
            errorMessages.add(e.getMessage());
        }
        assertThat(tree).isNotNull();
        CssRootNode root = tree.getRoot();
        assertThat(root).isNotNull();
        assertThat(errorMessages).containsExactly(errors);
        assertThat(root).hasToString(expected);
    }

    @Test
    void testDeclarationErrorHandling() throws GssParserException {
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

    @Test
    void testSelectorErrorHandling() throws GssParserException {
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

    @Test
    void testAtRuleErrorHandling() throws GssParserException {
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
        testErrorHandling("@a (b;) { c {} } d {}", "[[d]{[]}]",
                "Parse error in test at line 1 column 6:\n"
                        + "@a (b;) { c {} } d {}\n"
                        + "     ^\n");
        testErrorHandling("@a (b:c[]) { d[;}] {} e {} } f {}", "[[f]{[]}]",
                "Parse error in test at line 1 column 8:\n"
                        + "@a (b:c[]) { d[;}] {} e {} } f {}\n"
                        + "       ^\n");
        testErrorHandling("a { @b { c, {} d {} } e: f }", "[[a]{[@b[]{[]}, e:[f]]}]",
                "Parse error in test at line 1 column 11:\n"
                        + "a { @b { c, {} d {} } e: f }\n"
                        + "          ^\n");
        testErrorHandling("a[b=] { c {} } d {}", "[[d]{[]}]",
                "Parse error in test at line 1 column 5:\n"
                        + "a[b=] { c {} } d {}\n"
                        + "    ^\n");
    }

    @Test
    void testMatchingBraces() throws GssParserException {
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

    @Test
    void testErrorRecoveryWithInvalidArgumentList() throws GssParserException {
        testErrorHandling("div { transform:rotate(180 deg); }", "[[div]{[]}]",
                "Parse error in test at line 1 column 32:\n"
                        + "div { transform:rotate(180 deg); }\n"
                        + "                               ^\n");
    }

    @Test
    void testUnterminatedBlockCommentsWithoutErrorRecovery() {
        testError("div {}/*comment**p {}", 1, 7,
                "div {}/*comment**p {}",
                "      ^");
        testError("div {}/*/p {}", 1, 7,
                "div {}/*/p {}",
                "      ^");
        testError("div {}/*", 1, 7,
                "div {}/*",
                "      ^");
        testError("div {}/* *\ndiv { color: red; }", 1, 7,
                "div {}/* *",
                "      ^");
        testError("div {} /* comment */ div {} /* unterminated comment", 1, 29,
                "div {} /* comment */ div {} /* unterminated comment",
                "                            ^");
        testError("div {} /* comment */ /* unterminated comment", 1, 22,
                "div {} /* comment */ /* unterminated comment",
                "                     ^");
    }

    @Test
    void testUnterminatedBlockCommentsWithErrorRecovery() throws GssParserException {
        testErrorHandling("div {}/*comment**p {}", "[[div]{[]}]",
                "Parse error in test at line 1 column 7:\n"
                        + "div {}/*comment**p {}\n"
                        + "      ^\n");
        testErrorHandling("div {}/*/p {}", "[[div]{[]}]",
                "Parse error in test at line 1 column 7:\n"
                        + "div {}/*/p {}\n"
                        + "      ^\n");
        testErrorHandling("div {}/*", "[[div]{[]}]",
                "Parse error in test at line 1 column 7:\n"
                        + "div {}/*\n"
                        + "      ^\n");
        testErrorHandling("div {}/* *\ndiv { color: red; }", "[[div]{[]}]",
                "Parse error in test at line 1 column 7:\n"
                        + "div {}/* *\n"
                        + "      ^\n");
        testErrorHandling(
                "div {} /* comment */ div {} /* unterminated comment", "[[div]{[]}, [div]{[]}]",
                "Parse error in test at line 1 column 29:\n"
                        + "div {} /* comment */ div {} /* unterminated comment\n"
                        + "                            ^\n");
        testErrorHandling("div {} /* comment */ /* unterminated comment", "[[div]{[]}]",
                "Parse error in test at line 1 column 22:\n"
                        + "div {} /* comment */ /* unterminated comment\n"
                        + "                     ^\n");
    }

    private CssTree parse(String gss, boolean shouldHandleError,
                          List<GssParserException> handledErrors)
            throws GssParserException {
        GssParser parser = new GssParser(new SourceCode("test", gss));
        CssTree tree = parser.parse(shouldHandleError);
        handledErrors.addAll(parser.getHandledErrors());
        return tree;
    }

    private CssTree parse(String gss) throws GssParserException {
        return parse(gss, false, new ArrayList<>());
    }
}
