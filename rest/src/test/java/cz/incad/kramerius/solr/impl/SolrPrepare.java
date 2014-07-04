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
package cz.incad.kramerius.solr.impl;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class SolrPrepare {

    public static void solrMemoPrepare(SolrMemoization memo, String pid) throws LexerException, ParserConfigurationException, SAXException, IOException {
        if (PIDSupport.isComposedPID(pid)) {
            String first = PIDSupport.first(pid);

            PIDParser pidParser = new PIDParser(first);
            pidParser.objectPid();
            String objectId = pidParser.getObjectId();

            String path = "/solr/res/" + objectId + "@" + PIDSupport.rest(pid)
                    + ".xml";
            URL urlREs = SolrPrepare.class.getResource(path);
            Document parsedDoc = XMLUtils.parseDocument(urlREs.openStream());

            Element docElm = XMLUtils.findElement(parsedDoc.getDocumentElement(), "doc");
            
            EasyMock.expect(memo.getRememberedIndexedDoc(pid))
                    .andReturn(docElm).anyTimes();

        } else {
            PIDParser pidParser = new PIDParser(pid);
            pidParser.objectPid();
            String objectId = pidParser.getObjectId();

            URL urlREs = SolrPrepare.class.getResource("/solr/res/" + objectId
                    + ".xml");
            Document parsedDoc = XMLUtils.parseDocument(urlREs.openStream());

            Element docElm = XMLUtils.findElement(parsedDoc.getDocumentElement(), "doc");
            
            EasyMock.expect(memo.getRememberedIndexedDoc(pid))
                    .andReturn(docElm).anyTimes();
        }
        
    }
    
    public static void solrDataDocument(SolrAccess solrAccess, String pid)
            throws IOException, ParserConfigurationException, SAXException,
            LexerException {
        if (PIDSupport.isComposedPID(pid)) {
            String first = PIDSupport.first(pid);

            PIDParser pidParser = new PIDParser(first);
            pidParser.objectPid();
            String objectId = pidParser.getObjectId();

            String path = "/solr/res/" + objectId + "@" + PIDSupport.rest(pid)
                    + ".xml";
            URL urlREs = SolrPrepare.class.getResource(path);
            Document parsedDoc = XMLUtils.parseDocument(urlREs.openStream());
            EasyMock.expect(solrAccess.getSolrDataDocument(pid))
                    .andReturn(parsedDoc).anyTimes();

        } else {
            PIDParser pidParser = new PIDParser(pid);
            pidParser.objectPid();
            String objectId = pidParser.getObjectId();

            URL urlREs = SolrPrepare.class.getResource("/solr/res/" + objectId
                    + ".xml");
            Document parsedDoc = XMLUtils.parseDocument(urlREs.openStream());
            EasyMock.expect(solrAccess.getSolrDataDocument(pid))
                    .andReturn(parsedDoc).anyTimes();
        }
    }
}
