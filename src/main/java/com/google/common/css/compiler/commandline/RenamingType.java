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

import com.google.common.css.*;

/**
 * {@link RenamingType} is an enumeration of the possible values for the
 * {@code --rename} option in {@link ClosureCommandLineCompiler}. Each
 * corresponds to an implementation of {@link SubstitutionMapProvider} that
 * creates a {@link SubstitutionMap} to reflect the type of renaming.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
enum RenamingType {
    /**
     * No renaming is done.
     */
    NONE(IdentitySubstitutionMap::new),

    /**
     * A trailing underscore is added to each part of a CSS class.
     */
    DEBUG(() -> {
        // This wraps the SimpleSubstitutionMap in a SplittingSubstitutionMap so
        // that can be used with goog.getCssName().
        return new SplittingSubstitutionMap(new SimpleSubstitutionMap());
    }),


    /**
     * Each chunk of a CSS class as delimited by '-' is renamed using the
     * shortest available name.
     */
    CLOSURE(() -> {
        return new SplittingSubstitutionMap(new MinimalSubstitutionMap());
    }),
    ;

    private final SubstitutionMapProvider provider;

    private RenamingType(SubstitutionMapProvider provider) {
        this.provider = provider;
    }

    public SubstitutionMapProvider getCssSubstitutionMapProvider() {
        return provider;
    }
}
