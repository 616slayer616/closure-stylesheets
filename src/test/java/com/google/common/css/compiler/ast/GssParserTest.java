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

import com.google.common.collect.ImmutableList;
import com.google.common.css.SourceCode;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.passes.CompactPrinter;
import com.google.common.css.compiler.passes.testing.AstPrinter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the {@link GssParser}.
 *
 * @author fbenz@google.com (Florian Benz)
 */

class GssParserTest {

    private CssTree testValid(String gss) throws GssParserException {
        CssTree tree = parse(gss);
        assertThat(tree).isNotNull();
        return tree;
    }

    private void testTree(String gss, String output) throws GssParserException {
        CssTree tree = parse(gss);
        assertThat(tree).isNotNull();
        CssRootNode root = tree.getRoot();
        assertThat(root).isNotNull();
        assertThat(AstPrinter.print(tree)).isEqualTo(output);
    }

    @Test
    void testManySources() throws Exception {
        CssTree tree = parse(ImmutableList.of(
                new SourceCode("test1", "a {}"),
                new SourceCode("test2", "@component c { x {y: z} }"),
                new SourceCode("test3", "b {}")));
        CssRootNode root = tree.getRoot();
        assertThat(root).isNotNull();
        assertThat(AstPrinter.print(tree)).isEqualTo("[[a]{[]}@component [c]{[x]{[y:[[z]];]}}[b]{[]}]");
    }

    @Test
    void testAst1() throws Exception {
        testTree("a {}", "[[a]{[]}]");
    }

    @Test
    void testAst2() throws Exception {
        testTree("a.b c#d > e.f + g {}", "[[a.b c#d>e.f+g]{[]}]");
    }

    @Test
    void testAst3() throws Exception {
        testTree("a {x: y}", "[[a]{[x:[[y]];]}]");
    }

    @Test
    void testAst4() throws Exception {
        testTree("a {w: x; y: z}", "[[a]{[w:[[x]];y:[[z]];]}]");
    }

    @Test
    void testAst5() throws Exception {
        testTree("a {b: 1em}", "[[a]{[b:[[1em]];]}]");
    }

    @Test
    void testAst6() throws Exception {
        testTree("a {b: 1.5em}", "[[a]{[b:[[1.5em]];]}]");
    }

    @Test
    void testAst7() throws Exception {
        testTree("a {b: 'x'}", "[[a]{[b:[['x']];]}]");
    }

    @Test
    void testAst8() throws Exception {
        testTree("a {b: url(#x)}", "[[a]{[b:[url(#x)];]}]");
    }

    @Test
    void testAst9() throws Exception {
        testTree("a {b: url('#x')}", "[[a]{[b:[url('#x')];]}]");
    }

    @Test
    void testAst10() throws Exception {
        testTree("a {b: x y z}", "[[a]{[b:[[x][y][z]];]}]");
    }

    @Test
    void testAst11() throws Exception {
        testTree("a {b: c,d,e/f g,h i j,k}",
                "[[a]{[b:[[[c],[d],[[e]/[f]]][[g],[h]][i][[j],[k]]];]}]");
    }

    @Test
    void testAst12() throws Exception {
        testTree("a {b: rgb(0,0,0)}", "[[a]{[b:[rgb(0,0,0)];]}]");
    }

    @Test
    void testAst13() throws Exception {
        testTree("a {b: custom(0,0)}", "[[a]{[b:[custom(0,0)];]}]");
    }

    @Test
    void testAst14() throws Exception {
        testTree("@def a b;", "[@def [a] [b];]");
    }

    @Test
    void testAst15() throws Exception {
        testTree("@component a { x {y: z} }",
                "[@component [a]{[x]{[y:[[z]];]}}]");
    }

    @Test
    void testAst16() throws Exception {
        testTree("a:foo {\n bla : d ; }",
                "[[a:foo]{[bla:[[d]];]}]");
    }

    @Test
    void testAst17() throws Exception {
        testTree("foo {f: rgb(o=0);}",
                "[[foo]{[f:[rgb([[o]=[0]])];]}]");
    }

    @Test
    void testAst18() throws Exception {
        testTree("a:lang(c) { d: e }",
                "[[a:lang(c)]{[d:[[e]];]}]");
    }

    @Test
    void testAst19() throws Exception {
        testTree("a~b { d: e }",
                "[[a~b]{[d:[[e]];]}]");
    }

    @Test
    void testAst20() throws Exception {
        testTree("a:b(-2n+3) { d: e }",
                "[[a:b(-2n+3)]{[d:[[e]];]}]");
    }

    @Test
    void testAst21() throws Exception {
        testTree("a:not(#id) { d: e }",
                "[[a:not(#id)]{[d:[[e]];]}]");
    }

    @Test
    void testAst22() throws Exception {
        testTree(".a { d:e,f }",
                "[[.a]{[d:[[[e],[f]]];]}]");
    }

    @Test
    void testAst23() throws Exception {
        testTree(".a { d:e f,g h }",
                "[[.a]{[d:[[e][[f],[g]][h]];]}]");
    }

    @Test
    void testAst24() throws Exception {
        testTree("a~b/deep/c { d: e }",
                "[[a~b/deep/c]{[d:[[e]];]}]");
    }

    @Test
    void testParsingRules1() throws Exception {
        testValid("css_rule33 {\n" +
                "border: black ; /* comment */\n" +
                "height : 1em\n" +
                " }"
        );
    }

    // We don't test for comments between '!' and 'important'. See the comment on
    // the IMPORTANT_SYM in the grammar for the reason.
    @Test
    void testParsingRules2() throws Exception {
        testValid("ul.navbar {\n" +
                "    position: absolute;\n" +
                "    top: top;\n" +
                "    left: down;\n" +
                "    width: nice  }\n" +
                "\n" +
                ".foo {\n" +
                "   position: absolute ! important ;\n" +
                "}\n" +
                ".bar {\n" +
                "   position: absolute !  important;\n\n\n" +
                "}"
        );
    }

    @Test
    void testParsingRules3() throws Exception {
        testValid("css_rule33 test2 {\n" +
                "border: black ; /* comment */\n" +
                "height : 1em\n" +
                " }"
        );
    }

    @Test
    void testParsingRules4() throws Exception {
        testValid("p:before {content: counter(par-num, upper-roman) \". \"}");
    }

    @Test
    void testParsingSelector1() throws Exception {
        testValid("a b { x: y}");
    }

    @Test
    void testParsingSelector2() throws Exception {
        testValid("a > b { x: y}");
    }

    @Test
    void testParsingSelector3() throws Exception {
        testValid("a + b { x: y}");
    }

    @Test
    void testParsingSelector4() throws Exception {
        testValid("a + b > c d e.f + g { x: y}");
    }

    @Test
    void testParsingSelector5() throws Exception {
        testValid("a + b > c d e.f#d + g {}");
    }

    @Test
    void testParsingSelector6() throws Exception {
        testValid("a ~ b { x: y}");
    }

    @Test
    void testParsingSelector7() throws Exception {
        testValid("a /deep/ b { x: y}");
    }

    @Test
    void testParsingExpr1() throws Exception {
        testValid("aab {x:s r t}");
    }

    @Test
    void testParsingExpr2() throws Exception {
        testValid("aab {x:s 1em t}");
    }

    @Test
    void testParsingExpr3() throws Exception {
        testValid("aab {x:-1px +1px -1px 1.7px}");
    }

    @Test
    void testParsingURL() throws Exception {
        testValid("a { x: url('https://test.com') }");
    }

    @Test
    void testParsingHexcolor() throws Exception {
        testValid("a { x: #fff }");
    }

    @Test
    void testParsingFunction1Arg() throws Exception {
        testValid("a { x: f(1) }");
    }

    @Test
    void testParsingFunctionManyArgs() throws Exception {
        testValid("a { x: f(1, 2, 3) }");
    }

    @Test
    void testParsingFilterFunctions() throws Exception {
        testValid("a { filter: drop-shadow(1 2 3) custom(1 2 3);"
                + "filter: drop-shadow(1, 2, 3) custom(1, 2, 3);}");
    }

    @Test
    void testParsingWebkitFilterFunctions() throws Exception {
        testValid("a { filter: -webkit-drop-shadow(1 2) -webkit-custom(1 2);"
                + "filter: -webkit-drop-shadow(1, 2) -webkit-custom(1, 2);}");
    }

    @Test
    void testParsingLocalFunctions() throws Exception {
        testValid("@font-face { src: local(Gentium), url(Gentium.woff);"
                + "src: local(Gentium Bold), local(Gentium-Bold), url(GentiumBold.woff);}");
    }

    @Test
    void testParsingAt1() throws Exception {
        testValid("@import url('https://test.com/test.css');");
    }

    @Test
    void testParsingAt2() throws Exception {
        testValid("@import url(https://test.com/test.css);");
    }

    @Test
    void testParsingAt3() throws Exception {
        testValid("@component a extends b {\n" +
                "@def z 1;\n" +
                "x {y: z}\n" +
                "}");
    }

    @Test
    void testParsingDef1() throws Exception {
        testValid("@def RC_TOP_LEFT        tl;\n" +
                "@def RC_TOP_RIGHT       tr;\n" +
                "@def BASE_WARNING_LINK_COLOR   #c3d9ff; /* light blue */"
        );
    }

    @Test
    void testParsingDef3() throws Exception {
        testValid("@def A_B /* @default */ inherit;");
    }

    @Test
    void testParsingAttribute1() throws Exception {
        testValid("a[href=\"http://www.w3.org/\"]{\n" +
                "bla:d\n" +
                "}");
    }

    @Test
    void testParsingAttribute2() throws Exception {
        testValid("*[lang|=\"en\"] { color : red }");
    }

    @Test
    void testParsingPseudo1() throws Exception {
        testValid("a:foo {\n bla : d ; }");
    }

    @Test
    void testParsingPseudo2() throws Exception {
        testValid("a:lang(en) {\n bla : d ; }");
    }

    @Test
    void testParsingIf1() throws Exception {
        testValid("@if (RTL_LANG) {\n" +
                " @def RTL_FLAG 1; \n" +
                " @def LEFT right;\n" +
                "} @else {\n" +
                " @def IMGURL url('images/image.gif');\n" +
                "}");
    }

    @Test
    void testParsingIf2() throws Exception {
        testValid("@if BROWSER_IE6 {\n" +
                "  @def FUNBOX_MARGIN                    0;\n" +
                "} @elseif BROWSER_IE {\n" +
                "  @def FUNBOX_MARGIN                    1 0 -1px 0;\n" +
                "} @elseif BROWSER_FF3_OR_HIGHER {\n" +
                "  @def FUNBOX_MARGIN                    -2px 0 0 0;\n" +
                "} @else {\n" +
                "    @if(A) { @def BB 23; }\n" +
                "  @def FUNBOX_MARGIN                    -2px 0 -1px 0;\n" +
                "}");
    }

    @Test
    void testParsingIf3() throws Exception {
        testValid("@if (RTL_LANG) {\n" +
                " CSS_RULE2.CLASS#id{ d:34em; }\n" +
                "} @else {\n" +
                "}");
    }

    @Test
    void testParsingParenthesizedTerm() throws Exception {
        testValid("@if (FOO) { x { y: z } }");
    }

    @Test
    void testParsingBooleanTerm1() throws Exception {
        testValid("@if ( A && (!B || C )) { @def RTL_FLAG 1;}");
    }

    @Test
    void testParsingBooleanTerm2() throws Exception {
        testValid("@if (!A &&   !B || C || !(F && G ) ) { @def RTL_FLAG 1;}");
    }

    @Test
    void testParsingComplexDef1() throws Exception {
        testValid("@def A a, b, c;");
    }

    @Test
    void testParsingComplexDef2() throws Exception {
        testValid("@def FONT a, b, c 14px/2em #fff;");
    }

    @Test
    void testParsingEqualsOperator() throws Exception {
        testValid(".CSS_ {\n" +
                " filter: alpha(opacity = 85) ;\n" +
                "}");
    }

    @Test
    void testParsingColonFunctionName() throws Exception {
        testValid("x {y: a.b:c(d)}");
    }

    @Test
    void testParsingColonFunctionName2() throws Exception {
        testValid(".CSS_ {\n" +
                "-ms-filter: \"progid:DXImageTr.Microsoft.Alpha(Opacity=80)\" ;\n" +
                "filter: progid:DXImageTr.Microsoft.AlphaImageLoader" +
                "(src='images/muc_bubble_left.png', sizingMethod='scale' );\n" +
                "}");
    }

    @Test
    void testParsingEmptyPseudo() throws Exception {
        testValid("::a, :a[b]::c { x: y}");
    }

    @Test
    void testParsingArbitraryDim() throws Exception {
        testValid("a {x: 2emelet 3x 5t}");
    }

    @Test
    void testSelectorWithSpace() throws Exception {
        testValid("a /* x */ , b {x: y}");
    }

    @Test
    void testIeRect() throws Exception {
        // Non-standard IE workaround.
        testValid(".a { clip: rect(0 0 0 0);}");
    }

    @Test
    void testEllipse() throws Exception {
        testValid(".a { clip-path: ellipse(150px 300px at 50% 50%);}");
    }

    @Test
    void testInset() throws Exception {
        testValid(".a { clip-path: inset(100px 100px 100px 100px);}");
    }

    @Test
    void testCircle() throws Exception {
        testValid(".a { clip-path: circle(50% at right 5px bottom 10px);}");
    }

    @Test
    void testPolygon() throws Exception {
        testValid(".a { clip-path: polygon(0 0, 0 300px, 300px 600px);}");
    }

    @Test
    void testEqualAttribute() throws Exception {
        testValid("h1[foo=\"bar\"] {x : y}");
    }

    @Test
    void testCaretEqualAttribute() throws Exception {
        testValid("h1[foo^=\"bar\"] {x : y}");
    }

    @Test
    void testDollarEqualAttribute() throws Exception {
        testValid("h1[foo$=\"bar\"] {x : y}");
    }

    @Test
    void testAsteriskEqualAttribute() throws Exception {
        testValid("h1[foo*=\"bar\"] {x : y}");
    }

    @Test
    void testPipeEqualAttribute() throws Exception {
        testValid("h1[foo|=\"bar\"] {x : y}");
    }

    @Test
    void testImageSet() throws Exception {
        testValid("div:before {"
                + "content: -webkit-image-set(url(a.png) 1x, url(b.png) 2x);"
                + "content: -moz-image-set(url(a.png) 1x, url(b.png) 2x);"
                + "content: -o-image-set(url(a.png) 1x, url(b.png) 2x);"
                + "content: image-set(url(a.png) 1x, url(b.png) 2x);"
                + "}");
    }

    @Test
    void testWebkitGradient() throws Exception {
        CssTree tree = testValid(".CSS { background: " +
                "-webkit-gradient(linear, 0 0, 0 100%, from(#fff), to(#ddd)) }");

        CssRootNode root = tree.getRoot();
        assertThat(root).isNotNull();
        assertThat(AstPrinter.print(tree))
                .isEqualTo(
                        "[[.CSS]{[background:["
                                + "-webkit-gradient(linear,0 0,0 100%,from(#fff),to(#ddd))];]}]");

        CssRulesetNode ruleset =
                (CssRulesetNode) tree.getRoot().getBody().getChildAt(0);
        CssDeclarationNode decl =
                (CssDeclarationNode) ruleset.getDeclarations().getChildAt(0);
        CssFunctionNode function =
                (CssFunctionNode) decl.getPropertyValue().getChildAt(0);
        CssFunctionArgumentsNode args = function.getArguments();
        assertThat(args.numChildren())
                .as("The argument list should be flattened, and contain "
                        + "7 arguments + 6 separators (4 commas and 2 meaningful spaces).")
                .isEqualTo(13);
    }

    @Test
    void testGradients() throws Exception {
        testValid("div {"
                + "a:radial-gradient(-88px, -500px, #6A6A7A, #333, #000);"
                + "b:radial-gradient(30% 30%, closest-corner, white, black);"
                + "c:radial-gradient(center, 5em 40px, white, black);"
                + "d:linear-gradient(bottom left, red 20px, yellow, green,"
                + "blue 90%);"
                + "e:repeating-linear-gradient(left, red 10%, blue 30%);"
                + "f:repeating-radial-gradient(top left, circle, red, blue 10%,"
                + "red 20%);"
                + "}");
    }

    /* http://www.webkit.org/blog/1424/css3-gradients/ */
    @Test
    void testWebkitGradients() throws Exception {
        testValid("div {"
                + "a:-webkit-radial-gradient(-88px, -500px, #6A6A7A, #333, #000);"
                + "b:-webkit-radial-gradient(30% 30%, closest-corner, white, black);"
                + "c:-webkit-radial-gradient(center, 5em 40px, white, black);"
                + "d:-webkit-linear-gradient(bottom left, red 20px, yellow, green,"
                + "blue 90%);"
                + "e:-webkit-repeating-linear-gradient(left, red 10%, blue 30%);"
                + "f:-webkit-repeating-radial-gradient(top left, circle, red, blue 10%,"
                + "red 20%);"
                + "}");
    }

    @Test
    void testMozillaGradients() throws Exception {
        testValid("div {"
                + "a:-moz-radial-gradient(-88px, -500px, #6A6A7A, #333, #000);"
                + "b:-moz-radial-gradient(30% 30%, closest-corner, white, black);"
                + "c:-moz-radial-gradient(center, 5em 40px, white, black);"
                + "d:-moz-linear-gradient(bottom left, red 20px, yellow, green,"
                + "blue 90%);"
                + "e:-moz-repeating-linear-gradient(left, red 10%, blue 30%);"
                + "f:-moz-repeating-radial-gradient(top left, circle, red, blue 10%,"
                + "red 20%);"
                + "}");
    }

    @Test
    void testOperaGradients() throws Exception {
        testValid("div {"
                + "a:-o-radial-gradient(-88px, -500px, #6A6A7A, #333, #000);"
                + "b:-o-radial-gradient(30% 30%, closest-corner, white, black);"
                + "c:-o-radial-gradient(center, 5em 40px, white, black);"
                + "d:-o-linear-gradient(bottom left, red 20px, yellow, green,"
                + "blue 90%);"
                + "e:-o-repeating-linear-gradient(left, red 10%, blue 30%);"
                + "f:-o-repeating-radial-gradient(top left, circle, red, blue 10%,"
                + "red 20%);"
                + "}");
    }

    @Test
    void testInternetExplorerGradients() throws Exception {
        testValid("div {"
                + "a:-ms-radial-gradient(-88px, -500px, #6A6A7A, #333, #000);"
                + "b:-ms-radial-gradient(30% 30%, closest-corner, white, black);"
                + "c:-ms-radial-gradient(center, 5em 40px, white, black);"
                + "d:-ms-linear-gradient(bottom left, red 20px, yellow, green,"
                + "blue 90%);"
                + "e:-ms-repeating-linear-gradient(left, red 10%, blue 30%);"
                + "f:-ms-repeating-radial-gradient(top left, circle, red, blue 10%,"
                + "red 20%);"
                + "}");
    }

    @Test
    void testKonquererGradients() throws Exception {
        // Taken from http://twitter.github.com/bootstrap/1.4.0/bootstrap.css
        testValid("div {"
                + "background-image: -khtml-gradient(linear, left top, left bottom, "
                + "    from(#333333), to(#222222));"
                + "}");
    }

    @Test
    void testWebkitMinDevicePixelRatio() throws Exception {
        testValid("@media screen and (-webkit-min-device-pixel-ratio:0) {}");
    }

    @Test
    void testMediaQuery() throws Exception {
        testValid("@media screen and (max-height: 300px) and (min-width: 20px) {}");
    }

    @Test
    void testMediaQueryRatioNoSpaces() throws Exception {
        testValid("@media screen and (aspect-ratio: 3/4) {}");
    }

    @Test
    void testMediaQueryRatioWithSpaces() throws Exception {
        testValid("@media screen and (aspect-ratio: 3 / 4) {}");
    }

    @Test
    void testMediaQueryRatioWithManyLeadingSpaces() throws Exception {
        testValid("@media screen and (aspect-ratio: 3    / 4) {}");
    }

    @Test
    void testMediaQueryRatioWithTrailingSpaces() throws Exception {
        testValid("@media screen and (aspect-ratio: 3/ 4) {}");
    }

    @Test
    void testMediaQueryRatioWithNoTrailingSpaces() throws Exception {
        testValid("@media screen and (aspect-ratio: 3 /4) {}");
    }

    @Test
    void testMozLinearGradient() throws Exception {
        testValid(".CSS { background-image: " +
                "-moz-linear-gradient(bottom, #c0c0c0 0%, #dddddd 90%) }");
    }

    @Test
    void testParsingWebkitKeyframes1() throws Exception {
        testValid("@-webkit-keyframes bounce {\n" +
                "  from {\n" +
                "    left: 0px;\n" +
                "  }\n" +
                "  to {\n" +
                "    left: 200px;\n" +
                "  }\n" +
                "}\n");
    }

    @Test
    void testParsingMozKeyframes1() throws Exception {
        testValid("@-moz-keyframes bounce {\n" +
                "  from {\n" +
                "    left: 0px;\n" +
                "  }\n" +
                "  to {\n" +
                "    left: 200px;\n" +
                "  }\n" +
                "}\n");
    }

    @Test
    void testParsingWebkitKeyframes2() throws Exception {
        testValid("@-webkit-keyframes pulse {\n" +
                "  0% {\n" +
                "    background-color: red;\n" +
                "    opacity: 1.0;\n" +
                "    -webkit-transform: scale(1.0) rotate(0deg);\n" +
                "  }\n" +
                "  33.33% {\n" +
                "    background-color: blue;\n" +
                "    opacity: 0.75;\n" +
                "    -webkit-transform: scale(1.1) rotate(-5deg);\n" +
                "  }\n" +
                "  66.66% {\n" +
                "    background-color: green;\n" +
                "    opacity: 0.5;\n" +
                "    -webkit-transform: scale(1.1) rotate(5deg);\n" +
                "  }\n" +
                "  100% {\n" +
                "    background-color: red;\n" +
                "    opacity: 1.0;\n" +
                "    -webkit-transform: scale(1.0) rotate(0deg);\n" +
                "  }\n" +
                "}");
    }

    @Test
    void testParsingWebkitKeyframes3() throws Exception {
        testValid("@-webkit-keyframes bounce {\n" +
                "  0%, 51.2% {\n" +
                "    left: 0px;\n" +
                "    background: red;\n" +
                "  }\n" +
                "  25%, 90.5% {\n" +
                "    left: 200px;\n" +
                "    background: green;\n" +
                "  }\n" +
                "  25% {\n" +
                "    background: blue;\n" +
                "  }\n" +
                "}");
    }

    @Test
    void testParsingWebkitKeyframes4() throws Exception {
        testValid("@-webkit-keyframes from {}");
        testValid("@-webkit-keyframes to {}");
        testValid("from {}");
        testValid("to {}");
    }

    @Test
    void testEscapingInDoubleQuoteString() throws Exception {
        testValid("body {content: \"\\0af9bcHH\"}");
        testValid("body {content: \"\\0HH\"}");
        testValid("body {content: \"\\aHH\"}");
        testValid("body {content: \"\\gHH\"}");
        testValid("body {content: \"\\\"'HH\"}");
    }

    @Test
    void testEscapingInSingleQuoteString() throws Exception {
        testValid("body {content: '\\0af9bcHH'}");
        testValid("body {content: '\\0HH'}");
        testValid("body {content: '\\aHH'}");
        testValid("body {content: '\\gHH'}");
        testValid("body {content: '\"\\'HH'}");
    }

    @Test
    void testPseudoFunction() throws Exception {
        testValid("div :lang(en) { color: #FFF; }");
        testValid(":lang(fr) { color: #FFF; }");
    }

    @Test
    void testPseudoNth() throws Exception {
        testValid("div :nth-child(1n+1) { color: #FFF; }");
        testValid("div :nth-child(n+1) { color: #FFF; }");
        testValid("div :nth-child(+n+2) { color: #FFF; }");
        testValid("div :nth-child(n-1) { color: #FFF; }");
        testValid("div :nth-child(-n-1) { color: #FFF; }");
        testValid("div :nth-child(+2n+3) { color: #FFF; }");
        testValid("div :nth-child(-5n+1) { color: #FFF; }");
        // just 'n' is not supported by WebKit yet
        testValid("div :nth-child(n) { color: #FFF; }");
        testValid("div :nth-child(-n) { color: #FFF; }");
        testValid("div :nth-child(+n) { color: #FFF; }");
        testValid("div :nth-child(n-0) { color: #FFF; }");
        testValid("div :nth-child(0n+0) { color: #FFF; }");
        testValid("div :nth-child(1) { color: #FFF; }");
        testValid("div :nth-child(+7) { color: #FFF; }");
        testValid("div :nth-child(-9) { color: #FFF; }");
        testValid("div :nth-child(odd) { color: #FFF; }");
        testValid("div :nth-child(even) { color: #FFF; }");
    }

    @Test
    void testPseudoNot() throws Exception {
        testValid("p :not(.classy) { color: #123; }");
        testValid("p :not(div) { color: #123; }");
        testValid("p:not(div) { color: #123; }");
        testValid("p :not(  div  ) { color: #123; }");
        testValid("p :not(#id) { color: #123; }");
        testValid("*:not(:link):not(:visited) {}");
    }

    @Test
    void testPseudoElements() throws Exception {
        testValid("p::first-line { text-transform: uppercase }");
        testValid("p::first-letter { color: green; font-size: 200% }");
        testValid("div::after { color: #123; }");
        testValid("div::before { color: #123; }");
    }

    @Test
    void testOldPseudoElements() throws Exception {
        testValid("p:first-line { text-transform: uppercase }");
        testValid("p:first-letter { color: green; font-size: 200% }");
        testValid("div:after { color: #123; }");
        testValid("div:before { color: #123; }");
    }

    @Test
    void testMixinDefinitions() throws Exception {
        testValid("@defmixin name(PAR1, PAR2) { prop1: PAR1; prop2: PAR2 }");
        testValid("@defmixin name(  PAR1  , PAR2 )"
                + "{ prop1: PAR1; prop2: PAR2 }");
        testValid("@defmixin name(PAR1, PAR2) { prop1: PAR1; prop2: CONST; }");
    }

    @Test
    void testMixins() throws Exception {
        testValid("div { @mixin name(); }");
        testValid("div { @mixin name( ) ; }");
        testValid("div { prop1: val; @mixin defname(2px, #fff, 23%); }");
        testValid("div { prop1: val; @mixin defname(); p:v;}");
        testValid("div { @mixin foo(1px/1em); }");
        testValid("div { @mixin foo(1px 1px); }");
    }

    @Test
    void testUnquotedUrl() throws Exception {
        testValid("div { background-image: url(https://google.com/logo.png) }");
    }

    @Test
    void testFunctionApplicationUrl() throws Exception {
        testValid("div { background-image: url(dataUrl('s')) }");
    }

    @Test
    void testUrlOfFunctionOfId() throws Exception {
        // Bare URLs in function arguments are deprecated, but
        // we have some dependent code to cleanup before removing
        // the feature.
        testValid("div { background-image: url(dataUrl(x)); }");
    }

    @Test
    void testFn() throws Exception {
        testValid("div { background-image: url(https://foo) }");
    }

    @Test
    void testUrlPrefix() throws Exception {
        testTree("div { background-image: url-prefix(https://fo); }",
                "[[div]{[background-image:[url-prefix(https://fo)];]}]");
    }

    @Test
    void testUrlPrefix2() throws Exception {
        testTree("div { background-image: url-prefix(fn(0)); }",
                "[[div]{[background-image:[url-prefix(fn(0))];]}]");
    }

    @Test
    void testEmptyUrl() throws Exception {
        testValid("div { background-image: url() }");
    }

    @Test
    void testUrlWithWhitespace() throws Exception {
        testTree("div { background-image: url( 'https://google.com/logo.png'); }",
                "[[div]{[background-image:"
                        + "[url('https://google.com/logo.png')];]}]");
    }

    @Test
    void testUnquotedUrlWithWhitespace() throws Exception {
        testTree("div { background-image: url( https://google.com/logo.png); }",
                "[[div]{[background-image:"
                        + "[url(https://google.com/logo.png)];]}]");
    }

    @Test
    void testCdoCdc() throws Exception {
        testTree(
                "<!--\ndiv { color: red; }\n-->",
                "[[div]{[color:[[red]];]}]");
    }

    @Test
    void testIntraPropertyCdoCdc() {
        String css = ".foo{border:1px<!--solid-->blue;}";
        try {
            parse(css);
            Assertions.fail("CDO should not be accepted in property values.");
        } catch (GssParserException e) {
            assertThat(e.getGssError().getLocation().getBeginCharacterIndex()).as(
                            "The error should reflect that CDO is not accepted in property " + "values.")
                    .isEqualTo(css.indexOf("<!--"));
        }
    }

    @Test
    void testMicrosoftListAtRule() throws Exception {
        // This is syntactically valid according to CSS3, so we should
        // be able to ignore the proprietary @list rule and not fail
        // the whole parse.
        String[] samples = new String[]{
                "@list l0\n"
                        + "{mso-list-id:792754432;}\n"
                        + "div { border: solid thin black }",
                "@list l0:level1\n"
                        + "{mso-list-id:792754432;}\n"
                        + "div { border: solid thin black }"};
        for (String css : samples) {
            // no exceptions the first time
            CssTree t1 = parse(css);
            String output1 = CompactPrinter.printCompactly(t1);
            // also no exceptions the second time
            CssTree t2 = parse(output1);
            // and the we've reached a fixed point
            assertThat(AstPrinter.print(t2)).isEqualTo(AstPrinter.print(t1));
        }
    }

    @Test
    void testRunawayMicrosoftListAtRule() {
        String[] samples = new String[]{
                // unterminated block
                "@list l0 {mso-list-id:792754432;",
                // unterminated nested paren
                "@list l0 {mso-list-id:792754432;(}",
                // improper nesting with parens
                "@list l0 {mso-list-id:792754432;(})",
                // unterminated block, unmatched open bracket
                "@list l0 {mso-list-id:792754432;[",
                // unterminated block, close bracket without matching open bracket
                "@list l0 {mso-list-id:792754432;]"};
        for (String css : samples) {
            try {
                parse(css);
                Assertions.fail("The compiler should only accept complete @list rules, not " + css);
            } catch (GssParserException e) {
                // expected
            }
        }
    }

    @Test
    void testCustomBorderProperty() throws Exception {
        testTree(
                "a { border-height: 1em; }",
                "[[a]{[border-height:[[1em]];]}]");
        testTree(
                "a { border-left-height: 1em; }",
                "[[a]{[border-left-height:[[1em]];]}]");
        testTree(
                "a { border-right-height: 1em; }",
                "[[a]{[border-right-height:[[1em]];]}]");
        testTree(
                "a { border-top-height: 1em; }",
                "[[a]{[border-top-height:[[1em]];]}]");
        testTree(
                "a { border-bottom-height: 1em; }",
                "[[a]{[border-bottom-height:[[1em]];]}]");
    }

    @Test
    void testForLoop() throws Exception {
        testTree(
                "@for $i from 1 to 6 {}",
                "[@for [$i] [from] [1] [to] [6]{}]");
    }

    @Test
    void testForLoopWithStep() throws Exception {
        testTree(
                "@for $i from 1 to 6 step 2 {}",
                "[@for [$i] [from] [1] [to] [6] [step] [2]{}]");
    }

    @Test
    void testForLoopWithVariables() throws Exception {
        testTree(
                "@for $i from $x to $y step $z {}",
                "[@for [$i] [from] [$x] [to] [$y] [step] [$z]{}]");
    }

    @Test
    void testForLoopWithVariablesInBlock() throws Exception {
        testTree(
                "@for $i from 1 to 2 { .foo-$i { padding: $i } }",
                "[@for [$i] [from] [1] [to] [2]{[.foo-$i]{[padding:[[$i]];]}}]");
    }

    @Test
    void testComments() throws GssParserException {
        testTree("div {}/*comment*/", "[[div]{[]}]");
        testTree("div {}/*comment*/p {}", "[[div]{[]}[p]{[]}]");
        testTree("div {}/***comment**/p {}", "[[div]{[]}[p]{[]}]");
        testTree("div {}/***c/o**m//m***e////nt**/p {}", "[[div]{[]}[p]{[]}]");
        testTree("div {}/***c/o**m//m/***e////nt/***/p {}", "[[div]{[]}[p]{[]}]");
        testTree("div {}/****************/p {}", "[[div]{[]}[p]{[]}]");
        testTree("div {}/**/p {}", "[[div]{[]}[p]{[]}]");
        testTree("div {}/**/p {}/**/", "[[div]{[]}[p]{[]}]");
        testTree("div {}/**/p {}/**/div {}", "[[div]{[]}[p]{[]}[div]{[]}]");
    }

    @Test
    void testUnicodeRange() throws Exception {
        testValid("@font-face { unicode-range: U+26;}");
        testValid("@font-face { unicode-range: U+0015-00FF;}");
        testValid("@font-face { unicode-range: U+A015-C0FF;}");
        testValid("@font-face { unicode-range: U+26??;}");
    }

    @Test
    void testCalc_simple_noUnits() throws Exception {
        testValid(".elem { width: calc(5*2) }");
        testTree(".elem { width: calc(5*2) }", "[[.elem]{[width:[calc([[5]*[2]])];]}]");
    }

    @Test
    void testCalc_simple() throws Exception {
        testValid(".elem { width: calc(5px*2) }");
        testTree(".elem { width: calc(5px*2) }", "[[.elem]{[width:[calc([[5px]*[2]])];]}]");
    }

    @Test
    void testCalc_simpleConstant() throws Exception {
        testValid("@def A 5px; .elem { width: calc(A*2) }");
        testTree(
                "@def A 5px; .elem { width: calc(A*2) }",
                "[@def [A] [5px];[.elem]{[width:[calc([[A]*[2]])];]}]");
    }

    @Test
    void testCalc_complexConstant() throws Exception {
        testValid("@def A 5px+2; .elem { width: calc(A*2) }");
        testTree(
                "@def A 5px; .elem { width: calc(A*2) }",
                "[@def [A] [5px];[.elem]{[width:[calc([[A]*[2]])];]}]");
    }

    @Test
    void testCalc_complexConstant_unaryOperator() throws Exception {
        testValid("@def A -5px; .elem { width: calc(A/2) }");
        testTree(
                "@def A -5px; .elem { width: calc(A/2) }",
                "[@def [A] [-5px];[.elem]{[width:[calc([[A]/[2]])];]}]");
    }

    @Test
    void testCalc_withParenthesizedSums() throws Exception {
        testValid("p { width: calc(4 * (5px * 2)); }");
        testTree(
                "p { width: calc(4 * (5px * 2)); }", "[[p]{[width:[calc([[4]*[([5px]*[2])]])];]}]");
    }

    @Test
    void testCalc_fourOperands() throws Exception {
        testValid("p { width: calc(4 + 5 + 6 + 7);}");
        testTree(
                "p { width: calc(4 + 5 + 6 + 7);}", "[[p]{[width:[calc([[4] + [[5] + [[6] + [7]]]])];]}]");
    }

    @Test
    void testCalc_nestedConstant() throws Exception {
        testValid("@def A 5px; p { width: calc((A + 4) - (A * A)); }");
        testTree(
                "@def A 5px; p { width: calc((A + 4) - (A * A)); }",
                "[@def [A] [5px];[p]{[width:[calc([[([A] + [4])] - [([A]*[A])]])];]}]");
    }

    @Test
    void testNumericNodeLocation() throws GssParserException {
        CssTree tree = new GssParser(new SourceCode(null, "div{width:99px;}")).parse();
        final CssNumericNode[] resultHolder = new CssNumericNode[1];
        tree.getVisitController()
                .startVisit(
                        new DefaultTreeVisitor() {
                            @Override
                            public boolean enterValueNode(CssValueNode value) {
                                if (value instanceof CssNumericNode) {
                                    assertThat(resultHolder[0]).isNull();
                                    resultHolder[0] = (CssNumericNode) value;
                                }
                                return true;
                            }
                        });
        assertThat(resultHolder[0]).isNotNull();
        SourceCodeLocation location = resultHolder[0].getSourceCodeLocation();
        assertThat(location.getEndCharacterIndex() - location.getBeginCharacterIndex())
                .isEqualTo("99px".length());
    }

    @Test
    void testCustomDeclaration() throws GssParserException {
        testTree(":root { --test: 10px; }", "[[:root]{[--test:[[10px]];]}]");
    }

    @Test
    void testNoValueShouldFailCustomDeclaration() {
        assertThrows(GssParserException.class, () -> testValid(":root { --var:; }"));
    }

    @Test
    void testCustomPropertyReferenceInCalc() throws GssParserException {
        testValid("div { width: calc(10px * var(--test)); }");
    }

    @Test
    void testDefaultValue() throws GssParserException {
        testValid(".class { width: var(--test, 20px); }");
    }

    @Test
    void testCalcInVarDefaultValue() throws GssParserException {
        testValid(".class { width: var(--test, calc(100% - 20px)); }");
    }

    private CssTree parse(List<SourceCode> sources) throws GssParserException {
        GssParser parser = new GssParser(sources);
        return parser.parse();
    }

    private CssTree parse(String gss) throws GssParserException {
        return parse(ImmutableList.of(new SourceCode("test", gss)));
    }
}
