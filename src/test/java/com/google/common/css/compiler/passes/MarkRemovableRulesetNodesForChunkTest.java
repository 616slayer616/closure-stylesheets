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
import com.google.common.css.compiler.ast.CssRulesetNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTree.RulesetNodesToRemove;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.passes.testing.PassesTestBase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MarkRemovableRulesetNodesForChunk}.
 */
class MarkRemovableRulesetNodesForChunkTest extends PassesTestBase {

    @Test
    void testAllOneChunk() {
        collectRemovableRulesetNodes(
                linesToString(
                        ".CSS_RULE {",
                        "  border: 2px;",
                        "}",
                        ".CSS_RULE {",
                        "  border: 3px;",
                        "}"),
                ImmutableList.of("C1", "C1"));
        RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();
        assertThat(rules.getRulesetNodes()).hasSize(1);
        CssRulesetNode rule = rules.getRulesetNodes().iterator().next();
        checkRuleset("[[.CSS_RULE]{[border:[[2px]];]}]", rule);
    }

    @Test
    void testAllOneChunkButSkipping() {
        collectRemovableRulesetNodes(
                linesToString(
                        ".CSS_RULE {",
                        "  display: inline-box;",
                        "}",
                        ".CSS_RULE {",
                        "  display: -moz-inline-box;",
                        "}"),
                ImmutableList.of("C1", "C1"));
        RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();
        assertThat(rules.getRulesetNodes()).isEmpty();
    }

    @Test
    void testDiffChunkHit() {
        collectRemovableRulesetNodes(
                linesToString(
                        ".CSS_RULE {",
                        "  border: 2px;",
                        "}",
                        ".CSS_RULE {",
                        "  border: 3px;",
                        "}"),
                ImmutableList.of("C1", "C2"));
        RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();
        assertThat(rules.getRulesetNodes()).isEmpty();
    }

    @Test
    void testSameChunkHit() {
        collectRemovableRulesetNodes(
                linesToString(
                        ".CSS_RULE {",
                        "  border: 1px;",
                        "}",
                        ".CSS_RULE {",
                        "  border: 2px;",
                        "}",
                        ".CSS_RULE {",
                        "  border: 3px;",
                        "}"),
                ImmutableList.of("C1", "C1", "C2"));
        RulesetNodesToRemove rules = tree.getRulesetNodesToRemove();
        assertThat(rules.getRulesetNodes()).hasSize(1);
        CssRulesetNode rule = rules.getRulesetNodes().iterator().next();
        checkRuleset("[[.CSS_RULE]{[border:[[1px]];]}]", rule);
    }

    private void collectRemovableRulesetNodes(
            String source, List<String> chunks) {
        parseAndBuildTree(source);
        mapChunks(chunks);
        new MarkRemovableRulesetNodesForChunk<>("C1", tree, true).runPass();
    }

    private void mapChunks(final List<String> chunks) {
        tree.getVisitController().startVisit(
                new DefaultTreeVisitor() {
                    private int count = 0;

                    @Override
                    public boolean enterSelector(CssSelectorNode selector) {
                        selector.setChunk(chunks.get(count++));
                        return true;
                    }
                });
    }
}
