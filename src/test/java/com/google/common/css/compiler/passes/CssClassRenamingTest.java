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

import com.google.common.css.SubstitutionMap;
import com.google.common.css.compiler.ast.*;
import com.google.common.css.compiler.passes.testing.AstPrinter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests the {@link CssClassRenaming} compiler pass.
 *
 * @author oana@google.com (Oana Florescu)
 */
@SuppressWarnings("java:S2699")
class CssClassRenamingTest {

    @Test
    void testRunPass() {
        MutatingVisitController visitController = mock(MutatingVisitController.class);

        CssClassRenaming pass = new CssClassRenaming(visitController, null, null);
        visitController.startVisit(pass);
        pass.runPass();
    }

    @Test
    void testNoSubstitutionWithNullMap() {
        CssClassSelectorNode node = new CssClassSelectorNode("FOO", null);
        CssClassRenaming pass = new CssClassRenaming(null, null, null);
        pass.enterClassSelector(node);
    }

    @Test
    void testNoClassSubstitutionWhenClassNotFoundInMap() {
        CssClassSelectorNode refinerNode = new CssClassSelectorNode("FOO", null);
        SubstitutionMap cssClassRenamingMap = mock(SubstitutionMap.class);

        CssClassRenaming pass
                = new CssClassRenaming(null, cssClassRenamingMap, null);
        pass.enterClassSelector(refinerNode);

        verify(cssClassRenamingMap).get("FOO");
    }

    @Test
    void testNoIdSubstitutionWhenIdNotFoundInMap() {
        CssIdSelectorNode refinerNode = new CssIdSelectorNode("ID", null);
        SubstitutionMap idRenamingMap = mock(SubstitutionMap.class);

        CssClassRenaming pass = new CssClassRenaming(null, null, idRenamingMap);
        pass.enterIdSelector(refinerNode);

        verify(idRenamingMap).get("ID");
    }

    @Test
    void testEnterClassRefiner() {
        CssClassSelectorNode refinerNode = new CssClassSelectorNode("CSS_FOO",
                null);
        CssRefinerListNode refiners = new CssRefinerListNode();
        BackDoorNodeMutation.addChildToBack(refiners, refinerNode);

        CssRulesetNode ruleset = new CssRulesetNode();
        CssSelectorNode sel = new CssSelectorNode("", null);
        sel.setRefiners(refiners);
        ruleset.addSelector(sel);

        CssBlockNode body = new CssBlockNode(false);
        BackDoorNodeMutation.addChildToBack(body, ruleset);

        CssRootNode root = new CssRootNode(body);
        CssTree tree = new CssTree(null, root);

        SubstitutionMap classMap = new SubstitutionMap() {
            /** {@inheritDoc} */
            @Override
            public String get(String key) {
                return key.startsWith("CSS_") ? key + '_' : key;
            }
        };
        CssClassRenaming pass = new CssClassRenaming(
                tree.getMutatingVisitController(), classMap, null);
        pass.runPass();
        assertThat(AstPrinter.print(tree)).isEqualTo("[[.CSS_FOO_]{[]}]");
    }

    @Test
    void testEnterIdRefiner() {
        CssIdSelectorNode refinerNode = new CssIdSelectorNode("ID_FOO", null);
        CssRefinerListNode refiners = new CssRefinerListNode();
        BackDoorNodeMutation.addChildToBack(refiners, refinerNode);

        CssRulesetNode ruleset = new CssRulesetNode();
        CssSelectorNode sel = new CssSelectorNode("", null);
        sel.setRefiners(refiners);
        ruleset.addSelector(sel);

        CssBlockNode body = new CssBlockNode(false);
        BackDoorNodeMutation.addChildToBack(body, ruleset);

        CssRootNode root = new CssRootNode(body);
        CssTree tree = new CssTree(null, root);

        SubstitutionMap idMap = new SubstitutionMap() {
            /** {@inheritDoc} */
            @Override
            public String get(String key) {
                return key.startsWith("ID_") ? key + '_' : key;
            }
        };
        CssClassRenaming pass
                = new CssClassRenaming(tree.getMutatingVisitController(), null, idMap);
        pass.runPass();
        assertThat(AstPrinter.print(tree)).isEqualTo("[[#ID_FOO_]{[]}]");
    }
}
