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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import net.sf.json.JSONObject;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.item.Decorator;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.utils.FedoraUtils;

public class RightsDecorate implements Decorator {

	private static final String KEY = "RIGHTS";

	@Inject
	IsActionAllowed isActionAllowed;

	@Inject
	SolrAccess solrAccess;

	
	@Inject
	Provider<HttpServletRequest> requestProvider;
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public void decorate(JSONObject jsonObject) {
		try {
			boolean flag = false;
			String pid = jsonObject.getString("pid");
			ObjectPidsPath[] path = this.solrAccess.getPath(pid);
			for (ObjectPidsPath p : path) {
				if (isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), pid, null, p)) {
					flag = true;
					break;
				}
			}
			jsonObject.put("right", flag);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean applyOnContext(String context) {
		return "".equals(context);
	}

}
