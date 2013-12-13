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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.AbstractDecorator;
import cz.incad.kramerius.rest.api.k5.client.AbstractSolrDecorator;
import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;
import cz.incad.kramerius.utils.XMLUtils;

public class SolrDataNode extends AbstractSolrDecorator {

	public static final Logger LOGGER = Logger.getLogger(SolrSiblingsDecorate.class.getName());

	public static final String KEY = "DATA_NODE";

    @Inject
    SolrAccess solrAccess;


	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public void decorate(JSONObject jsonObject, Map<String, Object> context) {
		try {
			String pid = jsonObject.getString("pid");
			Document solrDoc = getSolrPidDocument(pid, context, solrAccess);
			Element result = XMLUtils.findElement(solrDoc.getDocumentElement(), "result");
			if (result != null) {
				Boolean value = SOLRUtils.value(result, "viewable", Boolean.class);
				if (value != null) {
					jsonObject.put("datanode", value);
				} else {
					jsonObject.put("datanode", false);
				}
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,e.getMessage(),e);
		}
	}
	
	// pro vsechny
	@Override
	public boolean applyOnContext(String context) {
		return true;
	}
}
