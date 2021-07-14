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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.css.SourceCodeLocation;

import java.text.MessageFormat;

/**
 * GSS parser error description.
 */
public class GssError implements Comparable<GssError> {
    private String message;
    private SourceCodeLocation location;
    private String line = null;
    private String format = null;

    public GssError(String message, SourceCodeLocation location) {
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(location);
        this.message = message;
        this.location = location;
    }

    public SourceCodeLocation getLocation() {
        return location;
    }

    public String getMessage() {
        return message;
    }

    public String getLine() {
        if (line == null) {
            String source = location.getSourceCode().getFileContents();
            int beginLineIndex =
                    location.getBeginCharacterIndex() - location.getBeginIndexInLine() + 1;
            int endLineIndex = source.indexOf('\n', location.getBeginCharacterIndex());
            if (endLineIndex == -1) {
                endLineIndex = source.length();
            }
            line = source.substring(beginLineIndex, endLineIndex);
        }
        return line;
    }

    public String format() {
        if (format == null) {
            format = messageFormat().apply();
        }
        return format;
    }

    /**
     * A String template, together with values for the template holes.
     * {@see java.text.MessageFormat}
     */
    public static class MessageFormatArgs {
        public final String pattern;
        public final Object[] arguments;

        public MessageFormatArgs(String pattern, Object... arguments) {
            this.pattern = pattern;
            this.arguments = arguments;
        }

        public String apply() {
            return MessageFormat.format(pattern, arguments);
        }
    }

    /**
     * Returns a MessageFormatArgs representation of the object. This is
     * a parbaked precursor to the value given by {@link #format},
     * intended for use in FormattingLoggers and other contexts where it
     * is useful to maintain separation between boilerplate and details.
     */
    public MessageFormatArgs messageFormat() {
        if (location.isUnknown()) {
            return new MessageFormatArgs("{0} at unknown location", message);
        } else if (location.getSourceCode().getFileName() == null) {
            return new MessageFormatArgs(
                    "{0} at line {1} column {2}:\n{3}\n{4}^\n",
                    message,
                    location.getBeginLineNumber(),
                    location.getBeginIndexInLine(), getLine(),
                    Strings.repeat(" ", location.getBeginIndexInLine() - 1));
        } else {
            return new MessageFormatArgs(
                    "{0} in {1} at line {2} column {3}:\n{4}\n{5}^\n",
                    message, location.getSourceCode().getFileName(),
                    location.getBeginLineNumber(),
                    location.getBeginIndexInLine(), getLine(),
                    Strings.repeat(" ", location.getBeginIndexInLine() - 1));
        }
    }

    @Override
    public boolean equals(Object o) {
        // Generated by Intellij IDEA
        if (this == o) return true;
        if (!(o instanceof GssError)) return false;

        GssError gssError = (GssError) o;

        if (!location.equals(gssError.location)) return false;
        if (!message.equals(gssError.message)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return 31 * message.hashCode() + location.hashCode();
    }

    /**
     * Compare errors by source name, location and message.
     */
    @Override
    public int compareTo(GssError o) {
        String source1 = location.getSourceCode().getFileName();
        String source2 = o.location.getSourceCode().getFileName();
        if (source1 != null && source2 != null) {
            int sourceCompare = source1.compareTo(source2);
            if (sourceCompare != 0) {
                return sourceCompare;
            }
        } else if (source1 == null && source2 != null) {
            return -1;
        } else if (source1 != null && source2 == null) {
            return 1;
        }

        // source1 and source2 are the same file.
        int locationCompare = location.compareTo(o.location);
        if (locationCompare != 0) {
            return locationCompare;
        }

        return message.compareTo(o.message);
    }
}
