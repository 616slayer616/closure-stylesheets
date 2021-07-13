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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.css.compiler.ast.testing.AstUtilityTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link CssDeclarationNode}
 *
 * @author oana@google.com (Oana Florescu)
 */
@RunWith(JUnit4.class)
public class CssDeclarationNodeTest extends AstUtilityTestCase {

  @Test
  public void testDeclarationNodeCreation() {
    CssPropertyNode propertyName = new CssPropertyNode("color", null);
    CssDeclarationNode node = new CssDeclarationNode(propertyName);

    assertThat(node.getParent()).isNull();
    assertThat(node.getSourceCodeLocation()).isNull();
    assertThat(node.getPropertyName().toString()).isEqualTo(propertyName.toString());
    assertThat(node.getPropertyValue().isEmpty()).isTrue();
    assertThat(node.isCustomDeclaration()).isFalse();
  }

  @Test
  public void testCompleteDeclarationNodeCreation() {
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
    assertThat(node.toString()).isEqualTo("color:[red]");
  }

  @Test
  public void testCompleteCustomDeclarationNodeCreation() {
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
    assertThat(node.toString()).isEqualTo("--theme-color:[BurlyWood]");
  }
  
  @Test
  public void testDeepCopyOfDeclarationNode() throws Exception {
    CssPropertyNode propertyName = new CssPropertyNode("color", null);
    CssDeclarationNode node1 = new CssDeclarationNode(propertyName);
    node1.setStarHack(false);
    
    CssDeclarationNode node2 = new CssDeclarationNode(node1);
    
    deepEquals(node1, node2);
  }

  @Test
  public void testDeepCopyOfDeclarationNode2() throws Exception {
    CssPropertyNode propertyName = new CssPropertyNode("color", null);
    CssDeclarationNode node1 = new CssDeclarationNode(propertyName);
    node1.setStarHack(true);
    
    CssDeclarationNode node2 = new CssDeclarationNode(node1);
    
    deepEquals(node1, node2);
  }

  @Test
  public void testDeepCopyOfDeclarationNode3() throws Exception {
    CssPropertyNode propertyName = new CssPropertyNode("color", null);
    CssDeclarationNode node1 = new CssDeclarationNode(propertyName);
    node1.setStarHack(true);
    node1.setShouldBeFlipped(true);
    
    CssDeclarationNode node2 = new CssDeclarationNode(node1);
    
    deepEquals(node1, node2);
  }
}
