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
package cz.incad.Kramerius.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pavels
 *
 */
public class JSONUtils {

    public static String escaped(String input) {
        StringBuilder retString = new StringBuilder();
        List<Character> mustBeEscaped = new ArrayList<Character>(); {
            mustBeEscaped.add(new Character('\\'));
            mustBeEscaped.add(new Character('\n'));
            mustBeEscaped.add(new Character('\''));
            mustBeEscaped.add(new Character('"'));
        }
        Map<Character,String> escapingPairs = new HashMap<Character, String>(); {
            escapingPairs.put(new Character('\n'), "\\n");
            escapingPairs.put(new Character('"'), "\\\"");
            escapingPairs.put(new Character('\''), "\'");
            escapingPairs.put(new Character('\\'), "\\\\");
        }
        char[] chArray = input.toCharArray();
        for (int i = 0; i < chArray.length; i++) {
            char ch = chArray[i];
            if (mustBeEscaped.contains(new Character(ch))) {
                retString.append('\\');
                retString.append(escapingPairs.get(new Character(ch)));
            } else {
                retString.append(ch);
            }
        }
        return retString.toString();
    }

}
