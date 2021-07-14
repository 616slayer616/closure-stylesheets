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

import com.google.common.collect.ImmutableList;
import com.google.common.css.SubstitutionMap;
import com.google.common.css.compiler.ast.*;

/**
 * Compiler pass that does CSS class renaming given a renaming map.
 *
 * @author oana@google.com (Oana Florescu)
 * @author fbenz@google.com (Florian Benz)
 */
public class CssClassRenaming extends DefaultTreeVisitor
        implements CssCompilerPass {

    private final MutatingVisitController visitController;
    private final SubstitutionMap cssClassRenamingMap;
    private final SubstitutionMap elementIdMap;

    public CssClassRenaming(MutatingVisitController visitController,
                            SubstitutionMap cssClassRenamingMap, SubstitutionMap elementIdMap) {
        this.visitController = visitController;
        this.cssClassRenamingMap = cssClassRenamingMap;
        this.elementIdMap = elementIdMap;
    }

    @Override
    public boolean enterClassSelector(CssClassSelectorNode node) {
        if (cssClassRenamingMap == null) {
            return true;
        }
        String substitution = cssClassRenamingMap.get(node.getRefinerName());
        if (substitution == null) {
            return true;
        }
        CssClassSelectorNode classSelector =
                new CssClassSelectorNode(substitution, node.getSourceCodeLocation());
        visitController.replaceCurrentBlockChildWith(
                ImmutableList.of(classSelector), false /* visitTheReplacementNodes */);
        return true;
    }

    @Override
    public boolean enterIdSelector(CssIdSelectorNode node) {
        if (elementIdMap == null) {
            return true;
        }
        String substitution = elementIdMap.get(node.getRefinerName());
        if (substitution == null) {
            return true;
        }
        CssIdSelectorNode idSelector =
                new CssIdSelectorNode(substitution, node.getSourceCodeLocation());
        visitController.replaceCurrentBlockChildWith(
                ImmutableList.of(idSelector), false /* visitTheReplacementNodes */);
        return true;
    }

    @Override
    public void runPass() {
        visitController.startVisit(this);
    }
}
