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

import com.google.common.base.Joiner;
import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.GssParser;
import com.google.common.css.compiler.ast.GssParserException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the collect constant definitions compiler pass.
 *
 * @author oana@google.com (Oana Florescu)
 */
class CollectConstantDefinitionsTest {

    @Test
    void testCollect1() {
        final ConstantDefinitions definitions =
                collectConstantDefinitions(
                        lines("@def COLOR red;",
                                "@def BORDER border(COLOR, 3px);",
                                ".CSS_RULE {",
                                "  background: generate(COLOR, 2px);",
                                "  border: BORDER;",
                                "}"));

        assertThat(!definitions.getConstants().isEmpty()).isTrue();
        assertThat(definitions.getConstants()).hasSize(2);

        CssDefinitionNode color = definitions.getConstantDefinition("COLOR");
        assertThat(color.getName()).hasToString("COLOR");
        assertThat(color.getParametersCount()).isEqualTo(1);
        assertThat(color.getParameters().get(0).getValue()).isEqualTo("red");

        CssDefinitionNode border = definitions.getConstantDefinition("BORDER");
        assertThat(border.getName()).hasToString("BORDER");
        assertThat(border.getParametersCount()).isEqualTo(1);
        assertThat(border.getParameters().get(0)).hasToString("border(COLOR,3px)");
    }

    @Test
    void testCollect2() {
        final ConstantDefinitions definitions =
                collectConstantDefinitions(
                        lines("@def COLOR red;",
                                "@def BORDER border(COLOR, 3px);",
                                "@if COND {",
                                "  @def COLOR blue;",
                                "}",
                                ".CSS_RULE {",
                                "  background: generate(COLOR, 2px);",
                                "  border: BORDER;",
                                "}"));

        assertThat(!definitions.getConstants().isEmpty()).isTrue();
        assertThat(definitions.getConstants()).hasSize(2);

        CssDefinitionNode border = definitions.getConstantDefinition("BORDER");
        assertThat(border.getName()).hasToString("BORDER");
        assertThat(border.getParametersCount()).isEqualTo(1);
        assertThat(border.getParameters().get(0)).hasToString("border(COLOR,3px)");

        CssDefinitionNode color = definitions.getConstantDefinition("COLOR");
        assertThat(color.getName()).hasToString("COLOR");
        assertThat(color.getParametersCount()).isEqualTo(1);
        assertThat(color.getParameters().get(0).getValue()).isEqualTo("blue");
    }

    private ConstantDefinitions collectConstantDefinitions(String source) {
        CssTree tree = parseStyleSheet(source);
        // Must create the definition nodes before they can be collected.
        new CreateDefinitionNodes(tree.getMutatingVisitController(),
                new DummyErrorManager()).runPass();

        CollectConstantDefinitions defPass = new CollectConstantDefinitions(tree);
        defPass.runPass();
        return defPass.getConstantDefinitions();
    }

    private CssTree parseStyleSheet(String sourceCode) {
        SourceCode input = new SourceCode("testInput", sourceCode);
        GssParser parser = new GssParser(input);
        try {
            return parser.parse();
        } catch (GssParserException e) {
            throw new RuntimeException(e);
        }
    }

    private String lines(String... lines) {
        return Joiner.on("\n").join(lines);
    }
}
