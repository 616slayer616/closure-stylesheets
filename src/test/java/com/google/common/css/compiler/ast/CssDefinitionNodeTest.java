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

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CssDefinitionNode}.
 *
 * @author oana@google.com (Oana Florescu)
 */
class CssDefinitionNodeTest {

    @Test
    void testDefinitionNodeCreation() {
        CssDefinitionNode definition = new CssDefinitionNode(
                new CssLiteralNode("COLOR"));

        assertThat(definition.getParent()).isNull();
        assertThat(definition.getSourceCodeLocation()).isNull();
        assertThat(definition.getType()).hasToString("@def");
        assertThat(definition).hasToString("@def COLOR []");
    }

    @Test
    void testDefinitionNodeCopy() {
        CssDefinitionNode definition1 = new CssDefinitionNode(
                new CssLiteralNode("COLOR"),
                Lists.newArrayList(new CssCommentNode("/* foo */", null)));
        CssDefinitionNode definition2 = new CssDefinitionNode(definition1);

        assertThat(definition1.getParent()).isNull();
        assertThat(definition2.getParent()).isNull();

        assertThat(definition1.getSourceCodeLocation()).isNull();
        assertThat(definition2.getSourceCodeLocation()).isNull();

        assertThat(definition1.getType()).hasToString("@def");
        assertThat(definition2.getType()).hasToString("@def");

        assertThat(definition1).hasToString("@def COLOR []");
        assertThat(definition2).hasToString("@def COLOR []");

        assertThat(definition1.hasComment("/* foo */")).isTrue();
        assertThat(definition2.hasComment("/* foo */")).isTrue();
    }
}
