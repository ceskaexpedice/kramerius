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
package cz.incad.kramerius.rest.api.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author pavels
 *
 */
public class ExceptionJSONObjectUtils {
    
    public static final String MESSAGE_KEY ="message";
    public static final String STATUS_CODE_KEY ="status";
    public static final String CAUSE_KEY ="cause";

    /**
     * Returns json object contains message key
     * @param mess 
     * @return
     * @throws JSONException 
     */
    public static JSONObject fromMessage(String mess, int statuscode)  {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(MESSAGE_KEY, mess);
            jsonObject.put(STATUS_CODE_KEY, statuscode);
            return jsonObject;
        } catch (JSONException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public static JSONObject fromMessage(String mess, int statuscode, Exception ex)  {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(MESSAGE_KEY, mess);
            jsonObject.put(STATUS_CODE_KEY, statuscode);
            StringWriter strWriter = new StringWriter();
            ex.printStackTrace(new PrintWriter(strWriter));
            jsonObject.put(CAUSE_KEY, strWriter.toString());
            return jsonObject;
        } catch (JSONException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
