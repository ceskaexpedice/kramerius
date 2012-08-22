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
package cz.incad.kramerius.lp.utils;

public class ASCIITranslate {

    private static char BLANK='_';
    
    private static String CASES = "áäčďéěíľĺňóöőôřŕšťúůüűýžÁÄČĎÉĚÍĽĹŇÓÖŐÔŘŔŠŤÚŮÜŰÝŽ";
    private static String TRANSLATED = "aacdeeillnoooorrstuuuuyzAACDEEILLNOOOORRSTUUUUYZ";


    public static char translatetable(char ch) {
        if (CASES.indexOf(ch) >= 0) {
            return TRANSLATED.charAt(CASES.indexOf(ch));
        }
        return BLANK;
    }

    public static char toch(char ch) {
        if ((ch & 0x7F) == ch) {
            return ch;
        } else return translatetable(ch);
    }
    
    
    public static String asciiString(String input) {
        StringBuilder builder = new StringBuilder();
        for (char ch : input.toCharArray()) {
            builder.append(toch(ch));
        }
        return builder.toString();
    }
    
    
}
