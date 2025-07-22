/*
 * Copyright (C) 2025  Inovatika
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
package cz.incad.kramerius.rest.oai.metadata;

import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.oai.OAIRecord;
import cz.incad.kramerius.rest.oai.metadata.utils.EDMUtils;
import cz.incad.kramerius.rest.oai.record.OAIRecordSupplement;
import cz.incad.kramerius.rest.oai.record.SupplementType;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class EDMUtilsTest {

    @Test
    public void testGenericCase() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        InputStream dcStream = this.getClass().getResourceAsStream("dc.xml");

        Configuration conf = EasyMock.createMock(Configuration.class);
        Instances instances = EasyMock.createMock(Instances.class);
        OneInstance knavInstance = EasyMock.createMock(OneInstance.class);
        OAIRecord oaiRecord = EasyMock.createMock(OAIRecord.class);

        EasyMock.expect(conf.getString("client")).andReturn("https://test-client").anyTimes();
        EasyMock.expect(conf.getString("acronym","")).andReturn("").anyTimes();
        EasyMock.expect(conf.getString("oai.set.edm.provider","")).andReturn("Czech digital library").anyTimes();
        EasyMock.expect(conf.getString("cdk.collections.sources.knav.name")).andReturn("Knihovna akademie ved").anyTimes();
        EasyMock.expect(conf.getString("cdk.collections.sources.knav.name_cs")).andReturn("Knihovna akademie ved").anyTimes();
        EasyMock.expect(conf.getString("cdk.collections.sources.knav.name_en")).andReturn("Library of academy of sciences").anyTimes();

        EasyMock.expect(instances.find("knav")).andReturn(knavInstance).anyTimes();
        EasyMock.expect(oaiRecord.getIdentifier()).andReturn("oai:oai-europeana.val.ceskadigitalniknihovna.cz:uuid:00035a90-9847-4822-95f8-e844694717eb").anyTimes();
        EasyMock.expect(oaiRecord.getSupplements()).andReturn(Arrays.asList(new OAIRecordSupplement("uuid:00035a90-9847-4822-95f8-e844694717aa", SupplementType.REPRESENTATIVE_PAGE_PID))).anyTimes();
        EasyMock.expect(knavInstance.getInstanceType()).andReturn(OneInstance.InstanceType.V7).anyTimes();

        EasyMock.replay(conf, instances, knavInstance, oaiRecord);


        Document owningDoc = XMLUtils.crateDocument("test");
        Element rdf = EDMUtils.createEdmDataElements(conf,
                "knav",
                "https://kramerius.lib.cas.cz/search/api",
                Arrays.asList("dnnto", "onsite"),
                instances, owningDoc, oaiRecord, dcStream, "uuid:00035a90-9847-4822-95f8-e844694717eb", "http://baseurl");

        XMLUtils.print(rdf, System.out);
        Assert.assertTrue(rdf != null);

        Assert.assertNotNull(XMLUtils.findElement(rdf, (elm)-> {
            return elm.getLocalName().equals("language") && elm.getNamespaceURI().equals("http://purl.org/dc/elements/1.1/");
        }));

        Assert.assertNotNull(XMLUtils.findElement(rdf, (elm)-> {
            return elm.getLocalName().equals("type") && elm.getNamespaceURI().equals("http://purl.org/dc/elements/1.1/");
        }));

        var found = XMLUtils.findElement(rdf, (elm)-> {
            return elm.getLocalName().equals("object") && elm.getNamespaceURI().equals("http://www.europeana.eu/schemas/edm/");
        });
        Assert.assertNotNull(found);
        String val =  found.getAttributeNodeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getValue();
        System.out.println(val);
        Assert.assertEquals("http://localhost:8080/search/api/v5.0/items/uuid:00035a90-9847-4822-95f8-e844694717aa/image", val);
    }

    @Test
    public void testNoLangElement() throws ParserConfigurationException, IOException, TransformerException, SAXException {
        InputStream dcStream = this.getClass().getResourceAsStream("no.language.dc.xml");

        Configuration conf = EasyMock.createMock(Configuration.class);
        Instances instances = EasyMock.createMock(Instances.class);
        OneInstance knavInstance = EasyMock.createMock(OneInstance.class);
        OAIRecord oaiRecord = EasyMock.createMock(OAIRecord.class);

        EasyMock.expect(conf.getString("client")).andReturn("https://test-client").anyTimes();
        EasyMock.expect(conf.getString("acronym","")).andReturn("").anyTimes();
        EasyMock.expect(conf.getString("oai.set.edm.provider","")).andReturn("Czech digital library").anyTimes();
        EasyMock.expect(conf.getString("cdk.collections.sources.knav.name")).andReturn("Knihovna akademie ved").anyTimes();
        EasyMock.expect(conf.getString("cdk.collections.sources.knav.name_cs")).andReturn("Knihovna akademie ved").anyTimes();
        EasyMock.expect(conf.getString("cdk.collections.sources.knav.name_en")).andReturn("Library of academy of sciences").anyTimes();


        EasyMock.expect(instances.find("knav")).andReturn(knavInstance).anyTimes();
        EasyMock.expect(oaiRecord.getIdentifier()).andReturn("oai:oai-europeana.val.ceskadigitalniknihovna.cz:uuid:00035a90-9847-4822-95f8-e844694717eb").anyTimes();
        EasyMock.expect(knavInstance.getInstanceType()).andReturn(OneInstance.InstanceType.V7).anyTimes();
        EasyMock.expect(oaiRecord.getSupplements()).andReturn(Arrays.asList(new OAIRecordSupplement("uuid:00035a90-9847-4822-95f8-e844694717aa", SupplementType.REPRESENTATIVE_PAGE_PID))).anyTimes();

        EasyMock.replay(conf, instances, knavInstance, oaiRecord);


        Document owningDoc = XMLUtils.crateDocument("test");
        Element rdf = EDMUtils.createEdmDataElements(conf,
                "knav",
                "https://kramerius.lib.cas.cz/search/api",
                Arrays.asList("dnnto", "onsite"),
                instances, owningDoc, oaiRecord, dcStream, "uuid:00035a90-9847-4822-95f8-e844694717eb", "http://baseurl");

        XMLUtils.print(rdf, System.out);
        Assert.assertTrue(rdf != null);

        Assert.assertNotNull(XMLUtils.findElement(rdf, (elm)-> {
            return elm.getLocalName().equals("language") && elm.getNamespaceURI().equals("http://purl.org/dc/elements/1.1/");
        }));

        Assert.assertNotNull(XMLUtils.findElement(rdf, (elm)-> {
            return elm.getLocalName().equals("type") && elm.getNamespaceURI().equals("http://purl.org/dc/elements/1.1/");
        }));
   }


    @Test
    public void testSubjectOrTypeElement() throws ParserConfigurationException, IOException, TransformerException, SAXException {
        InputStream dcStream = this.getClass().getResourceAsStream("no.subject_or_type.dc.xml");

        Configuration conf = EasyMock.createMock(Configuration.class);
        Instances instances = EasyMock.createMock(Instances.class);
        OneInstance knavInstance = EasyMock.createMock(OneInstance.class);
        OAIRecord oaiRecord = EasyMock.createMock(OAIRecord.class);

        EasyMock.expect(conf.getString("client")).andReturn("https://test-client").anyTimes();
        EasyMock.expect(conf.getString("acronym","")).andReturn("").anyTimes();
        EasyMock.expect(conf.getString("oai.set.edm.provider","")).andReturn("Czech digital library").anyTimes();
        EasyMock.expect(conf.getString("cdk.collections.sources.knav.name")).andReturn("Knihovna akademie ved").anyTimes();
        EasyMock.expect(conf.getString("cdk.collections.sources.knav.name_cs")).andReturn("Knihovna akademie ved").anyTimes();
        EasyMock.expect(conf.getString("cdk.collections.sources.knav.name_en")).andReturn("Library of academy of sciences").anyTimes();


        EasyMock.expect(instances.find("knav")).andReturn(knavInstance).anyTimes();
        EasyMock.expect(oaiRecord.getIdentifier()).andReturn("oai:oai-europeana.val.ceskadigitalniknihovna.cz:uuid:00035a90-9847-4822-95f8-e844694717eb").anyTimes();
        EasyMock.expect(oaiRecord.getSupplements()).andReturn(Arrays.asList(new OAIRecordSupplement("uuid:00035a90-9847-4822-95f8-e844694717aa", SupplementType.REPRESENTATIVE_PAGE_PID))).anyTimes();
        EasyMock.expect(knavInstance.getInstanceType()).andReturn(OneInstance.InstanceType.V7).anyTimes();

        EasyMock.replay(conf, instances, knavInstance, oaiRecord);


        Document owningDoc = XMLUtils.crateDocument("test");
        Element rdf = EDMUtils.createEdmDataElements(conf,
                "knav",
                "https://kramerius.lib.cas.cz/search/api",
                Arrays.asList("dnnto", "onsite"),
                instances, owningDoc, oaiRecord, dcStream, "uuid:00035a90-9847-4822-95f8-e844694717eb", "http://baseurl");

        XMLUtils.print(rdf, System.out);
        Assert.assertTrue(rdf != null);

        Assert.assertNotNull(XMLUtils.findElement(rdf, (elm)-> {
            return elm.getLocalName().equals("language") && elm.getNamespaceURI().equals("http://purl.org/dc/elements/1.1/");
        }));

        Assert.assertNotNull(XMLUtils.findElement(rdf, (elm)-> {
            return elm.getLocalName().equals("type") && elm.getNamespaceURI().equals("http://purl.org/dc/elements/1.1/");
        }));
    }


}
