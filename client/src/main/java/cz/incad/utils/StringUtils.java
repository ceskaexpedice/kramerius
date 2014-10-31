/*
 * Copyright (C) 2013 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.utils;

public class StringUtils {

    public static boolean isAnyString(String input) {
        return input != null && (!input.trim().equals(""));
    }
    
    public static boolean isEmptyString(String input) {
        return !isAnyString(input);
    }

    /**
     * Minus operator
     * @param bigger Bigger string
     * @param smaller Smaller string
     * @return result of Bigger - Smaller
     */
    public static String minus(String bigger, String smaller) {
        if (bigger.length() > smaller.length()) {
            return bigger.replace(smaller, "");
        } else throw new IllegalArgumentException("");
    }

    
    public static String escapeQueryChars(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // These characters are part of the query syntax and must be escaped
            if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '('
                    || c == ')' || c == ':' || c == '^' || c == '[' || c == ']'
                    || c == '\"' || c == '{' || c == '}' || c == '~'
                    || c == '*' || c == '?' || c == '|' || c == '&' || c == ';'
                    || Character.isWhitespace(c)) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

}
