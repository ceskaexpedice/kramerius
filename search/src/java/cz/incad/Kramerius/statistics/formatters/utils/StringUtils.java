/*
 * Copyright (C) 2012 Pavel Stastny
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
/**
 * 
 */
package cz.incad.Kramerius.statistics.formatters.utils;

/**
 * @author pavels
 *
 */
public class StringUtils {

    public static String nullify(String str) {
        return str != null  ? str : "";
    }

    public static boolean shouldEscape(char ch, char[]escapingCharss) {
        for (char c : escapingCharss) {
            if (ch == c) return true;
        }
        return false;
    }
    
    public static String escapeChars(String str, char[] escapingChars) {
        StringBuilder builder = new StringBuilder();
        char[] chrs = str.toCharArray();
        for (char c : chrs) {
            if (shouldEscape(c, escapingChars)) builder.append('\\');
            else builder.append(c);
        }
        return builder.toString();
    }
    
    public static String escapeNewLine(String str) {
        StringBuilder builder = new StringBuilder();
        char[] chrs = str.toCharArray();
        for (char c : chrs) {
            if (c=='\n') builder.append(' ');
            else builder.append(c);
        }
        return builder.toString();
    }

}
