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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link RemoveDefaultDeclarations}.
 *
 * @author oana@google.com (Oana Florescu)
 */
@ExtendWith(MockitoExtension.class)
class RemoveDefaultDeclarationsTest {

    @Mock
    MutatingVisitController visitController;

    @Test
    void testRunPass() {
        RemoveDefaultDeclarations pass
                = new RemoveDefaultDeclarations(visitController);
        visitController.startVisit(pass);

        pass.runPass();
    }

    @Test
    void testEnterDeclaration1() {
        RemoveDefaultDeclarations pass
                = new RemoveDefaultDeclarations(visitController);

        CssDeclarationNode node = new CssDeclarationNode(new CssPropertyNode("foo"));
        pass.enterDeclaration(node);
    }

    @Test
    void testEnterDeclaration2() {
        RemoveDefaultDeclarations pass
                = new RemoveDefaultDeclarations(visitController);

        CssDeclarationNode declaration = new CssDeclarationNode(
                new CssPropertyNode("property"),
                new CssPropertyValueNode());
        CssLiteralNode value = new CssLiteralNode("value");
        value.setIsDefault(true);
        BackDoorNodeMutation.addPropertyValueToDeclaration(declaration, value);

        pass.enterDeclaration(declaration);
    }

    /**
     * Two default values: remove entire declaration
     */
    @Test
    void testEnterDeclaration3() {
        RemoveDefaultDeclarations pass
                = new RemoveDefaultDeclarations(visitController);

        CssDeclarationNode declaration = new CssDeclarationNode(
                new CssPropertyNode("prop"),
                new CssPropertyValueNode());
        CssLiteralNode value = new CssLiteralNode("att1");
        value.setIsDefault(true);
        BackDoorNodeMutation.addPropertyValueToDeclaration(declaration, value);
        CssLiteralNode value1 = new CssLiteralNode("att2");
        value1.setIsDefault(true);
        BackDoorNodeMutation.addPropertyValueToDeclaration(declaration, value1);

        pass.enterDeclaration(declaration);
    }

    /**
     * One default value, one non-default value: remove the default value
     */
    @Test
    void testEnterDeclaration4() {
        RemoveDefaultDeclarations pass
                = new RemoveDefaultDeclarations(visitController);

        CssDeclarationNode declaration = new CssDeclarationNode(
                new CssPropertyNode("prop"),
                new CssPropertyValueNode());
        CssLiteralNode value = new CssLiteralNode("att1");
        value.setIsDefault(true);
        BackDoorNodeMutation.addPropertyValueToDeclaration(declaration, value);
        CssLiteralNode value1 = new CssLiteralNode("att2");
        value1.setIsDefault(false);
        BackDoorNodeMutation.addPropertyValueToDeclaration(declaration, value1);

        pass.enterDeclaration(declaration);
        pass.enterValueNode(value);
        pass.enterValueNode(value1);
    }

    /**
     * One default value, one non-default value, but property has position dependent values: remove no
     * values.
     */
    @Test
    void testEnterDeclaration5() {
        RemoveDefaultDeclarations pass
                = new RemoveDefaultDeclarations(visitController);

        CssDeclarationNode declaration = new CssDeclarationNode(
                new CssPropertyNode("margin"),
                new CssPropertyValueNode());
        CssLiteralNode value = new CssLiteralNode("att1");
        value.setIsDefault(true);
        BackDoorNodeMutation.addPropertyValueToDeclaration(declaration, value);
        CssLiteralNode value1 = new CssLiteralNode("att2");
        value1.setIsDefault(false);
        BackDoorNodeMutation.addPropertyValueToDeclaration(declaration, value1);

        pass.enterDeclaration(declaration);
        pass.enterValueNode(value);
        pass.enterValueNode(value1);
        pass.leaveDeclaration(declaration);
    }

    @Test
    void testEnterValueNotDefault() {
        RemoveDefaultDeclarations pass
                = new RemoveDefaultDeclarations(visitController);

        CssLiteralNode value = new CssLiteralNode("att");
        value.setIsDefault(false);

        pass.enterValueNode(value);
    }

    @Test
    void testEnterValueDefault() {
        RemoveDefaultDeclarations pass
                = new RemoveDefaultDeclarations(visitController);

        CssLiteralNode value = new CssLiteralNode("att");
        value.setIsDefault(true);

        pass.enterValueNode(value);
    }

    /**
     * Two default values and !important flag: don't delete them.
     */
    @Test
    void testImportantDefault() {
        RemoveDefaultDeclarations pass
                = new RemoveDefaultDeclarations(visitController);

        CssDeclarationNode declaration = new CssDeclarationNode(
                new CssPropertyNode("prop"),
                new CssPropertyValueNode());
        CssLiteralNode value = new CssLiteralNode("att1");
        CssPriorityNode importantValue = new CssPriorityNode(CssPriorityNode.
                PriorityType.IMPORTANT);
        importantValue.setValue("importantAtt");
        value.setIsDefault(true);
        BackDoorNodeMutation.addPropertyValueToDeclaration(declaration, value);
        BackDoorNodeMutation.addPropertyValueToDeclaration(declaration,
                importantValue);

        pass.enterDeclaration(declaration);
        pass.leaveDeclaration(declaration);
    }
}
