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

import com.google.common.css.compiler.ast.*;
import com.google.common.css.compiler.ast.CssAtRuleNode.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link HasConditionalNodes}.
 */
@ExtendWith(MockitoExtension.class)
class HasConditionalNodesTest {
    @Mock
    MutatingVisitController visitController;

    @Test
    void testEnterConditionalBlock() {
        HasConditionalNodes pass
                = new HasConditionalNodes(visitController);

        CssConditionalBlockNode node = new CssConditionalBlockNode();
        CssConditionalRuleNode rule = new CssConditionalRuleNode(Type.IF,
                new CssLiteralNode("condition"));
        BackDoorNodeMutation.addRuleToConditionalBlock(node, rule);

        pass.enterConditionalBlock(node);
    }

    @Test
    void testRunPassAndHasConditionalNodes() {
        HasConditionalNodes pass
                = new HasConditionalNodes(visitController);

        visitController.startVisit(pass);

        pass.runPass();
        assertThat(pass.hasConditionalNodes()).isFalse();
    }
}
