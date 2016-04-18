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
package cz.incad.kramerius.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.solr.SolrUtils;



public class SolrUtilsTest {

    @Test
    public void disectPIDPath() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Document parsed = solrDocument();
        List<String> disected = SolrUtils.disectPidPaths(parsed);
        Assert.assertTrue(disected.size() == 2);
        Assert.assertEquals("uuid:cd2b2ad0-62d4-11dd-ac0e-000d606f5dc6/uuid:5fe0b160-62d5-11dd-bdc7-000d606f5dc6/uuid:28286e70-64a6-11dd-981a-000d606f5dc6",disected.get(0));
        Assert.assertEquals("uuid:cd2b2ad0-62d4-11dd-ac0e-000d606f5dc6/uuid:5fe0b160-62d5-11dd-bdc7-000d606f5dc6/uuid:f0da6570-8f3b-11dd-b796-000d606f5dc6/uuid:28286e70-64a6-11dd-981a-000d606f5dc6",disected.get(1));
    }

    @Test
    public void disectPIDPath2() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Document parsed = solrDocument2();
        List<String> disected = SolrUtils.disectPidPaths(parsed);
        Assert.assertTrue(disected.size() == 1);
        Assert.assertEquals("uuid:bdc405b0-e5f9-11dc-bfb2-000d606f5dc6/uuid:b236d435-435d-11dd-b505-00145e5790ea/uuid:b7df7f2b-435d-11dd-b505-00145e5790ea/uuid:55219067-435f-11dd-b505-00145e5790ea", disected.get(0));
    }

    @Test
    public void disectPID() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Document parsed = solrDocument2();
        String disectedPid = SolrUtils.disectPid(parsed);
        Assert.assertNotNull(disectedPid);
        Assert.assertEquals("uuid:55219067-435f-11dd-b505-00145e5790ea", disectedPid);
    }


    @Test
    public void disectPIDFromPDFDoc() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Document parsed = solrPDFDocument2();
        //XMLUtils.getElements(parsed.getDocumentElement(),new XMLUT);
        Element findElement = XMLUtils.findElement(parsed.getDocumentElement(), "result");
        Assert.assertNotNull(findElement);
        List<Element> elements = XMLUtils.getElements(findElement, new XMLUtils.ElementsFilter() {

            @Override
            public boolean acceptElement(Element element) {
                return element.getNodeName().equals("doc");
            }
        });
        Assert.assertEquals("uuid:0823498e-bd85-4a98-b649-42ee5d43f5d8", SolrUtils.disectPid(elements.get(0)));
        Assert.assertEquals("uuid:0823498e-bd85-4a98-b649-42ee5d43f5d8/@1", SolrUtils.disectPid(elements.get(1)));
        Assert.assertEquals("uuid:0823498e-bd85-4a98-b649-42ee5d43f5d8/@2", SolrUtils.disectPid(elements.get(2)));
        Assert.assertEquals("uuid:0823498e-bd85-4a98-b649-42ee5d43f5d8/@3", SolrUtils.disectPid(elements.get(3)));
        Assert.assertEquals("uuid:0823498e-bd85-4a98-b649-42ee5d43f5d8/@4", SolrUtils.disectPid(elements.get(4)));
        Assert.assertEquals("uuid:0823498e-bd85-4a98-b649-42ee5d43f5d8/@5", SolrUtils.disectPid(elements.get(5)));
        Assert.assertEquals("uuid:0823498e-bd85-4a98-b649-42ee5d43f5d8/@6", SolrUtils.disectPid(elements.get(6)));
    }

    public static Document solrDocument() throws ParserConfigurationException, SAXException, IOException {
        InputStream is = SolrUtilsTest.class.getResourceAsStream("solr1.xml");
        Document parsed = XMLUtils.parseDocument(is);
        return parsed;
    }


    public static Document solrDocument2() throws ParserConfigurationException, SAXException, IOException {
        InputStream is = SolrUtilsTest.class.getResourceAsStream("solr2.xml");
        Document parsed = XMLUtils.parseDocument(is);
        return parsed;
    }

    public static Document solrPDFDocument() throws ParserConfigurationException, SAXException, IOException {
        InputStream is = SolrUtilsTest.class.getResourceAsStream("solrpdf.xml");
        Document parsed = XMLUtils.parseDocument(is);
        return parsed;
    }
    public static Document solrPDFDocument2() throws ParserConfigurationException, SAXException, IOException {
        InputStream is = SolrUtilsTest.class.getResourceAsStream("solrpdf2.xml");
        Document parsed = XMLUtils.parseDocument(is);
        return parsed;
    }

    
    @Test
    public void disectModels() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Document parsed = solrDocument();
        List<String> disected = SolrUtils.disectModelPaths(parsed);
        Assert.assertTrue(disected.size() == 2);
        Assert.assertEquals("monograph/monographunit/page",disected.get(0));
        Assert.assertEquals("monograph/monographunit/internalpart/page",disected.get(1));
    }    
}
