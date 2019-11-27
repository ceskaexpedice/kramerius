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
import java.util.Map;

import org.w3c.dom.Document;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;

/**
 * Dedicated for getting RELS_EXT, storing it in the context and return it
 */
public class RELSEXTDecoratorUtils {

    public static final String RELSEXT_PID_DOCUMENT_KEY = "relsext_pid_document";


    public static Document getRELSEXTPidDocument(String pid,
            Map<String, Object> context, FedoraAccess fedoraAccess)
            throws IOException {
        String key = RELSEXT_PID_DOCUMENT_KEY + "_" + pid;
        if (!context.containsKey(key)) {
            context.put(key, fedoraAccess.getRelsExt(pid));
        }
        return (Document) context.get(key);
    }
}
