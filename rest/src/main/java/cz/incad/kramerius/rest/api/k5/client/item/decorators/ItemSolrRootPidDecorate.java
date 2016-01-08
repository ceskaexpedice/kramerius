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
 * Doplni root pid z indexu
 * 
 * @author pavels
 */
public class ItemSolrRootPidDecorate extends AbstractItemDecorator {

    public static final Logger LOGGER = Logger
            .getLogger(ItemSolrRootPidDecorate.class.getName());

    public static final String SOLR_ROOTPID_KEY = AbstractItemDecorator
            .key("ROOTPID");

    @Inject
    SolrAccess solrAccess;

    @Inject
    SolrMemoization memo;
    
    @Override
    public String getKey() {
        return SOLR_ROOTPID_KEY;
    }

    @Override
    public void decorate(JSONObject jsonObject,
            Map<String, Object> runtimeContext) {
        if (jsonObject.has("pid")) {
            try {
                String pid = jsonObject.getString("pid");
                Element doc = this.memo.getRememberedIndexedDoc(pid);
                if (doc == null) doc = this.memo.askForIndexDocument(pid);
                if (doc != null) {
                    String root_pid = SOLRUtils.value(doc, "root_pid",
                            String.class);
                    if (root_pid != null) {
                        jsonObject.put("root_pid", root_pid);
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
    }

    @Override
    public boolean apply(JSONObject jsonObject, String context) {
        TokenizedPath tpath = super.itemContext(tokenize(context));
        return tpath.isParsed();
    }
}
