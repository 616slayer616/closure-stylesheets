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

package com.google.common.css;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for the delegating CSS class substitution map which handles compound class names.
 *
 * @author dgajda@google.com (Damian Gajda)
 */
class SplittingSubstitutionMapTest {

    @Test
    void testGet() {
        SubstitutionMap map = new SplittingSubstitutionMap(
                new SimpleSubstitutionMap());
        assertThat(map.get("a")).isEqualTo("a_");
        assertThat(map.get("a-b")).isEqualTo("a_-b_");
        assertThat(map.get("a-b-c")).isEqualTo("a_-b_-c_");
        assertThat(map.get("a-b-c-d")).isEqualTo("a_-b_-c_-d_");
    }

    @Test
    void testSameObjectReturnedIfNoDash() {
        // Manually force a non-interned string so that we can prove we got back
        // the same one we meant. If we just used a String literal, it would be
        // less convincing.
        String input = "abc";
        SubstitutionMap map = new SplittingSubstitutionMap(
                new PassThroughSubstitutionMap());
        assertThat(map.get(input)).isSameAs(input);
    }

    private static class PassThroughSubstitutionMap implements SubstitutionMap {
        @Override
        public String get(String key) {
            return key;
        }
    }
}
