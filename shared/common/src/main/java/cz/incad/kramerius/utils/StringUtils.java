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
package cz.incad.kramerius.utils;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.antlr.stringtemplate.StringTemplate;

/**
 * String utilities
 * @author pavels
 */
public class StringUtils {

    /**
     * Minus operator
     * @param bigger Bigger string
     * @param smaller Smaller string
     * @return result of Bigger - Smaller
     */
    public static String minus(String bigger, String smaller) {
        if (bigger.length() >= smaller.length()) {
            return bigger.replace(smaller, "");
        } else throw new IllegalArgumentException("");
    }
    
    
    /**
     * Returns string with escape sequences 
     * @param rawString Given string 
     * @param escapeChar Escape character
     * @param charsMustBeEscaped All characters that must be escaped
     * @return string with escape sequences
     */
    public static String escape(String rawString, Character escapeChar, Character ... charsMustBeEscaped) {
        StringWriter writer = new StringWriter();
        List<Character> mustBeEscaped = Arrays.asList(charsMustBeEscaped);
        char[] charArray = rawString.toCharArray();
        for (char c : charArray) {
            if (mustBeEscaped.contains(new Character(c))) {
                writer.write('\\');
            }
            writer.write(c);
        }
        return writer.toString();
    }
    
    /**
     * Remove escape sequences from given string 
     * @param rawString Given string
     * @param escapeChar Escape sequnce character
     * @param charsMustBeEscaped All characters that must be escaped
     * @return 
     */
    public static String unescape(String rawString, Character escapeChar, Character ... charsMustBeEscaped) {
        StringWriter writer = new StringWriter();
        List<Character> mustBeEscaped = Arrays.asList(charsMustBeEscaped);
        Stack<Character> stckChars = new Stack<Character>();
        char[] charArray = rawString.toCharArray();
        for (int i = charArray.length-1; i >=0; i--) {
            stckChars.push(new Character(charArray[i]));
        }
        
        while(!stckChars.isEmpty()) {
            Character cChar = stckChars.pop();
            if ((cChar.equals(escapeChar)) && (!stckChars.isEmpty()) && (mustBeEscaped.contains(stckChars.peek()))) {
                writer.write(stckChars.pop());
            } else {
                writer.write(cChar);
            }
        }
        
        return writer.toString();
        
    }
    

    /**
     * Returns true if given input string contains any characters otherwise returns false
     * @param input Input string 
     * @return True if given string contains any character otherwise returns false
     */
    public static boolean isAnyString(String input) {
        return input != null && (!input.trim().equals(""));
    }
}
