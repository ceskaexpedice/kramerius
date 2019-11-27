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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;

/**
 * Dplni informaci o tom, zda se jedna o datanode
 * 
 * @author pavels
 */
public class SolrDataNode extends AbstractItemDecorator {

    public static final Logger LOGGER = Logger.getLogger(SolrDataNode.class
            .getName());

    public static final String KEY = AbstractItemDecorator.key("DATA_NODE");// "DATA_NODE";

    @Inject
    SolrAccess solrAccess;
    
    @Inject
    SolrMemoization memo;
    
    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void decorate(JSONObject jsonObject, Map<String, Object> context) {
        try {
            if (jsonObject.has("pid")) {
                String pid = jsonObject.getString("pid");
                
                    
                Element doc = this.memo.getRememberedIndexedDoc(pid);
                if (doc == null) doc = this.memo.askForIndexDocument(pid);
                if (doc != null) {
                    Boolean value = SOLRUtils.value(doc, "viewable",
                            Boolean.class);
                    if (value != null) {
                        jsonObject.put("datanode", value);
                    } else {
                        jsonObject.put("datanode", false);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        }
    }

    @Override
    public boolean apply(JSONObject jsonObject, String context) {
        TokenizedPath tpath = super.itemContext(tokenize(context));
        return tpath.isParsed();
    }
}
