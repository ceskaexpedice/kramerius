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
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.solr.SolrUtils;

public class SolrUtilsTest {

    @Test
    public void disectPIDPath() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        InputStream is = SolrUtilsTest.class.getResourceAsStream("solr1.xml");
        Document parsed = XMLUtils.parseDocument(is);
        List<String> disected = SolrUtils.disectPidPaths(parsed);
        Assert.assertTrue(disected.size() == 2);
        Assert.assertEquals("uuid:cd2b2ad0-62d4-11dd-ac0e-000d606f5dc6/uuid:5fe0b160-62d5-11dd-bdc7-000d606f5dc6/uuid:28286e70-64a6-11dd-981a-000d606f5dc6",disected.get(0));
        Assert.assertEquals("uuid:cd2b2ad0-62d4-11dd-ac0e-000d606f5dc6/uuid:5fe0b160-62d5-11dd-bdc7-000d606f5dc6/uuid:f0da6570-8f3b-11dd-b796-000d606f5dc6/uuid:28286e70-64a6-11dd-981a-000d606f5dc6",disected.get(1));
    }

    @Test
    public void disectModels() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        InputStream is = SolrUtilsTest.class.getResourceAsStream("solr1.xml");
        Document parsed = XMLUtils.parseDocument(is);
        List<String> disected = SolrUtils.disectModelPaths(parsed);
        Assert.assertTrue(disected.size() == 2);
        Assert.assertEquals("monograph/monographunit/page",disected.get(0));
        Assert.assertEquals("monograph/monographunit/internalpart/page",disected.get(1));
    }    
}
