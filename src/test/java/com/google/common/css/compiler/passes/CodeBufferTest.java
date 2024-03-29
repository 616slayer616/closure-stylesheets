/*
 * Copyright 2015 Google Inc.
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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test {@link CodeBuffer} for correct behaviors when writing buffer and update lineIndex and
 * charIndex.
 *
 * @author steveyang@google.com (Chenyun Yang)
 */
class CodeBufferTest {
    @Test
    void testInitialSetup() {
        CodeBuffer buffer = new CodeBuffer();
        assertThat(buffer.getNextCharIndex()).isZero();
        assertThat(buffer.getNextLineIndex()).isZero();
        assertThat(buffer.getLastCharIndex()).isEqualTo(-1);
        assertThat(buffer.getLastLineIndex()).isZero();
    }

    @Test
    void testReset() {
        CodeBuffer buffer = new CodeBuffer();
        buffer.append("foo");
        buffer.reset();
        assertThat(buffer.getNextCharIndex()).isZero();
        assertThat(buffer.getNextLineIndex()).isZero();
        assertThat(buffer.getLastCharIndex()).isEqualTo(-1);
        assertThat(buffer.getLastLineIndex()).isZero();
    }

    @Test
    void testAppendNull() {
        CodeBuffer buffer = new CodeBuffer();
        buffer.append(null);
        assertThat(buffer.getNextCharIndex()).isZero();
        assertThat(buffer.getNextLineIndex()).isZero();
        assertThat(buffer.getLastCharIndex()).isEqualTo(-1);
        assertThat(buffer.getLastLineIndex()).isZero();
    }

    @Test
    void testAppendChar() {
        CodeBuffer buffer = new CodeBuffer();
        buffer.append('c');
        assertThat(buffer.getNextCharIndex()).isEqualTo(1);
        assertThat(buffer.getNextLineIndex()).isZero();
        assertThat(buffer.getLastCharIndex()).isZero();
        assertThat(buffer.getLastLineIndex()).isZero();
    }

    @Test
    void testAppendStr() {
        CodeBuffer buffer = new CodeBuffer();
        buffer.append("foo");
        assertThat(buffer.getNextCharIndex()).isEqualTo(3);
        assertThat(buffer.getNextLineIndex()).isZero();
        assertThat(buffer.getLastCharIndex()).isEqualTo(2);
        assertThat(buffer.getLastLineIndex()).isZero();
    }

    @Test
    void testAppendStrIncludeNewLine() {
        CodeBuffer buffer;

        buffer = new CodeBuffer();
        buffer.append("foo\nbarrr\n");
        assertThat(buffer.getNextCharIndex()).isZero();
        assertThat(buffer.getNextLineIndex()).isEqualTo(2);
        assertThat(buffer.getLastCharIndex()).isEqualTo(5);
        assertThat(buffer.getLastLineIndex()).isEqualTo(1);

        buffer = new CodeBuffer();
        buffer.append("foo\nbarrr");
        assertThat(buffer.getNextCharIndex()).isEqualTo(5);
        assertThat(buffer.getNextLineIndex()).isEqualTo(1);
        assertThat(buffer.getLastCharIndex()).isEqualTo(4);
        assertThat(buffer.getLastLineIndex()).isEqualTo(1);
    }

    @Test
    void testAppendObject() {
        CodeBuffer buffer = new CodeBuffer();
        class TestObject {
            @Override
            public String toString() {
                return "foobar";
            }
        }
        buffer.append(new TestObject());
        assertThat(buffer.getNextCharIndex()).isEqualTo(6);
        assertThat(buffer.getNextLineIndex()).isZero();
        assertThat(buffer.getLastCharIndex()).isEqualTo(5);
        assertThat(buffer.getLastLineIndex()).isZero();
    }

    @Test
    void testAppendNewLineChar() {
        CodeBuffer buffer = new CodeBuffer();
        buffer.append("foo");
        buffer.append('\n');
        assertThat(buffer.getNextCharIndex()).isZero();
        assertThat(buffer.getNextLineIndex()).isEqualTo(1);
        assertThat(buffer.getLastCharIndex()).isEqualTo(3);
        assertThat(buffer.getLastLineIndex()).isZero();
        buffer.append("bar");
        assertThat(buffer.getNextCharIndex()).isEqualTo(3);
        assertThat(buffer.getNextLineIndex()).isEqualTo(1);
        assertThat(buffer.getLastCharIndex()).isEqualTo(2);
        assertThat(buffer.getLastLineIndex()).isEqualTo(1);
    }

    @Test
    void testAppendSequenceOfNewLineChar() {
        CodeBuffer buffer = new CodeBuffer();
        buffer.append("foo\n\nbar");
        assertThat(buffer.getNextCharIndex()).isEqualTo(3);
        assertThat(buffer.getNextLineIndex()).isEqualTo(2);
        assertThat(buffer.getLastCharIndex()).isEqualTo(2);
        assertThat(buffer.getLastLineIndex()).isEqualTo(2);
    }

    @Test
    void testStartNewLine() {
        CodeBuffer buffer = new CodeBuffer();
        buffer.append("foo");
        buffer.startNewLine();
        assertThat(buffer.getNextCharIndex()).isZero();
        assertThat(buffer.getNextLineIndex()).isEqualTo(1);
        assertThat(buffer.getLastCharIndex()).isEqualTo(3);
        assertThat(buffer.getLastLineIndex()).isZero();
        buffer.append("bar");
        assertThat(buffer.getNextCharIndex()).isEqualTo(3);
        assertThat(buffer.getNextLineIndex()).isEqualTo(1);
        assertThat(buffer.getLastCharIndex()).isEqualTo(2);
        assertThat(buffer.getLastLineIndex()).isEqualTo(1);
    }

    @Test
    void testDeleteLastChar() {
        CodeBuffer buffer = new CodeBuffer();
        buffer.append("foo");
        buffer.deleteLastChar();
        assertThat(buffer.getNextCharIndex()).isEqualTo(2);
        assertThat(buffer.getNextLineIndex()).isZero();
        assertThat(buffer.getLastCharIndex()).isEqualTo(1);
        assertThat(buffer.getLastLineIndex()).isZero();
        buffer.deleteLastChar();
        assertThat(buffer.getNextCharIndex()).isEqualTo(1);
        assertThat(buffer.getNextLineIndex()).isZero();
        assertThat(buffer.getLastCharIndex()).isZero();
        assertThat(buffer.getLastLineIndex()).isZero();
        buffer.deleteLastChar();
        assertThat(buffer.getNextCharIndex()).isZero();
        assertThat(buffer.getNextLineIndex()).isZero();
        assertThat(buffer.getLastCharIndex()).isEqualTo(-1);
        assertThat(buffer.getLastLineIndex()).isZero();
    }

    @Test
    void testDeleteLastChars() {
        CodeBuffer buffer = new CodeBuffer();
        buffer.append("foo");
        buffer.deleteLastChars(2);
        assertThat(buffer.getNextCharIndex()).isEqualTo(1);
        assertThat(buffer.getNextLineIndex()).isZero();
        assertThat(buffer.getLastCharIndex()).isZero();
        assertThat(buffer.getLastLineIndex()).isZero();
    }

    @Test
    void testDeleteLastCharsWhenExceedBufferLength() {
        CodeBuffer buffer = new CodeBuffer();
        buffer.append("foo");
        buffer.deleteLastChars(10);
        assertThat(buffer.getNextCharIndex()).isZero();
        assertThat(buffer.getNextLineIndex()).isZero();
        assertThat(buffer.getLastCharIndex()).isEqualTo(-1);
        assertThat(buffer.getLastLineIndex()).isZero();
    }

    @Test
    void testDeleteLastCharForNewLine() {
        CodeBuffer buffer = new CodeBuffer();
        buffer.append("foo");
        buffer.startNewLine();
        buffer.append("barrr");
        buffer.startNewLine();
        buffer.append("c");
        buffer.deleteLastChar();
        assertThat(buffer.getNextCharIndex()).isZero();
        assertThat(buffer.getNextLineIndex()).isEqualTo(2);
        assertThat(buffer.getLastCharIndex()).isEqualTo(5);
        assertThat(buffer.getLastLineIndex()).isEqualTo(1);
        buffer.deleteLastChar();
        assertThat(buffer.getNextCharIndex()).isEqualTo(5);
        assertThat(buffer.getNextLineIndex()).isEqualTo(1);
        assertThat(buffer.getLastCharIndex()).isEqualTo(4);
        assertThat(buffer.getLastLineIndex()).isEqualTo(1);
    }

    @Test
    void testDeleteEndingIfEndingIs() {
        CodeBuffer buffer = new CodeBuffer();
        buffer.append("foo");
        buffer.startNewLine();
        buffer.append("barrr");
        buffer.startNewLine();
        buffer.append("c");
        buffer.deleteEndingIfEndingIs("c");
        assertThat(buffer.getNextCharIndex()).isZero();
        assertThat(buffer.getNextLineIndex()).isEqualTo(2);
        assertThat(buffer.getLastCharIndex()).isEqualTo(5);
        assertThat(buffer.getLastLineIndex()).isEqualTo(1);
        buffer.deleteEndingIfEndingIs("arrr\n");
        assertThat(buffer.getNextCharIndex()).isEqualTo(1);
        assertThat(buffer.getNextLineIndex()).isEqualTo(1);
        assertThat(buffer.getLastCharIndex()).isZero();
        assertThat(buffer.getLastLineIndex()).isEqualTo(1);
    }
}
