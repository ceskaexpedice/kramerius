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
package cz.incad.kramerius.rest.api.k5.client.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import cz.incad.kramerius.SolrAccess;

public class SOLRDecoratorUtils {

    public static final String SOLR_CHILDREN_DOCUMENT_KEY = "solr_children_document";

    public synchronized static void cacheChildrenDocuments(String pid, String query, Document doc, SolrAccess solrAccess, Map<String, Object> context) throws IOException {
        String key = SOLR_CHILDREN_DOCUMENT_KEY + "_" + pid;
        if (!context.containsKey(key)) {
            if (PIDSupport.isComposedPID(pid)) {
                pid = PIDSupport.convertToSOLRType(pid);
            }
            List<Document> docs = new ArrayList<Document>();
            context.put(key, solrAccess.getSolrDataDocument(pid));
        }
    }

    
    /*
    public static Document getSolrSearchDocument(String pid, String query,
            Map<String, Object> context, SolrAccess solrAccess)
            throws IOException {
        String key = SOLR_CHILDREN_DOCUMENT_KEY + "_" + pid;
        if (!context.containsKey(key)) {
            if (PIDSupport.isComposedPID(pid)) {
                pid = PIDSupport.convertToSOLRType(pid);
            }
            Document doc = solrAccess.request(query);
            context.put(key, solrAccess.getSolrDataDocument(pid));
        }
        return (Document) context.get(key);
    }*/

    /*
    public static final String SOLR_PID_DOCUMENT_KEY = "solr_pid_document";

    public static Document getSolrPidDocument(String pid,
            Map<String, Object> context, SolrAccess solrAccess)
            throws IOException {
        String key = SOLR_PID_DOCUMENT_KEY + "_" + pid;
        if (!context.containsKey(key)) {
            if (PIDSupport.isComposedPID(pid)) {
                pid = PIDSupport.convertToSOLRType(pid);
            }
            context.put(key, solrAccess.getSolrDataDocument(pid));
        }
        return (Document) context.get(key);
    }*/

}
