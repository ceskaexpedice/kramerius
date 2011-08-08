/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.NotImplementedException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.utils.solr.SolrUtils;

public class SolrAccessImpl implements SolrAccess {

    @Override
    public Document getSolrDataDocument(String pid) throws IOException {
        if (SpecialObjects.isSpecialObject(pid)) return null;
        try {
            return SolrUtils.getSolrDataInternal(SolrUtils.UUID_QUERY+"\""+pid+"\"");
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    @Override
    public ObjectPidsPath[] getPath(String pid) throws IOException {
        if (SpecialObjects.isSpecialObject(pid)) return new ObjectPidsPath[] {ObjectPidsPath.REPOSITORY_PATH};
        try {
            Document solrData = getSolrDataDocument(pid);
            List<String> disected = SolrUtils.disectPidPaths(solrData);
            ObjectPidsPath[] paths = new ObjectPidsPath[disected.size()];
            for (int i = 0; i < paths.length; i++) {
                paths[i] = new ObjectPidsPath(disected.get(i).split("/"));
            }
            return paths;
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Document getSolrDataDocumentByHandle(String handle) throws IOException {
        try {
            handle = URLEncoder.encode(handle, "UTF-8");
            return SolrUtils.getSolrDataInternal(SolrUtils.HANDLE_QUERY+handle);
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    @Override
    public ObjectModelsPath[] getPathOfModels(String pid) throws IOException {
        if (SpecialObjects.isSpecialObject(pid)) return new ObjectModelsPath[] { ObjectModelsPath.REPOSITORY_PATH};
        try {
            Document doc = getSolrDataDocument(pid);
            List<String> disected = SolrUtils.disectModelPaths(doc);
            ObjectModelsPath[] paths = new ObjectModelsPath[disected.size()];
            for (int i = 0; i < paths.length; i++) {
                paths[i] = new ObjectModelsPath(disected.get(i));
            }
            return paths;
        } catch (XPathExpressionException e) {
            throw new IOException(e);
       }
    }
    
}
