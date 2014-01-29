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

import net.sf.json.JSONObject;

/**
 * Implementation of this interface is able to decorate resulting JSON object
 * @author pavels
 *
 */
public interface JSONDecorator {

	/**
	 * Returns unique key of this decorator
	 * @return
	 */
	public String getKey();
	
	/**
	 * Decorate method
	 * @param jsonObject Resulting json object
	 * @param runtimeContext Running context
	 */
	public void decorate(JSONObject jsonObject, Map<String, Object> runtimeContext);

	/**
	 * Returns true, if the decorator can be applied
	 * @param jsonObject JSON object
	 * @param context The web context
	 * @return
	 */
	public boolean apply(JSONObject jsonObject, String context);
	
	
	public Map<String, Object> getRunningContext();
	
	public void before(Map<String, Object> runningContext);
	
	public void after();
}
