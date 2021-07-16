/*
 * Copyright 2012 Google Inc.
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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CssCompositeValueNode}.
 *
 * @author chrishenry@google.com (Chris Henry)
 */
class CssCompositeValueNodeTest {

    @Test
    void testDeepCopy() throws Exception {
        CssCompositeValueNode node = new CssCompositeValueNode(
                ImmutableList.<CssValueNode>of(
                        new CssLiteralNode("foo"), new CssLiteralNode("bar")),
                CssCompositeValueNode.Operator.SPACE, null);

        CssCompositeValueNode clone = node.deepCopy();
        assertThat(clone).isNotSameAs(node);
        assertThat(clone.getValues()).isNotSameAs(node.getValues());
        // Operator is enum.
        assertThat(clone.getOperator()).isSameAs(node.getOperator());
        assertThat(clone.getValues()).asList().hasSize(2);

        CssValueNode clonedChild1 = clone.getValues().get(0);
        assertThat(clonedChild1).isNotSameAs(node.getValues().get(0));
        assertThat(clonedChild1.getClass()).isEqualTo(node.getValues().get(0).getClass());
        assertThat(clonedChild1.getValue()).isEqualTo("foo");

        CssValueNode clonedChild2 = clone.getValues().get(1);
        assertThat(clonedChild2).isNotSameAs(node.getValues().get(1));
        assertThat(clonedChild2.getClass()).isEqualTo(node.getValues().get(1).getClass());
        assertThat(clonedChild2.getValue()).isEqualTo("bar");
    }
}
