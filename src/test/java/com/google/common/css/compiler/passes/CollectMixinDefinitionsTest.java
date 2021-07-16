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

package com.google.common.css.compiler.passes;

import com.google.common.css.compiler.ast.CssMixinDefinitionNode;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.passes.testing.PassesTestBase;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link CollectMixinDefinitions} compiler pass.
 *
 * @author fbenz@google.com (Florian Benz)
 */
@SuppressWarnings("java:S2699")
class CollectMixinDefinitionsTest extends PassesTestBase {
    private Map<String, CssMixinDefinitionNode> definitions;

    @Test
    void testSimpleMixinDefinition() {
        parseAndBuildTree(
                "@defmixin test(PAR1, PAR2) { width: PAR1; height: PAR2; }");
    }

    @Test
    void testCollectedMixinDefinitions() {
        parseAndBuildTree(linesToString(
                "@defmixin test1(PAR1, PAR2) { width: PAR1; height: PAR2; }",
                "@defmixin test2() {}",
                "@defmixin test3(PAR1) { color: PAR1; }"));
        assertThat(definitions).isNotNull();
        assertThat(definitions.keySet()).containsExactlyInAnyOrder("test1", "test2", "test3");
    }

    @Test
    void testDupilicateMixinDefinitionNames() throws GssParserException {
        parseAndRun("@defmixin test() {} @defmixin test() {}",
                CollectMixinDefinitions.DUPLICATE_MIXIN_DEFINITION_NAME_ERROR_MESSAGE);
    }

    @Test
    void testDupilicateArgumentNames() throws GssParserException {
        parseAndRun("@defmixin test(PAR, PAR) {}",
                CollectMixinDefinitions.DUPLICATE_ARGUMENT_NAME_ERROR_MESSAGE);
    }

    @Test
    void testInvalidArgument() throws GssParserException {
        parseAndRun("@defmixin test(Par) {}",
                CollectMixinDefinitions.INVALID_ARGUMENT_ERROR_MESSAGE);
    }

    @Test
    void testInvalidBlock1() throws GssParserException {
        parseAndRun("@if (COND) { @defmixin test(PAR) {} }",
                CollectMixinDefinitions.INVALID_BLOCK_ERROR_MESSAGE);
    }

    @Test
    void testInvalidBlock2() throws GssParserException {
        parseAndRun("@component X { @defmixin test(PAR) {} }",
                CollectMixinDefinitions.INVALID_BLOCK_ERROR_MESSAGE);
    }

    @Override
    protected void runPass() {
        // This pass has to run before.
        new CreateMixins(tree.getMutatingVisitController(), errorManager).runPass();
        new CreateConstantReferences(tree.getMutatingVisitController()).runPass();

        CollectMixinDefinitions collectDefinitions = new CollectMixinDefinitions(
                tree.getMutatingVisitController(), errorManager);
        collectDefinitions.runPass();
        definitions = collectDefinitions.getDefinitions();
    }
}
