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

import com.google.common.css.compiler.ast.testing.AstUtilityTestCase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CssDeclarationNode}
 *
 * @author oana@google.com (Oana Florescu)
 */
class CssDeclarationNodeTest extends AstUtilityTestCase {

    @Test
    void testDeclarationNodeCreation() {
        CssPropertyNode propertyName = new CssPropertyNode("color", null);
        CssDeclarationNode node = new CssDeclarationNode(propertyName);

        assertThat(node.getParent()).isNull();
        assertThat(node.getSourceCodeLocation()).isNull();
        assertThat(node.getPropertyName()).hasToString(propertyName.toString());
        assertThat(node.getPropertyValue().isEmpty()).isTrue();
        assertThat(node.isCustomDeclaration()).isFalse();
    }

    @Test
    void testCompleteDeclarationNodeCreation() {
        CssPropertyNode propertyName = new CssPropertyNode("color", null);
        CssLiteralNode colorValue = new CssLiteralNode("red");
        CssPropertyValueNode propertyValue = new CssPropertyValueNode();
        propertyValue.addChildToBack(colorValue);

        CssDeclarationNode node = new CssDeclarationNode(propertyName,
                propertyValue);

        assertThat(node.getParent()).isNull();
        assertThat(node.getSourceCodeLocation()).isNull();
        assertThat(node.getPropertyValue()).isEqualTo(propertyValue);
        assertThat(node.isCustomDeclaration()).isFalse();
        assertThat(node).hasToString("color:[red]");
    }

    @Test
    void testCompleteCustomDeclarationNodeCreation() {
        CssPropertyNode propertyName = new CssPropertyNode("--theme-color", null);
        CssLiteralNode colorValue = new CssLiteralNode("BurlyWood");
        CssPropertyValueNode propertyValue = new CssPropertyValueNode();
        propertyValue.addChildToBack(colorValue);

        CssDeclarationNode node = new CssDeclarationNode(propertyName,
                propertyValue);

        assertThat(node.getParent()).isNull();
        assertThat(node.getSourceCodeLocation()).isNull();
        assertThat(node.getPropertyValue()).isEqualTo(propertyValue);
        assertThat(node.isCustomDeclaration()).isTrue();
        assertThat(node).hasToString("--theme-color:[BurlyWood]");
    }

    @Test
    void testDeepCopyOfDeclarationNode() throws Exception {
        CssPropertyNode propertyName = new CssPropertyNode("color", null);
        CssDeclarationNode node1 = new CssDeclarationNode(propertyName);
        node1.setStarHack(false);

        CssDeclarationNode node2 = new CssDeclarationNode(node1);

        deepEquals(node1, node2);
        assertThat(node1).isNotEqualTo(node2);
    }

    @Test
    void testDeepCopyOfDeclarationNode2() throws Exception {
        CssPropertyNode propertyName = new CssPropertyNode("color", null);
        CssDeclarationNode node1 = new CssDeclarationNode(propertyName);
        node1.setStarHack(true);

        CssDeclarationNode node2 = new CssDeclarationNode(node1);

        deepEquals(node1, node2);
        assertThat(node1).isNotEqualTo(node2);
    }

    @Test
    void testDeepCopyOfDeclarationNode3() throws Exception {
        CssPropertyNode propertyName = new CssPropertyNode("color", null);
        CssDeclarationNode node1 = new CssDeclarationNode(propertyName);
        node1.setStarHack(true);
        node1.setShouldBeFlipped(true);

        CssDeclarationNode node2 = new CssDeclarationNode(node1);

        deepEquals(node1, node2);
        assertThat(node1).isNotEqualTo(node2);
    }
}
