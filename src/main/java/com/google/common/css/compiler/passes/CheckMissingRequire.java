/*
 * Copyright 2013 Google Inc.
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

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.MULTILINE;

/**
 * A compiler pass that checks for missing {@code @require} lines for def constant references
 * and mixins. This pass is used in conjunction with CollectProvideNamespaces, which provides
 * namespaces for constant definitions and mixins.
 * Example for def references:
 * file foo/gss/button.gss provides namespace {@code @provide 'foo.gss.button';} and has
 * the def: {@code @def FONT_SIZE 10px;}.
 * File foo/gss/item.gss references the above def as follows:
 * {@code @def ITEM_FONT_SIZE FONT_SIZE;}
 * This pass enforces that file foo/gss/item.gss contains {@code @require 'foo.gss.button';}
 */
public final class CheckMissingRequire extends DefaultTreeVisitor implements CssCompilerPass {
    private static final Logger logger = Logger.getLogger(CheckMissingRequire.class.getName());

    private static final Pattern OVERRIDE_SELECTOR_REGEX = Pattern.compile(
            "^\\s*(?:/\\*?)?\\*\\s+@overrideSelector\\s+\\{(.*)}\\s*(?:\\*/)?$", MULTILINE);

    private static final Pattern OVERRIDE_DEF_REGEX = Pattern.compile(
            "^\\s*(?:/\\*?)?\\*\\s+@overrideDef\\s+\\{(.*)}\\s*(?:\\*/)?$", MULTILINE);

    private final VisitController visitController;
    private final ErrorManager errorManager;

    // Key: filename; Value: provide namespace
    private final Map<String, String> filenameProvideMap;
    // Key: filename; Value: require namespace
    private final ListMultimap<String, String> filenameRequireMap;

    // Multiple namespaces can contain the same defs due to duplicate defs (or mods).
    // Key: def name; Value: provide namespace
    private final ListMultimap<String, String> defProvideMap;
    // Key: defmixin name; Value: provide namespace
    private final ListMultimap<String, String> defmixinProvideMap;

    public CheckMissingRequire(VisitController visitController,
                               ErrorManager errorManager,
                               Map<String, String> filenameProvideMap,
                               ListMultimap<String, String> filenameRequireMap,
                               ListMultimap<String, String> defProvideMap,
                               ListMultimap<String, String> defmixinProvideMap) {
        this.visitController = visitController;
        this.errorManager = errorManager;
        this.filenameProvideMap = filenameProvideMap;
        this.filenameRequireMap = filenameRequireMap;
        this.defProvideMap = defProvideMap;
        this.defmixinProvideMap = defmixinProvideMap;
    }

    @Override
    public boolean enterValueNode(CssValueNode node) {
        if (node instanceof CssConstantReferenceNode) {
            CssConstantReferenceNode reference = (CssConstantReferenceNode) node;
            String filename = reference.getSourceCodeLocation().getSourceCode().getFileName();
            List<String> provides = defProvideMap.get(reference.getValue());
            if (hasMissingRequire(provides, filenameProvideMap.get(filename),
                    filenameRequireMap.get(filename))) {
                StringBuilder error = new StringBuilder("Missing @require for constant " +
                        reference.getValue() + ". Please @require namespace from:\n");
                for (String namespace : defProvideMap.get(reference.getValue())) {
                    error.append("\t");
                    error.append(namespace);
                    error.append("\n");
                }
                errorManager.report(new GssError(error.toString(), reference.getSourceCodeLocation()));
            }
        }
        return true;
    }

    @Override
    public boolean enterMixin(CssMixinNode node) {
        String filename = node.getSourceCodeLocation().getSourceCode().getFileName();
        List<String> provides = defmixinProvideMap.get(node.getDefinitionName());
        if (hasMissingRequire(provides, filenameProvideMap.get(filename),
                filenameRequireMap.get(filename))) {
            StringBuilder error = new StringBuilder("Missing @require for mixin " +
                    node.getDefinitionName() + ". Please @require namespace from:\n");
            for (String namespace : defmixinProvideMap.get(node.getDefinitionName())) {
                error.append("\t");
                error.append(namespace);
                error.append("\n");
            }
            errorManager.report(new GssError(error.toString(), node.getSourceCodeLocation()));
        }
        return true;
    }

    private boolean hasMissingRequire(List<String> provides, String currentNamespace,
                                      List<String> requires) {
        // Either the namespace should be provided in this very file or it should be @require'd here.
        Set<String> defNamespaceSet = Sets.newHashSet(provides);
        Set<String> requireNamespaceSet = Sets.newHashSet(requires);
        requireNamespaceSet.retainAll(defNamespaceSet);
        return requireNamespaceSet.isEmpty() && !defNamespaceSet.contains(currentNamespace);
    }

    /*
     * Check whether @overrideSelector namespaces are @require'd.
     */
    @Override
    public boolean enterSelector(CssSelectorNode node) {
        String filename = node.getSourceCodeLocation().getSourceCode().getFileName();
        for (CssRefinerNode refiner : node.getRefiners().getChildren()) {
            for (CssCommentNode comment : refiner.getComments()) {
                Matcher matcher = OVERRIDE_SELECTOR_REGEX.matcher(comment.getValue());
                if (match(matcher, filename, "overrideSelector", node.getSourceCodeLocation()))
                    return true;
            }
        }
        return true;
    }

    /*
     * Check whether @overrideDef namespaces are @require'd.
     */
    @Override
    public boolean enterDefinition(CssDefinitionNode node) {
        String filename = node.getSourceCodeLocation().getSourceCode().getFileName();
        for (CssCommentNode comment : node.getComments()) {
            Matcher matcher = OVERRIDE_DEF_REGEX.matcher(comment.getValue());
            if (match(matcher, filename, "overrideDef", node.getSourceCodeLocation()))
                return true;
        }
        return true;
    }

    private boolean match(Matcher matcher, String filename, String override, SourceCodeLocation node) {
        if (matcher.find()) {
            String overrideNamespace = matcher.group(1);
            List<String> requires = filenameRequireMap.get(filename);
            Set<String> requireNamespaceSet = Sets.newHashSet(requires);
            if (!requireNamespaceSet.contains(overrideNamespace)) {
                String error = "Missing @require for @" + override + " {"
                        + overrideNamespace + "}. Please @require this namespace in file: "
                        + filename + ".\n";
                errorManager.report(new GssError(error, node));
                return true;
            }
        }
        return false;
    }

    @Override
    public void runPass() {
        visitController.startVisit(this);
    }
}
