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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.FunctionalTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link MarkDefaultDefinitions}.
 *
 * @author oana@google.com (Oana Florescu)
 */
@RunWith(JUnit4.class)
public class MarkDefaultDefinitionsTest extends FunctionalTestBase {

  @Test
  public void testMarkDefaultDefinitions1() {
    parseAndBuildTree(
        "/* @default */ @def COLOR red;");
    runPass();

    CssDefinitionNode definition =
        (CssDefinitionNode) tree.getRoot().getBody().getChildren().get(0);

    // Check each of the value nodes in the definition.
    for (CssValueNode node : definition.getParameters()) {
      assertThat(node.getIsDefault()).isTrue();
    }
  }
  
  @Test
  public void testMarkDefaultDefinitions2() {
    parseAndBuildTree(
        "/* @default */ @def PADDING 2px 3px 5px 1px;");
    runPass();

    CssDefinitionNode definition =
        (CssDefinitionNode) tree.getRoot().getBody().getChildren().get(0);

    // Check each of the value nodes in the definitions.
    for (CssValueNode node : definition.getParameters()) {
      assertThat(node.getIsDefault()).isTrue();
    }
  }
  
  @Test
  public void testMarkDefaultDefinition3() {
    parseAndBuildTree(
        "@def PADDING /* @default */ 2px 3px 5px 1px;");
    runPass();

    CssDefinitionNode definition =
        (CssDefinitionNode) tree.getRoot().getBody().getChildren().get(0);

    // Check each of the value nodes in the definitions.
    for (CssValueNode node : definition.getParameters()) {
      assertThat(node.getIsDefault()).isTrue();
    }
  }

  @Test
  public void testMarkDefaultDefinitions4() {
    parseAndBuildTree(
        "@def PADDING 2px 3px 5px 1px;");
    runPass();

    CssDefinitionNode definition =
        (CssDefinitionNode) tree.getRoot().getBody().getChildren().get(0);

    // Check each of the value nodes in the definitions.
    for (CssValueNode node : definition.getParameters()) {
      assertThat(node.getIsDefault()).isFalse();
    }
  }

  @Override
  protected void runPass() {
    MarkDefaultDefinitions pass = new MarkDefaultDefinitions(
        tree.getVisitController());
    pass.runPass();
  }
}
