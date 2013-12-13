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
package cz.incad.kramerius.rest.api.k5.client.item.decorators;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import net.sf.json.JSONObject;
import cz.incad.kramerius.rest.api.k5.client.AbstractDecorator;
import cz.incad.kramerius.rest.api.k5.client.Decorator;
import cz.incad.kramerius.rest.api.k5.client.utils.JSONUtils;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.FedoraUtils;

public class HandleDecorate  extends AbstractDecorator {

	private static final String KEY = "HREF";

	
	@Inject
	Provider<HttpServletRequest> requestProvider;

	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public void decorate(JSONObject jsonObject, Map<String, Object> context) {
		String str = ApplicationURL.applicationURL(this.requestProvider.get()).toString()+"/handle/"+jsonObject.getString("pid");
		JSONUtils.link(jsonObject, "handle", str);
		//jsonObject.put("handle", str);
	}

	@Override
	public boolean applyOnContext(String context) {
		return true;
	}
}
