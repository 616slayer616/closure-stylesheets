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

import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReplaceConstantReferences}.
 *
 * @author oana@google.com (Oana Florescu)
 */
@ExtendWith(MockitoExtension.class)
class ReplaceConstantReferencesTest {

    @Mock
    MutatingVisitController mockVisitController;
    @Mock
    CssTree mockTree;
    @Mock
    ConstantDefinitions mockDefinitions;
    @Mock
    SourceCodeLocation mockLoc;
    @Mock
    CssConstantReferenceNode mockRefNode;
    @Mock
    ErrorManager mockErrorManager;
    @Captor
    ArgumentCaptor<List<CssNode>> cssNodesCaptor;

    @Test
    void testRunPass() {
        when(mockTree.getMutatingVisitController()).thenReturn(mockVisitController);

        ReplaceConstantReferences pass =
                new ReplaceConstantReferences(
                        mockTree,
                        new ConstantDefinitions(),
                        true /* removeDefs */,
                        new DummyErrorManager(),
                        true /* allowUndefinedConstants */);
        mockVisitController.startVisit(pass);

        pass.runPass();
    }

    @Test
    void testEnterDefinitionNode() {
        when(mockTree.getMutatingVisitController()).thenReturn(mockVisitController);

        ReplaceConstantReferences pass =
                new ReplaceConstantReferences(
                        mockTree,
                        new ConstantDefinitions(),
                        true /* removeDefs */,
                        new DummyErrorManager(),
                        true /* allowUndefinedConstants */);

        mockVisitController.removeCurrentNode();

        CssDefinitionNode node = new CssDefinitionNode(new CssLiteralNode("COLOR"));
        pass.enterDefinition(node);
    }

    @Test
    void testEnterValueNode() {
        CssDefinitionNode def = new CssDefinitionNode(new CssLiteralNode("COLOR"));
        def.getParameters().add(new CssLiteralNode("red"));

        CssPropertyNode prop1 = new CssPropertyNode("padding", null);
        CssPropertyValueNode value1 = new CssPropertyValueNode();
        BackDoorNodeMutation.addChildToBack(value1, new CssNumericNode("5", "px"));

        CssPropertyNode prop2 = new CssPropertyNode("color", null);
        CssPropertyValueNode value2 = new CssPropertyValueNode();
        CssConstantReferenceNode ref = new CssConstantReferenceNode("COLOR", null);
        BackDoorNodeMutation.addChildToBack(value2, ref);

        CssDeclarationNode decl1 = new CssDeclarationNode(prop1);
        decl1.setPropertyValue(value1);
        CssDeclarationNode decl2 = new CssDeclarationNode(prop2);
        decl2.setPropertyValue(value2);

        CssRulesetNode ruleset = new CssRulesetNode();
        CssSelectorNode sel = new CssSelectorNode("foo", null);
        ruleset.addSelector(sel);
        ruleset.addDeclaration(decl1);
        ruleset.addDeclaration(decl2);

        CssBlockNode body = new CssBlockNode(false);
        BackDoorNodeMutation.addChildToBack(body, ruleset);

        CssRootNode root = new CssRootNode(body);
        CssTree tree = new CssTree(null, root);
        ConstantDefinitions constantDefinitions = new ConstantDefinitions();
        constantDefinitions.addConstantDefinition(def);

        ReplaceConstantReferences pass =
                new ReplaceConstantReferences(tree, constantDefinitions,
                        true /* removeDefs */, new DummyErrorManager(),
                        true /* allowUndefinedConstants */);
        pass.runPass();
        assertThat(tree.getRoot().getBody().toString())
                .isEqualTo("[[foo]{[padding:[5px], color:[red]]}]");
    }

    // TODO(oana): Added a task in tracker for fixing these dependencies and
    // making the mocking of objects easier.
    @Test
    void testEnterArgumentNode() {
        CssDefinitionNode def = new CssDefinitionNode(new CssLiteralNode("COLOR"));

        when(mockTree.getMutatingVisitController()).thenReturn(mockVisitController);
        when(mockDefinitions.getConstantDefinition("COLOR")).thenReturn(def);

        ReplaceConstantReferences pass =
                new ReplaceConstantReferences(
                        mockTree,
                        mockDefinitions,
                        true /* removeDefs */,
                        new DummyErrorManager(),
                        true /* allowUndefinedConstants */);

        CssConstantReferenceNode node = new CssConstantReferenceNode("COLOR", null);
        pass.enterArgumentNode(node);

        Mockito.verify(mockVisitController)
                .replaceCurrentBlockChildWith(cssNodesCaptor.capture(), eq(true));
        assertThat(cssNodesCaptor.getValue()).asList().hasSize(1);
        assertThat(cssNodesCaptor.getValue().get(0).getClass()).isEqualTo(CssCompositeValueNode.class);
    }

    @Test
    void testAllowUndefinedConstants() {
        when(mockRefNode.getValue()).thenReturn("Foo");

        ReplaceConstantReferences allowingPass =
                new ReplaceConstantReferences(
                        mockTree,
                        mockDefinitions,
                        true /* removeDefs */,
                        mockErrorManager,
                        true /* allowUndefinedConstants */);
        allowingPass.replaceConstantReference(mockRefNode);

        // This should not cause an error to be reported.
        verify(mockErrorManager, times(0)).report(ArgumentMatchers.any());
    }

    @Test
    void testAllowUndefinedConstantsError() {
        when(mockRefNode.getValue()).thenReturn("Foo");
        when(mockRefNode.getSourceCodeLocation()).thenReturn(mockLoc);

        ReplaceConstantReferences nonAllowingPass =
                new ReplaceConstantReferences(
                        mockTree,
                        mockDefinitions,
                        true /* removeDefs */,
                        mockErrorManager,
                        false /* allowUndefinedConstants */);
        nonAllowingPass.replaceConstantReference(mockRefNode);

        // This should cause an error to be reported.
        verify(mockErrorManager).report(ArgumentMatchers.any());
    }
}
