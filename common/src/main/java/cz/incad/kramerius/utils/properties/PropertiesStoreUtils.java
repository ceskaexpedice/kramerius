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
package cz.incad.kramerius.utils.properties;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import cz.incad.kramerius.utils.StringUtils;

/**
 * Helper class for storing properties in one line
 * @author pavels
 */
// TODO: move it to nparams lang
public class PropertiesStoreUtils {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PropertiesStoreUtils.class.getName());
    
    /**
     * Store properties to one line
     * @param properties Properties
     * @return Serialized properties
     */
    public static String storeProperties(Properties properties) {
        StringWriter writer = new StringWriter();
        Object[] keys = properties.keySet().toArray();
        //Set<Object> keys = properties.keySet();
        for (int i = 0,ll=keys.length; i < ll; i++) {
            String key = keys[i].toString();
            writer.write(StringUtils.escape( key.trim(), '\\', ';'));
            writer.write("=");
            writer.write(StringUtils.escape( properties.getProperty(key), '\\', ';'));
            if (i<ll-1) {
                writer.write(";");
            }
        }
        return writer.toString();
    }
    
    /**
     * Load properties from one line
     * @param str Serialized properties
     * @return Deserialized properites object
     */
    public static Properties loadProperties(String str) {
        List<Character> mustBeEscaped = Arrays.asList(new Character('\\'),new Character(';'));
        
        List<String> tokens = new ArrayList<String>();
        StringWriter processWriter = new StringWriter();
        Stack<Character> stckChars = new Stack<Character>();
        char[] charArray = str.toCharArray();
        for (int i = charArray.length-1; i >=0; i--) {
            stckChars.push(new Character(charArray[i]));
        }
        
        while(!stckChars.isEmpty()) {
            Character cChar = stckChars.pop();
            if ((cChar.equals(new Character('\\')) && (!stckChars.isEmpty()) && (mustBeEscaped.contains(stckChars.peek())))) {
                processWriter.write(stckChars.pop());
            } else if (cChar.equals(new Character(';'))){
                tokens.add(processWriter.toString());
                processWriter = new StringWriter();
            } else {
                processWriter.write(cChar);
            }
        }

        tokens.add(processWriter.toString());
        
        Properties props = new Properties();
        for (String token : tokens) {
            if (token.indexOf("=") > 0) {
                String[] vals = token.split("=");
                if (vals.length == 2) {
                    String key = vals[0];
                    String val = vals[1];
                    props.put(key, val);
                } else {
                    LOGGER.warning("no eq character in token '"+token+"'");
                }
            }
        }
        return props;
    }
}
