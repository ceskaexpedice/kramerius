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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.AbstractDecorator;
import cz.incad.kramerius.rest.api.k5.client.AbstractSolrDecorator;
import cz.incad.kramerius.rest.api.k5.client.Decorator;
import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;
import cz.incad.kramerius.utils.XMLUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SolrSiblingsDecorate extends  AbstractSolrDecorator {

	public static final Logger LOGGER = Logger.getLogger(SolrSiblingsDecorate.class.getName());
	
    public static final String SOLR_SIBLINGS_KEY = "SOLR_SIBLINGS";

    @Inject
    SolrAccess solrAccess;

	@Override
	public String getKey() {
		return SOLR_SIBLINGS_KEY;
	}

	@Override
	public void decorate(JSONObject jsonObject, Map<String, Object> context) {
    	try {
			String pid = jsonObject.getString("pid");
			Document solrDoc = getSolrPidDocument(pid, context, solrAccess);
            Element result = XMLUtils.findElement(solrDoc.getDocumentElement(), "result");
            if (result != null) {
            	JSONArray jsonArray = new JSONArray();
            	List<String> pid_paths = SOLRUtils.array(result, "pid_path", String.class);
                List<String> model_paths = SOLRUtils.array(result, "model_paths", String.class);
                int mIndex = Math.min(pid_paths.size(), model_paths.size());
                for (int i = 0; i < mIndex; i++) {
            		JSONObject pidObj = new JSONObject();
            		pidObj.put("pid", pid_paths.get(i));
            		pidObj.put("model", model_paths.get(i));
            		jsonArray.add(pidObj);
                }
                jsonObject.put("context", jsonArray);
            }
			
    	} catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}


	}

	@Override
	public boolean applyOnContext(String context) {
		return (context.equals("siblings")); 
	}
}
