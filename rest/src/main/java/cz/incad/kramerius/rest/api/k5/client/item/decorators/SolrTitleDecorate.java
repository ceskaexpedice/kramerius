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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import net.sf.json.JSONObject;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.item.Decorator;
import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;
import cz.incad.kramerius.utils.XMLUtils;

public class SolrTitleDecorate implements Decorator {

	public static final Logger LOGGER = Logger.getLogger(SolrTitleDecorate.class.getName());
	
	public static final String SOLR_TITLE_KEY ="SOLR_TITLE";
	
	@Inject
	SolrAccess solrAccess;
	
	@Override
	public String getKey() {
		return SOLR_TITLE_KEY;
	}

	@Override
	public void decorate(JSONObject jsonObject) {
		try {
			String pid = jsonObject.getString("pid");
			Document solrDoc = this.solrAccess.getSolrDataDocument(pid);
			Element result = XMLUtils.findElement(solrDoc.getDocumentElement(), "result");
			if (result != null) {
				Element doc = XMLUtils.findElement(result, "doc");
				if (doc != null) {
					String title = SOLRUtils.string(doc, "root_title");
					if (title != null) {
						jsonObject.put("title", title);
					}
				}
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
		}
	}

	@Override
	public boolean applyOnContext(String context) {
		// TODO: jaky kontext dat ??
		return true;
	}

}
