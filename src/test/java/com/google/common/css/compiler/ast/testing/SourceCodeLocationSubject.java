/*
 * Copyright 2016 Google Inc.
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

package com.google.common.css.compiler.ast.testing;

import com.google.common.css.SourceCodeLocation;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;

import javax.annotation.CheckReturnValue;

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

/**
 * Truth subject for {@link SourceCodeLocation}.
 */
public class SourceCodeLocationSubject
        extends Subject<SourceCodeLocationSubject, SourceCodeLocation> {

    static final Subject.Factory<SourceCodeLocationSubject, SourceCodeLocation> LOCATION =
            SourceCodeLocationSubject::new;

    private final SourceCodeLocation actual;

    @CheckReturnValue
    public static SourceCodeLocationSubject assertThat(SourceCodeLocation target) {
        return assertAbout(LOCATION).that(target);
    }

    public SourceCodeLocationSubject(FailureMetadata failureMetadata, SourceCodeLocation subject) {
        super(failureMetadata, subject);
        actual = subject;
    }

    public void hasSpan(int beginLine, int beginIndex, int endLine, int endIndex) {
        check("").that(actual).isNotNull();
        if (!(beginLine == actual.getBeginLineNumber()
                && beginIndex == actual.getBeginIndexInLine()
                && endLine == actual.getEndLineNumber()
                && endIndex == actual.getEndIndexInLine())) {
            failWithoutActual(simpleFact(String.format(
                    "Location did not match <%s,%s -> %s,%s>, was <%s,%s -> %s,%s>",
                    beginLine,
                    beginIndex,
                    endLine,
                    endIndex,
                    actual.getBeginLineNumber(),
                    actual.getBeginIndexInLine(),
                    actual.getEndLineNumber(),
                    actual.getEndIndexInLine())));
        }
    }

    public void matches(String text) {
         check("").that(actual).isNotNull();
        String source =
                actual
                        .getSourceCode()
                        .getFileContents()
                        .substring(
                                actual.getBeginCharacterIndex(), actual.getEndCharacterIndex());
         check("").that(source).isEqualTo(text);
    }

    public void isUnknown() {
         check("").that(actual).isNotNull();
         check("").that(actual.isUnknown()).named("isUnknown").isTrue();
    }
}
