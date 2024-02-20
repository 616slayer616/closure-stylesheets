/*
 * Copyright 2011 Google Inc.
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

package com.google.common.css.compiler.commandline;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.css.IdentitySubstitutionMap;
import com.google.common.css.RecordingSubstitutionMap;
import com.google.common.css.SubstitutionMap;
import com.google.common.css.SubstitutionMapProvider;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RenamingTypeTest} is a unit test for {@link RenamingType}.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
class RenamingTypeTest {

    @Test
    void testNone() {
        SubstitutionMapProvider provider = RenamingType.NONE
                .getCssSubstitutionMapProvider();
        assertThat(provider).isNotNull();

        SubstitutionMap map = provider.get();
        assertThat(map).isNotNull();
        // This is sufficient to guarantee the contract of the renaming.
        assertThat(map).isInstanceOf(IdentitySubstitutionMap.class);
    }

    @Test
    void testDebug() {
        SubstitutionMapProvider provider = RenamingType.DEBUG
                .getCssSubstitutionMapProvider();
        assertThat(provider).isNotNull();

        SubstitutionMap map = provider.get();
        assertThat(map).isNotNull();

        assertThat(map.get("dialog")).isEqualTo("dialog_");
        assertThat(map.get("dialog-button")).isEqualTo("dialog_-button_");
        assertThat(map.get("button_")).isEqualTo("button__");

        testRenamingTypeThatWrapsASplittingSubstitutionMap(RenamingType.DEBUG);
    }


    @Test
    void testClosure() {
        SubstitutionMapProvider provider = RenamingType.CLOSURE
                .getCssSubstitutionMapProvider();
        assertThat(provider).isNotNull();

        SubstitutionMap map = provider.get();
        assertThat(map).isNotNull();

        assertThat(map.get("dialog")).isEqualTo("a");
        assertThat(map.get("settings")).isEqualTo("b");
        assertThat(map.get("dialog-button")).isEqualTo("a-c");
        assertThat(map.get("button")).isEqualTo("c");
        assertThat(map.get("goog-imageless-button-button-pos"))
                .as("A CSS class may include a part with the same name multiple times.")
                .isEqualTo("d-e-c-c-f");

        testRenamingTypeThatWrapsASplittingSubstitutionMap(RenamingType.CLOSURE);
    }

    @Test
    void testClosureWithInputRenamingMap() {
        SubstitutionMapProvider provider = RenamingType.CLOSURE.getCssSubstitutionMapProvider();
        RecordingSubstitutionMap map =
                new RecordingSubstitutionMap.Builder()
                        .withSubstitutionMap(provider.get())
                        .shouldRecordMappingForCodeGeneration(Predicates.alwaysTrue())
                        .build();

        ImmutableMap<String, String> inputRenamingMap =
                ImmutableMap.of("dialog", "e", "content", "b", "settings", "m", "unused", "T");
        map.initializeWithMappings(inputRenamingMap);

        assertThat(map.get("dialog")).isEqualTo("e");
        assertThat(map.get("settings")).isEqualTo("m");
        assertThat(map.get("dialog-button")).isEqualTo("e-a");
        assertThat(map.get("button")).isEqualTo("a");
        assertThat(map.get("title")).isEqualTo("c");
        assertThat(map.get("goog-imageless-button-button-pos-dialog"))
                .as("Should accept same part multiple times even with a input renaming map.")
                .isEqualTo("d-f-a-a-g-e");

        Map<String, String> expectedMappings =
                ImmutableMap.<String, String>builder()
                        .putAll(inputRenamingMap)
                        .put("button", "a")
                        .put("goog", "d")
                        .put("imageless", "f")
                        .put("pos", "g")
                        .put("title", "c")
                        .build();
        Map<String, String> observedMappings = map.getMappings();
        // "content" wasn't observed, but it should still be in the output
        assertThat(observedMappings).containsAllEntriesOf(expectedMappings);
    }

    private void testRenamingTypeThatWrapsASplittingSubstitutionMap(RenamingType
                                                                            renamingType) {
        SubstitutionMapProvider provider = renamingType
                .getCssSubstitutionMapProvider();
        RecordingSubstitutionMap map =
                new RecordingSubstitutionMap.Builder().withSubstitutionMap(provider.get()).build();

        map.get("dialog-content");
        map.get("dialog-title");

        Set<String> expectedMappings = ImmutableSet.of(
                "content",
                "dialog",
                "title"
        );
        Set<String> observedMappings = map.getMappings().keySet();
        assertThat(observedMappings)
                .as("There should be entries for both 'dialog' and 'content' in"
                        + "case someone does: "
                        + "goog.getCssName(goog.getCssName('dialog'), 'content')")
                .isEqualTo(expectedMappings);
    }
}
