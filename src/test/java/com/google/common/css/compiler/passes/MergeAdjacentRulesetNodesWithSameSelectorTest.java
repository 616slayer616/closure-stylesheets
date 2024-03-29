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

import com.google.common.base.Joiner;
import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ast.*;
import com.google.common.css.compiler.passes.testing.AstPrinter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MergeAdjacentRulesetNodesWithSameSelector}.
 *
 * @author oana@google.com (Oana Florescu)
 */
class MergeAdjacentRulesetNodesWithSameSelectorTest {

    @Test
    void testRunPass() {
        MutatingVisitController visitController = mock(MutatingVisitController.class);
        CssTree tree = mock(CssTree.class);
        when(tree.getMutatingVisitController()).thenReturn(visitController);

        MergeAdjacentRulesetNodesWithSameSelector pass =
                new MergeAdjacentRulesetNodesWithSameSelector(tree);
        visitController.startVisit(pass);

        pass.runPass();
    }

    @Test
    void testEnterTree() {
        CssTree tree = new CssTree((SourceCode) null);
        tree.getRulesetNodesToRemove().addRulesetNode(new CssRulesetNode());
        assertThat(tree.getRulesetNodesToRemove().getRulesetNodes()).isNotEmpty();

        MergeAdjacentRulesetNodesWithSameSelector pass =
                new MergeAdjacentRulesetNodesWithSameSelector(tree);
        pass.enterTree(tree.getRoot());
        assertThat(tree.getRulesetNodesToRemove().getRulesetNodes()).isEmpty();
    }

    @Test
    void testPassResult() throws Exception {
        CssTree tree = new GssParser(new SourceCode(null, lines(
                "@-moz-document url-prefix() {",
                "  foo {",
                "    padding: 6px;",
                "   }",
                "  foo {",
                "    margin: 4px;",
                "   }",
                "}",
                "foo, .bar #id {",
                "  padding: 5px;",
                "}",
                "foo, .bar #id {",
                "  margin: 2px;",
                "}"))).parse();

        assertThat(AstPrinter.print(tree))
                .isEqualTo(
                        "[@-moz-document [url-prefix()]"
                                + "{[foo]{[padding:[[6px]];]}[foo]{[margin:[[4px]];]}}"
                                + "[foo,.bar #id]{[padding:[[5px]];]}"
                                + "[foo,.bar #id]{[margin:[[2px]];]}]");

        MergeAdjacentRulesetNodesWithSameSelector pass =
                new MergeAdjacentRulesetNodesWithSameSelector(tree);
        pass.runPass();
        // As the elimination pass is not run here, we still have the one of the old
        // rulesets in each place.
        assertThat(AstPrinter.print(tree))
                .isEqualTo(
                        "[@-moz-document [url-prefix()]"
                                + "{[foo]{[padding:[[6px]];margin:[[4px]];]}[foo]{[margin:[[4px]];]}}"
                                + "[foo,.bar #id]{[padding:[[5px]];margin:[[2px]];]}"
                                + "[foo,.bar #id]{[margin:[[2px]];]}]");
    }

    @Test
    void testPassResult2() {
        CssPropertyNode prop1 = new CssPropertyNode("padding", null);
        CssPropertyValueNode value1 = new CssPropertyValueNode();
        BackDoorNodeMutation.addChildToBack(value1, new CssNumericNode("5", "px"));
        CssDeclarationNode decl1 = new CssDeclarationNode(prop1);
        decl1.setPropertyValue(value1);

        CssPropertyNode prop2 = new CssPropertyNode("display", null);
        CssPropertyValueNode value2 = new CssPropertyValueNode();
        BackDoorNodeMutation.addChildToBack(value2, new CssNumericNode("2", "px"));
        CssDeclarationNode decl2 = new CssDeclarationNode(prop2);
        decl2.setPropertyValue(value2);

        CssRulesetNode ruleset1 = new CssRulesetNode();
        CssSelectorNode sel1 = new CssSelectorNode("foo", null);
        ruleset1.addSelector(sel1);
        ruleset1.addDeclaration(decl1);

        CssRulesetNode ruleset2 = new CssRulesetNode();
        CssSelectorNode sel2 = new CssSelectorNode("foo", null);
        ruleset2.addSelector(sel2);
        ruleset2.addDeclaration(decl2);

        CssBlockNode body = new CssBlockNode(false);
        BackDoorNodeMutation.addChildToBack(body, ruleset1);
        BackDoorNodeMutation.addChildToBack(body, ruleset2);

        CssRootNode root = new CssRootNode(body);
        CssTree tree = new CssTree(null, root);
        assertThat(tree.getRoot().getBody())
                .hasToString("[[foo]{[padding:[5px]]}, [foo]{[display:[2px]]}]");

        MergeAdjacentRulesetNodesWithSameSelector pass =
                new MergeAdjacentRulesetNodesWithSameSelector(tree, true);
        pass.runPass();
        // skip merging rules with display -> we expect output == input
        assertThat(tree.getRoot().getBody())
                .hasToString("[[foo]{[padding:[5px]]}, [foo]{[display:[2px]]}]");
    }

    private String lines(String... lines) {
        return Joiner.on("\n").join(lines);
    }
}
