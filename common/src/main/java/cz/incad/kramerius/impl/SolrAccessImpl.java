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
import java.io.InputStream;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.solr.SolrUtils;

public class SolrAccessImpl implements SolrAccess {

    @Override
    public Document getSolrDataDocumentByUUID(String uuid) throws IOException {
        if (SpecialObjects.isSpecialObject(uuid)) return null;
        try {
            return SolrUtils.getSolrDataInternal(SolrUtils.UUID_QUERY+uuid);
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    @Override
    public String[] getPathOfUUIDs(String uuid) throws IOException {
        if (SpecialObjects.isSpecialObject(uuid)) return new String[0];
        try {
            Document solrData = getSolrDataDocumentByUUID(uuid);
            String pidPath = SolrUtils.disectPidPath(solrData);
            return pidPath.split("/");
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
    public String[] getPathOfModels(String uuid) throws IOException {
        if (SpecialObjects.isSpecialObject(uuid)) return new String[0];
        try {
            Document doc = getSolrDataDocumentByUUID(uuid);
            return SolrUtils.disectPath(doc).split("/");
        } catch (XPathExpressionException e) {
            throw new IOException(e);
       }
    }
    
}
