/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.utils.json;

/**
 * Utility class for JSON objects
 * @author pavels
 */
public class JSONUtils {

    /**
     * Escape quotes
     * @param data Raw data
     * @return String with escape sequences
     */
    public static String escapeQuotes(String data) {
        return data.replace("\"", "\\\"");
    }
    
    /**
     * Cut quotes
     * @param data Raw data
     * @return String without quotes
     */
    public static String cutQuotes(String data) {
        if (data.startsWith("\"")) {
            data = data.substring(1);
        }
        if (data.endsWith("\"")) {
            data = data.substring(0,data.length() -1);
        }
        return data;
    }
    
    public static void main(String[] args) {
        String data = "nevi\"m";
        System.out.println(data);
        System.out.println(escapeQuotes(data));
    }
}
