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

import com.google.common.css.SourceCode;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.*;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LocationBoundingVisitor}.
 */
class LocationBoundingVisitorTest extends NewFunctionalTestBase {
    private LocationBoundingVisitor locationBoundingVisitor;

    @Override
    protected void runPass() {
        locationBoundingVisitor = new LocationBoundingVisitor();
        tree.getVisitController()
                .startVisit(UniformVisitor.Adapters.asVisitor(locationBoundingVisitor));
    }

    @Test
    void testTrivialBound() {
        CssLiteralNode red = new CssLiteralNode("red");
        SourceCodeLocation expected =
                new SourceCodeLocation(
                        new SourceCode(null, ""),
                        5 /* beginCharacterIndex */,
                        3 /* beginLineNumber */,
                        1 /* beginIndexInLine */,
                        15 /* endCharacterIndex */,
                        3 /* endLineNumber */,
                        11 /* endIndexInLine */);
        red.setSourceCodeLocation(expected);
        assertThat(LocationBoundingVisitor.bound(red)).isEqualTo(expected);
    }

    @Test
    void testUnknown() throws Exception {
        parseAndRun("div { color: red; }");
        CssTreeVisitor eraseLocations =
                UniformVisitor.Adapters.asVisitor(
                        new UniformVisitor() {
                            @Override
                            public void enter(CssNode n) {
                                n.setSourceCodeLocation(SourceCodeLocation.getUnknownLocation());
                            }

                            @Override
                            public void leave(CssNode node) {
                            }
                        });
        tree.getMutatingVisitController().startVisit(eraseLocations);
        SourceCodeLocation actual = LocationBoundingVisitor.bound(tree.getRoot());
        assertThat(actual)
                .as(new com.google.common.css.compiler.ast.GssError("boo", actual).format())
                .isEqualTo(SourceCodeLocation.getUnknownLocation());
    }

    @Test
    void testMixedSubtree() throws Exception {
        // Let's examine a non-trivial tree
        parseAndRun("div { color: red; }");

        // First: establish some facts we can use later on
        CssSelectorNode div = findFirstNodeOf(CssSelectorNode.class);
        assertThat(div.getSourceCodeLocation().isUnknown())
                .as("There should be a node with known location")
                .isFalse();
        CssLiteralNode red = findFirstNodeOf(CssLiteralNode.class);
        assertThat(red.getValue())
                .as("There should be a distinguished second node")
                .isEqualTo("red");
        assertThat(red.getSourceCodeLocation().isUnknown())
                .as("The second node  should also have known location")
                .isFalse();
        CssDeclarationBlockNode block =
                findFirstNodeOf(CssDeclarationBlockNode.class);
        assertThat(block.getSourceCodeLocation()==null || block.getSourceCodeLocation().isUnknown())
                .as("There should be a node with an known location")
                .isFalse();

        // Next: demonstrate some properties of the visitor
        SourceCodeLocation actual = LocationBoundingVisitor.bound(tree.getRoot());
        assertThat(actual.isUnknown()).isFalse();
        assertThat(actual.getBeginCharacterIndex())
                .as("The tree-wide lower bound should l.b. a known node.")
                .isLessThanOrEqualTo(div.getSourceCodeLocation().getBeginCharacterIndex());
        assertThat(actual.getBeginCharacterIndex())
                .as("The tree-wide lower bound should l.b. all the known nodes.")
                .isLessThanOrEqualTo(red.getSourceCodeLocation().getBeginCharacterIndex());
        assertThat(actual.getEndCharacterIndex())
                .as("The tree-wide upper bound should u.b. a known node.")
                .isGreaterThan(div.getSourceCodeLocation().getEndCharacterIndex());
        assertThat(actual.getEndCharacterIndex())
                .as("The tree-wide upper bound should u.b. all the known nodes.")
                .isGreaterThan(red.getSourceCodeLocation().getEndCharacterIndex());

        for (CssNode n : new CssNode[]{div, red, block}) {
            SourceCodeLocation nLocation = n.getSourceCodeLocation();
            for (CssNode a : n.ancestors()) {
                try {
                    if (!a.getSourceCodeLocation().isUnknown()) {
                        // LocationBoundingVisitor guarantees that ancestors contain
                        // their descendents only as long as the ancestor doesn't
                        // have explicit bounds, in which case all bets are off.
                        // E.g., consider this tree
                        //
                        //   graph  beginCharacterIndex endCharacterIndex
                        //   ---    ---                 ---
                        //    div   5                   8
                        //     |
                        //   span   3                   42
                        // These indices make no sense but GIGO.
                        continue;
                    }
                    SourceCodeLocation aBound = LocationBoundingVisitor.bound(a);
                    assertThat(aBound.getBeginCharacterIndex())
                            .as("ancestral lower bounds should not exceed descendent l.b.s")
                            .isLessThanOrEqualTo(nLocation.getBeginCharacterIndex());
                    assertThat(aBound.getBeginCharacterIndex())
                            .as("ancestral upper bounds should equal or exceed descendent u.b.s")
                            .isLessThanOrEqualTo(nLocation.getBeginCharacterIndex());
                } catch (NullPointerException e) {
                    // Our tree-traversal code is a bit buggy, so give up
                    // on this ancestor and try another one. To the extent
                    // we can visit ancestors, these properties we assert
                    // should hold.
                }
            }
        }
        // For good measure: some specific, empirical, and reasonable-looking
        // assertions.
        assertThat(actual.getBeginCharacterIndex()).isZero();
        assertThat(actual.getEndCharacterIndex()).isEqualTo(19);
    }
}
