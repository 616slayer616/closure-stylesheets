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

import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link ColorValueOptimizer}.
 *
 * @author oana@google.com (Oana Florescu)
 */
class ColorValueOptimizerTest {

    @Test
    void testRunPass() {
        MutatingVisitController visitController = mock(MutatingVisitController.class);
        ColorValueOptimizer pass = new ColorValueOptimizer(visitController);
        visitController.startVisit(pass);

        pass.runPass();
    }

    @Test
    void testEnterValueNode1() {
        CssPropertyNode prop = new CssPropertyNode("color", null);
        CssPropertyValueNode value = new CssPropertyValueNode();
        BackDoorNodeMutation.addChildToBack(value, new CssHexColorNode("#112233", null));

        CssDeclarationNode decl = new CssDeclarationNode(prop);
        decl.setPropertyValue(value);

        CssRulesetNode ruleset = new CssRulesetNode();
        CssSelectorNode sel = new CssSelectorNode("foo", null);
        ruleset.addSelector(sel);
        ruleset.addDeclaration(decl);

        CssBlockNode body = new CssBlockNode(false);
        BackDoorNodeMutation.addChildToBack(body, ruleset);

        CssRootNode root = new CssRootNode(body);
        CssTree tree = new CssTree(null, root);

        ColorValueOptimizer pass = new ColorValueOptimizer(
                tree.getMutatingVisitController());
        pass.runPass();
        assertThat(tree.getRoot().getBody()).hasToString("[[foo]{[color:[#123]]}]");
    }

    @Test
    void testEnterValueNode2() {
        CssPropertyNode prop = new CssPropertyNode("color", null);
        CssPropertyValueNode value = new CssPropertyValueNode();
        BackDoorNodeMutation.addChildToBack(value, new CssHexColorNode("#123344", null));

        CssDeclarationNode decl = new CssDeclarationNode(prop);
        decl.setPropertyValue(value);

        CssRulesetNode ruleset = new CssRulesetNode();
        CssSelectorNode sel = new CssSelectorNode("foo", null);
        ruleset.addSelector(sel);
        ruleset.addDeclaration(decl);

        CssBlockNode body = new CssBlockNode(false);
        BackDoorNodeMutation.addChildToBack(body, ruleset);

        CssRootNode root = new CssRootNode(body);
        CssTree tree = new CssTree(null, root);

        ColorValueOptimizer pass = new ColorValueOptimizer(
                tree.getMutatingVisitController());
        pass.runPass();
        assertThat(tree.getRoot().getBody()).hasToString("[[foo]{[color:[#123344]]}]");
    }

    private CssNumericNode createNumericNode(String value) {
        if (value.endsWith("%")) {
            return new CssNumericNode(value.substring(0, value.length() - 1), "%");
        }
        return new CssNumericNode(value, "");
    }

    private CssFunctionNode createRgbFunctionNode(String r, String g, String b) {
        CssFunctionNode function = new CssFunctionNode(
                CssFunctionNode.Function.byName("rgb"), null /* sourceCodeLocation */);
        List<CssValueNode> values = Lists.newArrayList();
        values.add(createNumericNode(r));
        values.add(createNumericNode(g));
        values.add(createNumericNode(b));
        CssFunctionArgumentsNode args = new CssFunctionArgumentsNode(values);
        function.setArguments(args);
        return function;
    }

    @Test
    void testEnterFunctionNode1() {
        CssPropertyNode prop = new CssPropertyNode("color", null);
        CssPropertyValueNode value = new CssPropertyValueNode();
        CssFunctionNode function = createRgbFunctionNode("0%", "100%", "0%");
        BackDoorNodeMutation.addChildToBack(value, function);

        CssDeclarationNode decl = new CssDeclarationNode(prop);
        decl.setPropertyValue(value);

        CssRulesetNode ruleset = new CssRulesetNode();
        CssSelectorNode sel = new CssSelectorNode("foo", null);
        ruleset.addSelector(sel);
        ruleset.addDeclaration(decl);

        CssBlockNode body = new CssBlockNode(false);
        BackDoorNodeMutation.addChildToBack(body, ruleset);

        CssRootNode root = new CssRootNode(body);
        CssTree tree = new CssTree(null, root);

        ColorValueOptimizer pass = new ColorValueOptimizer(
                tree.getMutatingVisitController());
        pass.runPass();
        assertThat(tree.getRoot().getBody()).hasToString("[[foo]{[color:[#0f0]]}]");
    }

    @Test
    void testCanShortenHex() {
        assertThat(ColorValueOptimizer.canShortenHexString("#000000")).isTrue();
        assertThat(ColorValueOptimizer.canShortenHexString("#00aa22")).isTrue();
        assertThat(ColorValueOptimizer.canShortenHexString("#000001")).isFalse();
        assertThat(ColorValueOptimizer.canShortenHexString("#000")).isFalse();
    }

    @Test
    void testShortenHex() {
        assertThat(ColorValueOptimizer.shortenHexString("#000000")).isEqualTo("#000");
        assertThat(ColorValueOptimizer.shortenHexString("#00aa22")).isEqualTo("#0a2");
    }

    @Test
    void testParseRgbArguments() {
        CssFunctionNode function = createRgbFunctionNode("0", "15", "255");
        assertThat(ColorValueOptimizer.parseRgbArguments(function)).isEqualTo("#000fff");
        function = createRgbFunctionNode("0%", "50%", "100%");
        assertThat(ColorValueOptimizer.parseRgbArguments(function)).isEqualTo("#0080ff");
    }

    @Test
    void testParseRgbArgumentsOutOfRange() {
        // Surprisingly, these are valid according to W3C and should be clamped
        // to valid values.
        CssFunctionNode function = createRgbFunctionNode("1", "1", "300");
        assertThat(ColorValueOptimizer.parseRgbArguments(function)).isEqualTo("#0101ff");
        function = createRgbFunctionNode("-10", "1", "1");
        assertThat(ColorValueOptimizer.parseRgbArguments(function)).isEqualTo("#000101");
    }

    @Test
    void testParseRgbArgumentsBadArgs() {
        CssFunctionNode function = new CssFunctionNode(
                CssFunctionNode.Function.byName("rgb"), null /* sourceCodeLocation */);
        List<CssValueNode> values = Lists.newArrayList();
        values.add(createNumericNode("0"));
        CssFunctionArgumentsNode args = new CssFunctionArgumentsNode(values);
        function.setArguments(args);

        try {
            ColorValueOptimizer.parseRgbArguments(function);
            Assertions.fail("Too few arguments to rgb function; should have thrown.");
        } catch (NumberFormatException expected) {
            // Exception is expected.
        }
    }
}
