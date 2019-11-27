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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;

public class SolrContextDecorate extends AbstractItemDecorator {

    public static final Logger LOGGER = Logger
            .getLogger(SolrContextDecorate.class.getName());

    public static final String SOLR_CONTEXT_KEY = AbstractItemDecorator
            .key("CONTEXT");

    @Inject
    SolrAccess solrAccess;

    @Inject
    SolrMemoization memo;
    
    @Override
    public String getKey() {
        return SOLR_CONTEXT_KEY;
    }

    @Override
    public void decorate(JSONObject jsonObject, Map<String, Object> context) {
        try {
            String pid = jsonObject.getString("pid");
                
            Element doc = this.memo.getRememberedIndexedDoc(pid);
            if (doc == null) doc = this.memo.askForIndexDocument(pid);

            if (doc != null) {
                    List<String> pidPaths = SOLRUtils.narray(doc, "pid_path",
                            String.class);
                    List<String> modelPaths = SOLRUtils.narray(doc,
                            "model_path", String.class);
                    if (pidPaths != null && modelPaths != null) {
                        JSONArray jaContext = new JSONArray();
                        for (int i = 0; i < pidPaths.size(); i++) {
                            JSONArray ja = new JSONArray();
                            String[] pids = pidPaths.get(i).split("/");
                            String[] models = modelPaths.get(i).split("/");
                            for (int j = 0; j < pids.length; j++) {
                                JSONObject jo = new JSONObject();
                                jo.put("pid", pids[j]);
                                jo.put("model", models[j]);
                                ja.put(jo);
                            }
                            jaContext.put(ja);
                        }
                        jsonObject.put("context", jaContext);
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
        return (tpath.isParsed() && tpath.getRestPath().isEmpty());
    }
}
