/*
 * Copyright 2008 Google Inc.
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

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for MinimalSubstitutionMap.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
class MinimalSubstitutionMapTest {

    private static final char[] START_CHARS = new char[]{'a'};

    private static final char[] CHARS = new char[]{'1', '2'};

    private MinimalSubstitutionMap map;

    /**
     * @return a MinimalSubstitutionMap that uses fewer characters for CSS class
     * names
     */
    private static MinimalSubstitutionMap createTestMap() {
        return new MinimalSubstitutionMap(START_CHARS, CHARS);
    }

    /**
     * Tests basic get() functionality, and that get() returns the same value when
     * applied to the same key.
     */
    @Test
    void testGet() {
        map = createTestMap();
        assertThat(map.get("foo")).isEqualTo("a");

        // Note that the order the secondary characters appear in the generated CSS
        // class names does not match the order they appear in the CHARS array. This
        // is acceptable; the only important thing is that the names are unique,
        // which is confirmed by test_toShortString().
        assertThat(map.get("bar")).isEqualTo("a2");
        assertThat(map.get("baz")).isEqualTo("a1");

        assertThat(map.get("foo")).isEqualTo("a");
    }

    /**
     * Tests that the get() function correctly omits values from the blacklist.
     */
    @Test
    void testGetWithBlacklist() {
        map = new MinimalSubstitutionMap(START_CHARS, CHARS, ImmutableSet.of("a"));

        // We skipped over "a".  See testGet().
        assertThat(map.get("foo")).isEqualTo("a2");

        // Move on to a new value, and then go back to "foo" to prove repeatability
        assertThat(map.get("bar")).isEqualTo("a1");
        assertThat(map.get("foo")).isEqualTo("a2");
    }

    /**
     * Tests toShortString() by enumerating all values of toShortString(0) to
     * toShortString(2^8-1) and verifying that each value is unique. Also ensures
     * that there are only 2 strings with 2 chars, 4 strings with 3 chars,
     * 8 strings with 4 chars, etc.
     */
    @Test
    void testToShortString() {
        map = createTestMap();

        List<String> classes = new ArrayList<>();
        int n = 0;

        BigInteger NUM_CHARS = BigInteger.valueOf(CHARS.length);
        int MAX_POWER = 8;

        for (int power = 0; power <= MAX_POWER; ++power) {
            int stringLength = power + 1;
            int numberOfStringsWithThisLength = NUM_CHARS.pow(power).intValue();
            for (int i = 0; i < numberOfStringsWithThisLength; ++i) {
                String renamedClass = map.toShortString(n);

                // Use blaze test --test_output=all to see this
                System.out.println("RENAMED CLASS: " + renamedClass);

                assertThat(classes)
                        .withFailMessage("Already contains a class named: " + renamedClass)
                        .doesNotContain(renamedClass);
                assertThat(renamedClass)
                        .withFailMessage("Class name did not match expected length")
                        .hasSize(stringLength);
                classes.add(renamedClass);
                ++n;
            }
            assertThat(classes)
                    .withFailMessage("Does not contain all possible CSS class names of length " + stringLength)
                    .hasSize(NUM_CHARS.pow(power + 1).intValue() - 1);

        }

        assertThat(classes)
                .withFailMessage("Does not contain all possible CSS class names of length " + (MAX_POWER + 1))
                .hasSize(NUM_CHARS.pow(MAX_POWER + 1).intValue() - 1);
    }
}
