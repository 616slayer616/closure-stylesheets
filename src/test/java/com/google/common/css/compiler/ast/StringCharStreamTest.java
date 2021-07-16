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

package com.google.common.css.compiler.ast;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertWithMessage;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for a more efficient implementation of the CharStream which only expects to operate on
 * {@code String} object.
 *
 * @author dgajda@google.com (Damian Gajda)
 */
class StringCharStreamTest {

    @Test
    void testLocation() throws Exception {
        StringCharStream s = new StringCharStream(
                "01234\n" +
                        "6789\n" +
                        "bcd");

        assertWithMessage("column").that(s.getEndColumn()).isEqualTo(0);
        assertWithMessage("line").that(s.getEndLine()).isEqualTo(1);
        assertWithMessage("char index").that(s.getCharIndex()).isEqualTo(-1);

        readCharCheckLocation(s, '0', 1, 1, 0);
        readCharCheckLocation(s, '1', 1, 2, 1);
        readCharCheckLocation(s, '2', 1, 3, 2);
        readCharCheckLocation(s, '3', 1, 4, 3);
        readCharCheckLocation(s, '4', 1, 5, 4);
        readCharCheckLocation(s, '\n', 1, 6, 5);
        readCharCheckLocation(s, '6', 2, 1, 6);
        readCharCheckLocation(s, '7', 2, 2, 7);
        readCharCheckLocation(s, '8', 2, 3, 8);
        readCharCheckLocation(s, '9', 2, 4, 9);
        readCharCheckLocation(s, '\n', 2, 5, 10);
        readCharCheckLocation(s, 'b', 3, 1, 11);
        readCharCheckLocation(s, 'c', 3, 2, 12);
        readCharCheckLocation(s, 'd', 3, 3, 13);
    }

    @Test
    void testBackup() throws Exception {
        StringCharStream s = new StringCharStream(
                "01234\n" +
                        "6789\n" +
                        "bcd");
        readCharCheckLocation(s, '0', 1, 1, 0);
        readCharCheckLocation(s, '1', 1, 2, 1);
        readCharCheckLocation(s, '2', 1, 3, 2);

        s.backup(3);
        readCharCheckLocation(s, '0', 1, 1, 0);
        readCharCheckLocation(s, '1', 1, 2, 1);
        readCharCheckLocation(s, '2', 1, 3, 2);

        s.backup(2);
        readCharCheckLocation(s, '1', 1, 2, 1);
        readCharCheckLocation(s, '2', 1, 3, 2);

        readCharCheckLocation(s, '3', 1, 4, 3);
        readCharCheckLocation(s, '4', 1, 5, 4);
        readCharCheckLocation(s, '\n', 1, 6, 5);
        readCharCheckLocation(s, '6', 2, 1, 6);
        readCharCheckLocation(s, '7', 2, 2, 7);

        s.backup(4);
        readCharCheckLocation(s, '4', 1, 5, 4);
        readCharCheckLocation(s, '\n', 1, 6, 5);
        readCharCheckLocation(s, '6', 2, 1, 6);
        readCharCheckLocation(s, '7', 2, 2, 7);

        s.backup(3);
        readCharCheckLocation(s, '\n', 1, 6, 5);
        readCharCheckLocation(s, '6', 2, 1, 6);
        readCharCheckLocation(s, '7', 2, 2, 7);

        readCharCheckLocation(s, '8', 2, 3, 8);
        readCharCheckLocation(s, '9', 2, 4, 9);
        readCharCheckLocation(s, '\n', 2, 5, 10);
        readCharCheckLocation(s, 'b', 3, 1, 11);
        readCharCheckLocation(s, 'c', 3, 2, 12);
        readCharCheckLocation(s, 'd', 3, 3, 13);

        s.backup(2);
        readCharCheckLocation(s, 'c', 3, 2, 12);
        readCharCheckLocation(s, 'd', 3, 3, 13);
    }

    @Test
    void testConvertCharacterIndex() throws Exception {
        StringCharStream s = new StringCharStream(
                "01234\n" +
                        "6789\n" +
                        "bcd");
        checkCharacterIndex(s, 1, 1, 0);
        checkCharacterIndex(s, 1, 2, 1);
        checkCharacterIndex(s, 1, 3, 2);
        checkCharacterIndex(s, 1, 4, 3);
        checkCharacterIndex(s, 1, 5, 4);
        checkCharacterIndex(s, 1, 6, 5);
        checkCharacterIndex(s, 2, 1, 6);
        checkCharacterIndex(s, 2, 2, 7);
        checkCharacterIndex(s, 2, 3, 8);
        checkCharacterIndex(s, 2, 4, 9);
        checkCharacterIndex(s, 2, 5, 10);
        checkCharacterIndex(s, 3, 1, 11);
        checkCharacterIndex(s, 3, 2, 12);
        checkCharacterIndex(s, 3, 3, 13);
    }

    @Test
    void testGetImageAndGetSuffix() throws Exception {
        StringCharStream s = new StringCharStream(
                "01234\n" +
                        "6789\n" +
                        "bcd");
        readCharCheckLocation(s, '0', 1, 1, 0);
        readCharCheckLocation(s, '1', 1, 2, 1);

        beginTokenCheckLocation(s, '2', 1, 3, 2);
        readCharCheckLocation(s, '3', 1, 4, 3);
        readCharCheckLocation(s, '4', 1, 5, 4);
        readCharCheckLocation(s, '\n', 1, 6, 5);
        readCharCheckLocation(s, '6', 2, 1, 6);

        assertThat(s.getImage()).isEqualTo("234\n6");
        assertThat(new String(s.getSuffix(3))).isEqualTo("4\n6");
        assertThat(new String(s.getSuffix(4))).isEqualTo("34\n6");

        s.backup(2);
        readCharCheckLocation(s, '\n', 1, 6, 5);
        readCharCheckLocation(s, '6', 2, 1, 6);
        readCharCheckLocation(s, '7', 2, 2, 7);

        assertThat(s.getImage()).isEqualTo("234\n67");

        beginTokenCheckLocation(s, '8', 2, 3, 8);
        readCharCheckLocation(s, '9', 2, 4, 9);
        readCharCheckLocation(s, '\n', 2, 5, 10);

        assertThat(s.getImage()).isEqualTo("89\n");
        assertThat(new String(s.getSuffix(3))).isEqualTo("89\n");
        // Try to reach behind the token start!
        assertThat(new String(s.getSuffix(4))).isEqualTo("789\n");

        beginTokenCheckLocation(s, 'b', 3, 1, 11);
        readCharCheckLocation(s, 'c', 3, 2, 12);
        readCharCheckLocation(s, 'd', 3, 3, 13);

        assertThat(s.getImage()).isEqualTo("bcd");
        assertThat(new String(s.getSuffix(2))).isEqualTo("cd");
        assertThat(new String(s.getSuffix(4))).isEqualTo("\nbcd");

        try {
            s.readChar();
            Assert.fail();
        } catch (IOException e) {
            // Should thrown an exception, when reaching behind the end of the string.
        }

        // Token creation and suffix creation, should be unaffected by trying to
        // read EOF.
        assertThat(s.getImage()).isEqualTo("bcd");
        assertThat(new String(s.getSuffix(2))).isEqualTo("cd");
        assertThat(new String(s.getSuffix(4))).isEqualTo("\nbcd");
    }

    private void checkCharacterIndex(StringCharStream s,
                                     int line, int column, int charIndex) {
        assertWithMessage("char index")
                .that(s.convertToCharacterIndex(line, column))
                .isEqualTo(charIndex);
    }

    private void readCharCheckLocation(
            StringCharStream s, char c, int line, int column, int charIndex)
            throws IOException {
        assertWithMessage("char").that(s.readChar()).isEqualTo(c);
        checkLocation(s, line, column, charIndex);
    }

    private void beginTokenCheckLocation(
            StringCharStream s, char c, int line, int column, int charIndex)
            throws IOException {
        assertWithMessage("char").that(s.beginToken()).isEqualTo(c);
        checkLocation(s, line, column, charIndex);
        assertWithMessage("begin column").that(s.getBeginColumn()).isEqualTo(column);
        assertWithMessage("begin line").that(s.getBeginLine()).isEqualTo(line);
        assertWithMessage("char index").that(s.getCharIndex()).isEqualTo(charIndex);
        assertWithMessage("token start").that(s.getTokenStart()).isEqualTo(charIndex);
    }

    private void checkLocation(
            StringCharStream s, int line, int column, int charIndex) {
        assertWithMessage("column").that(s.getEndColumn()).isEqualTo(column);
        assertWithMessage("line").that(s.getEndLine()).isEqualTo(line);
        assertWithMessage("char index").that(s.getCharIndex()).isEqualTo(charIndex);
    }
}
