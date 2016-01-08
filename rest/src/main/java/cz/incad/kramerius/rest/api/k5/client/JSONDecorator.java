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
package cz.incad.kramerius.rest.api.k5.client;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Implementation of this interface is able to decorate resulting JSON object
 * 
 * @author pavels
 *
 */
public interface JSONDecorator {

    /**
     * Returns unique key of this decorator
     * 
     * @return
     */
    public String getKey();

    /**
     * Decorate method
     * 
     * @param jsonObject
     *            Resulting json object
     * @param runtimeContext
     *            Running context
     */
    public void decorate(JSONObject jsonObject,
            Map<String, Object> runtimeContext);

    /**
     * Returns true, if the decorator can be applied
     * 
     * @param jsonObject
     *            JSON object
     * @param context
     *            The web context
     * @return
     * @throws JSONException 
     */
    public boolean apply(JSONObject jsonObject, String context) throws JSONException;

    /**
     * Running context - suitable for sharing objects to prevent multiple
     * request, for example. solr data document
     * 
     * @return
     */
    public Map<String, Object> getRunningContext();

    /**
     * Before trigger - called before decorators started work
     * 
     * @param runningContext
     */
    public void before(Map<String, Object> runningContext);

    /**
     * After trigger - called after decorators finish their work
     */
    public void after();

}
