/*
 * Copyright 2006 Google Inc.
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

import java.util.Map;

/**
 * An interface for a one-to-one string mapping function.
 */
public interface SubstitutionMap {

    /**
     * Gets the string that should be substituted for {@code key}. The same
     * value must be consistently returned for any particular {@code key}, and
     * the returned value must not be returned for any other {@code key} value.
     *
     * @param key the text to be replaced (never null)
     * @return the value to substitute for {@code key}
     */
    String get(String key);


    /**
     * A substitution map that can be reconsitituted from saved mappings.
     *
     * <p>This allows re-using a substitution map across multiple compile
     * steps, and allows stability when incrementally recompiling part of
     * a project.
     *
     * <p>Compilation starts with no rename map on disk, in which case
     * no call to this method is necessary.
     *
     * <p>After the first compilation,
     * RecordingSubstitutionMap#getRenameMap may be used with
     * an {@link OutputRenamingMapFormat} to serialize the renaming map
     * in a form that can be used with Closure Library code or Closure
     * Templates.
     *
     * <p>Before a re-compile, {@link OutputRenamingMapFormat#readRenamingMap}
     * can be used to generate a set of initial mappings that can be
     * passed to {@link Initializable#initializeWithMappings} to prepare
     * a structurally equivalent substitution map to produce IDs that do
     * not conflict with those generated by the one used for the previous
     * compile.
     *
     * <p>Subsequent com
     */
    interface Initializable extends SubstitutionMap {
        void initializeWithMappings(Map<? extends String, ? extends String> initialMappings);
    }
}
