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

import com.google.common.css.compiler.passes.testing.PassesTestBase;
import org.junit.jupiter.api.Test;

/**
 * Functional tests for {@link ReplaceConstantReferences}.
 *
 * @author oana@google.com (Oana Florescu)
 */
class ReplaceConstantReferencesFunctionalTest extends PassesTestBase {

    @Test
    void testReplaceConstants1() {
        testTreeConstruction(linesToString(
                "@def A red;",
                "@def B 3px;",
                "@def C 1;",
                "@def CORNER_BG gssFunction(A, B, C);",
                ".CSS_RULE {",
                "  background: CORNER_BG;",
                "}"),
                "[[.CSS_RULE]{[background:[gssFunction(red,3px,1)];]}]");
    }

    @Test
    void testReplaceConstants2() {
        testTreeConstruction(linesToString(
                "@def A red;",
                "@def B A;",
                "@def C green;",
                "@def D blue;",
                "@def COLOR B C D;",
                ".CSS_RULE {",
                "  border-color: COLOR;",
                "}"),
                "[[.CSS_RULE]{[border-color:[[red][green][blue]];]}]");
    }

    @Test
    void testReplaceConstants3() {
        testTreeConstruction(linesToString(
                "@def COLOR #ccc;",
                "@def BG_COLOR #fff;",
                "@def CONTAINER_COLOR BG_COLOR;",
                "@def BORDER_TOP_COLOR blendColors(COLOR, #000);",
                "@def INPUT_BG_COLOR  CONTAINER_COLOR;",
                "@def BORDER_COLOR BORDER_TOP_COLOR COLOR COLOR COLOR;",
                ".CSS_RULE {",
                "  border-color: BORDER_COLOR;",
                "}"),
                "[[.CSS_RULE]{[border-color:"
                        + "[blendColors(#ccc,#000) [#ccc][#ccc][#ccc]];]}]");
    }

    @Test
    void testReplaceConstants4() {
        testTreeConstruction(linesToString(
                "@def IE6 0;",
                "@def HEIGHT 1px;",
                "@def TOP_RIGHT tr;",
                "@def EXT_COLOR red;",
                "@def COLOR blendColors(EXT_COLOR, 0, -8, -8);",
                "@def BORDER_TOP_COLOR function(COLOR, HEIGHT, TOP_RIGHT, IE6);",
                "@def BORDER_COLOR BORDER_TOP_COLOR;",
                ".CSS_RULE {",
                "  border-color: BORDER_COLOR;",
                "}"),
                "[[.CSS_RULE]{[border-color:"
                        + "[function(blendColors(red,0,-8,-8),1px,tr,0)];]}]");
    }

    @Test
    void testReplaceConstants5() {
        testTreeConstruction(linesToString(
                "@def EXT_COLOR red;",
                "@def COLOR blendColors(EXT_COLOR);",
                "@def FUN_COLOR function(COLOR);",
                "@def BORDER_COLOR FUN_COLOR;",
                ".CSS_RULE {",
                "  border-color: BORDER_COLOR;",
                "}"),
                "[[.CSS_RULE]{[border-color:"
                        + "[function(blendColors(red))];]}]");
    }

    @Test
    void testWebkitGradient() {
        testTreeConstruction(linesToString(
                "@def A #fff;",
                "@def B #ddd;",
                "@def C 100%;",
                "@def D -webkit-gradient(linear, 0 0, 0 C, from(A), to(B));",
                ".CSS_RULE {",
                "  background: D;",
                "}"),
                "[[.CSS_RULE]{[background:"
                        + "[-webkit-gradient(linear,0 0,0 100%,from(#fff),to(#ddd))];]}]");
    }

    @Test
    void testCompositeValueNodeReplacement() {
        testTreeConstruction(linesToString(
                "@def DARK_DIVIDER_LEFT -1px 0 1px rgba(5,4,4,.3);",
                "@def DARK_DIVIDER_RIGHT 1px 0 1px rgba(73,71,71,.3);",
                ".A {",
                "  box-shadow: inset DARK_DIVIDER_RIGHT, inset DARK_DIVIDER_LEFT;",
                "}"),
                "[[.A]{[box-shadow:"
                        + "[[inset][[[1px] [0] [1px] rgba(73,71,71,.3)],"
                        + "[inset]][-1px][0][1px]rgba(5,4,4,.3)"
                        + "];]}]");
    }

    @Test
    void testVariableInFunctionInComposite() {
        testTreeConstruction(linesToString(
                "@def BG_COLOR beige;",
                "",
                "a {",
                "  background:-webkit-linear-gradient(top, BG_COLOR 30%,"
                        + " rgba(255,255,255,0)),",
                "    -webkit-linear-gradient(top, rgba(255,255,255,0), BG_COLOR 70%);",
                "}"),

                "[[a]{[background:[[-webkit-linear-gradient(top,beige 30%,"
                        + "rgba(255,255,255,0)),-webkit-linear-gradient(top,"
                        + "rgba(255,255,255,0),beige 70%)]];]}]");
    }

    @Test
    void testCompositeValueNodeWithFunctions() {
        testTreeConstruction(linesToString(
                "@def DEF_A top, red 30%, rgba(0, 0, 0, 0);",
                "@def DEF_B top, rgba(0, 0, 0, 0), red 30%;",
                ".A {",
                "  background: linear-gradient(DEF_A), linear-gradient(DEF_B);",
                "}"),
                "[[.A]{[background:[["
                        + "linear-gradient([[top],[red]] [[30%],rgba(0,0,0,0)]),"
                        + "linear-gradient([[top],rgba(0,0,0,0),[red]] 30%)]];]}]");
    }

    @Test
    void testFontReplacement() {
        testTreeConstruction(linesToString(
                "@def BASE_TINY_FONT_FACE verdana, arial, \"Courrier New\", sans-serif;",
                "@def BASE_TINY_FONT_SIZE 19px;",
                "@def BASE_TINY_FONT      BASE_TINY_FONT_SIZE BASE_TINY_FONT_FACE;",
                ".A {",
                "  font: BASE_TINY_FONT;",
                "}"),
                "[[.A]{[font:[[19px][[verdana],[arial],[\"Courrier New\"],[sans-serif]]];]}]");
    }

    @Test
    void testMediaQueryReplacement1() {
        testTreeConstruction(linesToString(
                "@def QUERY screen and (min-resolution:96dpi);",
                "@media QUERY {",
                "  .A {",
                "    color: red;",
                "  }",
                "}"),
                "[@media [screen] [and] [(min-resolution:96dpi)]{[.A]{[color:[[red]];]}}]");
    }

    @Test
    void testMediaQueryReplacement2() {
        testTreeConstruction(linesToString(
                "@def QUERY (min-resolution:96dpi);",
                "@media screen and QUERY {",
                "  .A {",
                "    color: red;",
                "  }",
                "}"),
                "[@media [screen] [and] [(min-resolution:96dpi)]{[.A]{[color:[[red]];]}}]");
    }

    @Test
    void testCalcReplacement() {
        testTreeConstruction(
                "@def A 5px; .elem { width: calc(A * 2) }", "[[.elem]{[width:[calc([[[5px]]*[2]])];]}]");
    }

    @Test
    void testCalcReplacement_Complex1() {
        testTreeConstruction(
                "@def A 5px; .elem { width: calc((A * (A + 5) - A) / 2) }",
                "[[.elem]{[width:[calc([[([([[5px]]*[([[5px]] + [5])])] - [[5px]])]/[2]])];]}]");
    }

    @Test
    void testCalcReplacement_Complex2() {
        testTreeConstruction(
                "@def A 5px;"
                        + "@def C calc(2 + A + 10px);"
                        + ".foo {"
                        + "width: C;"
                        + "}",
                "[[.foo]{[width:[calc([[2] + [[[5px]] + [10px]]])];]}]");
    }

    @Override
    protected void runPass() {
        new CreateDefinitionNodes(tree.getMutatingVisitController(), errorManager).runPass();
        new CreateStandardAtRuleNodes(tree.getMutatingVisitController(), errorManager).runPass();
        new CreateConstantReferences(tree.getMutatingVisitController()).runPass();
        new CreateConditionalNodes(tree.getMutatingVisitController(), errorManager).runPass();
        CollectConstantDefinitions defPass = new CollectConstantDefinitions(tree);
        defPass.runPass();
        new ReplaceConstantReferences(tree, defPass.getConstantDefinitions(),
                true /* removeDefs */, errorManager,
                true /* allowUndefinedConstants */).runPass();
    }
}
