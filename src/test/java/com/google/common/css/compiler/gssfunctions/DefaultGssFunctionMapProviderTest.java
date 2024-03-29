/*
 * Copyright 2011 Google Inc.
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

package com.google.common.css.compiler.gssfunctions;

import com.google.common.collect.ImmutableSet;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import com.google.common.css.compiler.passes.ResolveCustomFunctionNodes;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

/**
 * {@link DefaultGssFunctionMapProviderTest} is a unit test for {@link
 * DefaultGssFunctionMapProvider}, which is used to provide the default functions for <a
 * href="http://code.google.com/p/closure-stylesheets/">Closure Stylesheets</a>.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
class DefaultGssFunctionMapProviderTest extends NewFunctionalTestBase {

    @Override
    protected void runPass() {
        Map<String, GssFunction> functionMap = new DefaultGssFunctionMapProvider()
                .get();
        final boolean allowUnknownFunctions = false;
        final Set<String> allowedNonStandardFunctions = ImmutableSet.of();
        new ResolveCustomFunctionNodes(
                tree.getMutatingVisitController(), errorManager, functionMap,
                allowUnknownFunctions, allowedNonStandardFunctions).runPass();
    }

    @Test
    void testAddPercentagesMissingUnit() throws GssParserException {
        parseAndRun("A { width: add(5%, 10); }",
                "Parameters' units don't match (\"%\" vs \"\")");
    }

    @Test
    void testAddPercentagesZeroSize() throws GssParserException {
        test("A { width: add(5%, 0, 10%); }",
                " A { width: 15%; }");
        test("A { width: add(5%, 0%, 10%); }",
                " A { width: 15%; }");
        test("A { width: add(5%, -0, 10%); }",
                " A { width: 15%; }");
        test("A { width: add(5%, -0%, 10%); }",
                " A { width: 15%; }");
    }

    @Test
    void testAddPercentagesImplicitUnit() throws GssParserException {
        test("A { width: add(5%, 10%); }",
                " A { width: 15%; }");
    }

    @Test
    void testAddNegativeValues() throws GssParserException {
        test("A { width: add(5%, 10%, -25%, 50%); }",
                " A { width: 40%; }");
    }

    @Test
    void testMult() throws GssParserException {
        test("A { width: mult(5, 6); }",
                " A { width: 30; }");
        test("A { width: mult(5%, 10); }",
                " A { width: 50%; }");
        test("A { width: mult(2%, 4, 3); }",
                " A { width: 24%; }");
        test("A { width: mult(2px, -4, 3); }",
                " A { width: -24px; }");
    }

    @Test
    void testMultUnitNotFirst() throws GssParserException {
        parseAndRun("A { width: mult(5, 10%, 15ex); }",
                "Only the first argument may have a unit associated with it, "
                        + " but has unit: %");
        parseAndRun("A { width: mult(5, 10, 15ex); }",
                "Only the first argument may have a unit associated with it, "
                        + " but has unit: ex");
    }

    @Test
    void testDivide() throws GssParserException {
        test("A { width: divide(30, 6); }",
                " A { width: 5; }");
        test("A { width: divide(100%, 5); }",
                " A { width: 20%; }");
        test("A { width: divide(100%, 5, 2); }",
                " A { width: 10%; }");
        test("A { width: divide(256px, -4, 2); }",
                " A { width: -32px; }");
    }

    @Test
    void testDivUnitNotFirst() throws GssParserException {
        parseAndRun("A { width: divide(100px, 10px); }",
                "Only the first argument may have a unit associated with it, "
                        + " but has unit: px");
        parseAndRun("A { width: divide(100, 10, 2ex); }",
                "Only the first argument may have a unit associated with it, "
                        + " but has unit: ex");
    }

    @Test
    void testMaximum() throws GssParserException {
        test("A { width: maximum(5%, 10%); }",
                " A { width: 10%; }");
        test("A { width: maximum(5%, 50%, -25%, 10%); }",
                " A { width: 50%; }");

        // Test various zero values.
        parseAndRun("A { width: maximum(-5%, 0, -10%); }",
                "Parameters' units don't match (\"%\" vs \"\")");
        test("A { width: maximum(-5%, 0%, -10%); }",
                " A { width: 0%; }");
        parseAndRun("A { width: maximum(-5%, -0, -10%); }",
                "Parameters' units don't match (\"%\" vs \"\")");
        test("A { width: maximum(-5%, -0%, -10%); }",
                " A { width: -0%; }");
    }

    @Test
    void testMaxUnspecifiedUnit() throws GssParserException {
        parseAndRun("A { width: maximum(5%, 10); }",
                "Parameters' units don't match (\"%\" vs \"\")");
    }

    @Test
    void testMaxMismatchedUnits() throws GssParserException {
        parseAndRun("A { width: maximum(5%, 10px, 20%); }",
                "Parameters' units don't match (\"%\" vs \"px\")");
    }

    @Test
    void testMinimum() throws GssParserException {
        test("A { width: minimum(5%, 10%); }",
                " A { width: 5%; }");
        test("A { width: minimum(5%, 50%, -25%, 10%); }",
                " A { width: -25%; }");

        // Test various zero values.
        parseAndRun("A { width: minimum(5%, 0, 10%); }",
                "Parameters' units don't match (\"%\" vs \"\")");
        test("A { width: minimum(5%, 0%, 10%); }",
                " A { width: 0%; }");
        parseAndRun("A { width: minimum(5%, -0, 10%); }",
                "Parameters' units don't match (\"%\" vs \"\")");
        test("A { width: minimum(5%, -0%, 10%); }",
                " A { width: -0%; }");
    }

    @Test
    void testMinUnspecifiedUnit() throws GssParserException {
        parseAndRun("A { width: minimum(5%, 10); }",
                "Parameters' units don't match (\"%\" vs \"\")");
    }

    @Test
    void testMinMismatchedUnits() throws GssParserException {
        parseAndRun("A { width: minimum(5%, 10px, 20%); }",
                "Parameters' units don't match (\"%\" vs \"px\")");
    }

    @Test
    void testScalars() throws GssParserException {
        // This is an example of why add() and sub() should be able to take
        // arguments without any units.
        test("A {"
                        + "width: maximum("
                        + "  mult(add(35px, -15px), add(2, 3)), " // 100px
                        + "  divide(sub(500px, 100px), sub(10, 2))"  //  50px
                        + "); }",
                "A { width: 100px; }");

        // This is an example of why maximum() and minimum() should be able to take
        // arguments without any units.
        test("A {"
                        + "width: minimum("
                        + "  mult(add(35px, -15px), maximum(2, 3)), " //  60px
                        + "  divide(sub(500px, 100px), minimum(10, 2))"  // 200px
                        + "); }",
                "A { width: 60px; }");
    }

    @Test
    void testConcat() throws GssParserException {
        test("A { background-image:url(concat('https://', www, '.google.com', \"/example.gif\")); }",
                "A { background-image:url('https://www.google.com/example.gif'); }");
        test("A { x:concat(a); }", "A { x:'a'; }");
        test("A { x:concat(\"'\", \"\\\"\", 'bar','\"', '\\''); }",
                "A { x:'\\'\"bar\"\\'' }");
        test("A { x:concat('\\\\\\\\'); }",
                "A { x:'\\\\\\\\'; }");
    }
}
