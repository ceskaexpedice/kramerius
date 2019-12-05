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
package cz.incad.kramerius.database.utils;

import java.util.StringTokenizer;


/**
 * Helper class for interpret version value from database
 * @author pavels
 */
public class VersionVal {
    
    /**
     * Interpret database version value. Must contains 3 digits separated by dot.
     * @param ver String version
     * @return Value of database version
     */
    public static int interpretVersionValue(String ver) {
        StringTokenizer tokenizer = new StringTokenizer(ver,".");
        if (tokenizer.countTokens() == 3) {
            int first = Integer.parseInt(tokenizer.nextToken()) * 100;
            int second = Integer.parseInt(tokenizer.nextToken())*10;
            int third = Integer.parseInt(tokenizer.nextToken());
            return first + second + third;
        } else throw new IllegalArgumentException("illegal version value '"+ver+"'");
    }
}
