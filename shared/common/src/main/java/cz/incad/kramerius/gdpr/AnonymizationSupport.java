/*
 * Copyright (C) Sep 4, 2024 Pavel Stastny
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
package cz.incad.kramerius.gdpr;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;


public class AnonymizationSupport {

    public static final Logger LOGGER = Logger.getLogger(AnonymizationSupport.class.getName());
    
    public static final List<String> DEFAULT_ANONYMIZATION_PROPERTIES = Arrays.asList(
    "username",
    "session_eppn",
    "dnnt_user",
    "eduPersonUniqueId",
    "affilation",
    "remoteAddr",
    "eduPersonPrincipalName",
    "email",
    "preffered_user_name");

    public static JSONObject annonymizeObject(List<String> annonymizationKeys, String line) {
        JSONObject lineJSONObject = new JSONObject(line);
        annonymizationKeys.stream().forEach(key -> {
            if (lineJSONObject.has(key)) {
                Object o = lineJSONObject.get(key);
                if (o instanceof String) {
                    String stringToBeHashed = o.toString();
                    String newVal = null;
                    if (stringToBeHashed.contains("@")) {
                        String[] split = stringToBeHashed.split("@");
                        newVal = String.format("%s@%s", hashVal(split[0]), hashVal(split[1]));
                    } else {
                        newVal = hashVal(stringToBeHashed);
                    }
                    lineJSONObject.put(key, newVal);
                }
            }
        });
        return lineJSONObject;
    }

    public static String hashVal(String value) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            Base64.Encoder base64Encoder = Base64.getEncoder();
            md5.update(value.getBytes("UTF-8"));
            return base64Encoder.encodeToString(md5.digest());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return value;
        }
    }
}
