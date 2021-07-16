package com.google.common.css.compiler.ast;

public abstract class EscapeUtil {

    private EscapeUtil() {
    }

    /**
     * Replaces unprintable characters by their escaped (or unicode escaped)
     * equivalents in the given string
     * Used to convert raw characters to their escaped version
     * when these raw version cannot be used as part of an ASCII
     * string literal.
     *
     * @param str input
     * @return escaped string
     */
    public static String addEscapes(String str) {
        StringBuilder retval = new StringBuilder();
        char ch;
        for (int i = 0; i < str.length(); i++) {
            switch (str.charAt(i)) {
                case '\b':
                    retval.append("\\b");
                    continue;
                case '\t':
                    retval.append("\\t");
                    continue;
                case '\n':
                    retval.append("\\n");
                    continue;
                case '\f':
                    retval.append("\\f");
                    continue;
                case '\r':
                    retval.append("\\r");
                    continue;
                case '\"':
                    retval.append("\\\"");
                    continue;
                case '\'':
                    retval.append("\\'");
                    continue;
                case '\\':
                    retval.append("\\\\");
                    continue;
                default:
                    if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
                        String s = "0000" + Integer.toString(ch, 16);
                        retval.append("\\u").append(s.substring(s.length() - 4));
                    } else {
                        retval.append(ch);
                    }
            }
        }
        return retval.toString();
    }
}
