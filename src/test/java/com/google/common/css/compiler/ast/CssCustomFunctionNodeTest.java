/*
 * Copyright 2017 Google Inc.
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
import com.google.common.css.compiler.ast.CssStringNode.Type;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link CssCustomFunctionNode}.
 */
class CssCustomFunctionNodeTest {

    private static final CssValueNode SPACE = new CssLiteralNode(" ");
    private static final CssValueNode COMMA = new CssLiteralNode(",");

    @Test
    void fixupFunctionArgumentsDoesNotModifyLiteralNodes_b35587881() {
        CssValueNode x = new CssLiteralNode("x");
        CssValueNode y = new CssLiteralNode("y");
        ImmutableList<CssValueNode> parameters = ImmutableList.of(x, SPACE, y);
        List<CssValueNode> fixedParameters = CssCustomFunctionNode.fixupFunctionArguments(parameters);

        assertThat(x.getValue()).isEqualTo("x");
        assertThat(y.getValue()).isEqualTo("y");
        assertThat(fixedParameters).asList().hasSize(1);
        assertThat(fixedParameters.get(0)).hasToString("x y");
    }

    @Test
    void stringsAsFunctionArgumentsAreNotHideouslyBroken() {
        CssValueNode x = new CssLiteralNode("x");
        CssValueNode y = new CssStringNode(Type.DOUBLE_QUOTED_STRING, "double quotes!");
        CssValueNode z = new CssStringNode(Type.SINGLE_QUOTED_STRING, "single quotes!");
        ImmutableList<CssValueNode> parameters = ImmutableList.of(x, SPACE, y, COMMA, z);
        List<CssValueNode> fixedParameters = CssCustomFunctionNode.fixupFunctionArguments(parameters);

        assertThat(fixedParameters).asList().hasSize(2);
        assertThat(fixedParameters.get(0)).hasToString("x \"double quotes!\"");
        assertThat(fixedParameters.get(1)).hasToString("'single quotes!'");
    }
}
