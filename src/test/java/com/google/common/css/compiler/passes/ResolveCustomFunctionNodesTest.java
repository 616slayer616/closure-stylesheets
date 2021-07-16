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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import com.google.common.css.compiler.gssfunctions.GssFunctions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ResolveCustomFunctionNodes}.
 */
class ResolveCustomFunctionNodesTest extends NewFunctionalTestBase {

    protected boolean allowUnknownFunctions = false;

    protected Map<String, GssFunction> createTestFunctionMap() {
        return new ImmutableMap.Builder<String, GssFunction>()
                .put("plus", new GssFunctions.AddToNumericValue())
                .put("minus", new GssFunctions.SubtractFromNumericValue())
                .put("mult", new GssFunctions.Mult())
                .put("div", new GssFunctions.Div())
                .put("minimum", new GssFunctions.MinValue())
                .put("maximum", new GssFunctions.MaxValue())
                .build();
    }

    @Override
    protected void runPass() {
        new ResolveCustomFunctionNodes(
                tree.getMutatingVisitController(), errorManager,
                createTestFunctionMap(), allowUnknownFunctions,
                ImmutableSet.<String>of() /* allowedNonStandardFunctions */).runPass();
    }

    @Test
    void testAcceptBuiltInFunction() throws Exception {
        parseAndRun("A { color: rgb(0,0,0) }");
    }

    @Test
    void testUnknownFunctionError() throws Exception {
        parseAndRun("A { width: -example(a,b) }", "Unknown function \"-example\"");
    }

    @Test
    void testUnknownFunctionAllowed() throws Exception {
        allowUnknownFunctions = true;
        parseAndRun("A { width: f(a,b) }");
        assertThat(getFirstPropertyValue().toString()).isEqualTo("[f(a,b)]");
    }

    @Test
    void testWrongNumberOfArgsError() throws Exception {
        parseAndRun("A { width: plus(2px); }",
                "Not enough arguments");
    }

    @Test
    void testWrongArgumentError1() throws Exception {
        parseAndRun("A { width: maximum(2,bar,foo) }",
                "Size must be a CssNumericNode with a unit or 0; was: bar");
    }

    @Test
    void testPlus() throws Exception {
        parseAndRun("A { width: plus(2px, 3px) }");
        assertThat(getFirstPropertyValue().toString()).isEqualTo("[5px]");
    }

    @Test
    void testMinus() throws Exception {
        parseAndRun("A { width: minus(2em, 5.5em) }");
        assertThat(getFirstPropertyValue().toString()).isEqualTo("[-3.5em]");
    }

    @Test
    void testMax() throws Exception {
        parseAndRun("A { width: maximum(-2%, -5%) }");
        assertThat(getFirstPropertyValue().toString()).isEqualTo("[-2%]");
    }

    @Test
    void testMultiply() throws Exception {
        parseAndRun("A { width: mult(-2, -5) }");
        assertThat(getFirstPropertyValue().toString()).isEqualTo("[10]");
    }

    @Test
    void testFunctionWithinFunction() throws Exception {
        parseAndRun("A { width: maximum(10px, maximum(2px, 30px)) }");
        assertThat(getFirstPropertyValue().toString()).isEqualTo("[30px]");
    }
}
