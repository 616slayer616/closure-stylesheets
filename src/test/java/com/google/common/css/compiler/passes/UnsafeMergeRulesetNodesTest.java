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
import com.google.common.css.compiler.ast.*;
import org.junit.jupiter.api.Test;

import static com.google.common.css.compiler.passes.UnsafeMergeRulesetNodes.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UnsafeMergeRulesetNodes}.
 */
class UnsafeMergeRulesetNodesTest {

    /**
     * Tests for {@link UnsafeMergeRulesetNodes#TO_STRING_COMPARATOR}.
     */
    @Test
    void testToStringComparator() {
        assertThat(TO_STRING_COMPARATOR.compare(
                new CssSelectorNode("a"), new CssSelectorNode("a"))).isZero();
        assertThat(TO_STRING_COMPARATOR.compare(
                new CssSelectorNode("a"), new CssSelectorNode("b"))).isNegative();
    }

    /**
     * Tests for {@link UnsafeMergeRulesetNodes#TO_STRING_ITERABLE_COMPARATOR}.
     */
    @Test
    void testToStringIterableComparator() {
        assertThat(TO_STRING_ITERABLE_COMPARATOR.compare(
                ImmutableList.of("a", "b"), ImmutableList.of("a", "b"))).isZero();
        assertThat(TO_STRING_ITERABLE_COMPARATOR.compare(
                ImmutableList.of("b", "a"), ImmutableList.of("a", "b"))).isPositive();
        assertThat(TO_STRING_ITERABLE_COMPARATOR.compare(
                ImmutableList.of("a"), ImmutableList.of("a", "b"))).isNegative();
    }

    /**
     * Tests for {@link UnsafeMergeRulesetNodes#DECLARATION_COMPARATOR}.
     */
    @Test
    void testDeclarationComparator() {
        CssPropertyNode padding = new CssPropertyNode("padding");
        CssPropertyNode paddingLeft = new CssPropertyNode("padding-left");
        CssPropertyNode margin = new CssPropertyNode("margin");
        CssPropertyNode marginLeft = new CssPropertyNode("margin-left");

        CssValueNode px1 = new CssNumericNode("1", "px");
        CssValueNode px2 = new CssNumericNode("2", "px");

        CssPropertyValueNode v1 = new CssPropertyValueNode(ImmutableList.of(px1));
        CssPropertyValueNode v1s =
                new CssPropertyValueNode(ImmutableList.of(px1, px1, px1, px1));
        CssPropertyValueNode v2s =
                new CssPropertyValueNode(ImmutableList.of(px2, px2, px2, px2));

        CssDeclarationNode padding1 =
                new CssDeclarationNode(padding.deepCopy(), v1s.deepCopy());
        CssDeclarationNode padding2 =
                new CssDeclarationNode(padding.deepCopy(), v2s.deepCopy());
        CssDeclarationNode paddingLeft1 =
                new CssDeclarationNode(paddingLeft.deepCopy(), v1.deepCopy());

        CssDeclarationNode margin1 =
                new CssDeclarationNode(margin.deepCopy(), v1s.deepCopy());
        CssDeclarationNode marginLeft1 =
                new CssDeclarationNode(marginLeft.deepCopy(), v1.deepCopy());

        assertThat(DECLARATION_COMPARATOR.compare(margin1, margin1.deepCopy())).isZero();
        assertThat(DECLARATION_COMPARATOR.compare(margin1, marginLeft1)).isNegative();
        assertThat(DECLARATION_COMPARATOR.compare(margin1, paddingLeft1)).isNegative();
        assertThat(DECLARATION_COMPARATOR.compare(padding1, padding2)).isNegative();
        assertThat(DECLARATION_COMPARATOR.compare(marginLeft1, padding2)).isNegative();
        assertThat(DECLARATION_COMPARATOR.compare(paddingLeft1, padding2)).isPositive();
    }
}
