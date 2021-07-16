/*
 * Copyright 2008 Google Inc.
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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CssNode}
 *
 * @author oana@google.com (Oana Florescu)
 */
class CssNodeTest {

    @Test
    void testNodeCreation() {
        CssLiteralNode node = new CssLiteralNode("");

        assertThat(node.getParent()).isNull();
        assertThat(node.getSourceCodeLocation()).isNull();
    }

    @Test
    void testBecomeParent() {
        CssPropertyValueNode parentNode = new CssPropertyValueNode();
        CssLiteralNode childNode = new CssLiteralNode("");
        parentNode.becomeParentForNode(childNode);

        assertThat(childNode.getParent()).isEqualTo(parentNode);
    }

    @Test
    void testDeleteAsParent() {
        CssPropertyValueNode parentNode = new CssPropertyValueNode();
        CssLiteralNode childNode = new CssLiteralNode("");
        parentNode.becomeParentForNode(childNode);
        assertThat(childNode.getParent()).isEqualTo(parentNode);

        parentNode.removeAsParentOfNode(childNode);
        assertThat(childNode.getParent()).isNull();
    }
}
