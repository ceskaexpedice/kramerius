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

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import net.sf.json.JSONObject;
import cz.incad.kramerius.rest.api.k5.client.item.Decorator;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.FedoraUtils;

public class SmallImageDecorate implements Decorator {

	@Inject
	Provider<HttpServletRequest> reqProvider;	
	
	@Override
	public String getKey() {
		return FedoraUtils.IMG_THUMB_STREAM;
	}

	@Override
	public boolean applyOnContext(String context) {
		return true;
	}



	@Override
	public void decorate(JSONObject jsonObject) {
		String str = ApplicationURL.applicationURL(this.reqProvider.get()).toString()+"/img?pid="+jsonObject.getString("pid")+"&stream=IMG_THUMB&action=GETRAW";
		jsonObject.put(FedoraUtils.IMG_THUMB_STREAM, str);
	}
	
}
