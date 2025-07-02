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
package cz.incad.kramerius.rest.oai;

import cz.incad.kramerius.rest.oai.record.OAIRecordSupplement;
import cz.incad.kramerius.rest.oai.record.SupplementType;
import cz.incad.kramerius.utils.XMLUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class OAIRecorFactoryTest {

    @Test
    public void testOAIRecord() throws ParserConfigurationException, IOException, SAXException {
        InputStream is = OAIRecorFactoryTest.class.getResourceAsStream("solr.page.xml");
        Assert.assertNotNull(is);
        Document xmlparsedDoc = XMLUtils.parseDocument(is);
        Element doc = XMLUtils.findElement(xmlparsedDoc.getDocumentElement(), (elm) -> {
            return elm.getNodeName().equals("doc");
        });

        OAIRecord oaiRec = OAIRecordFactory.createRecord("localhost", doc, null, null);

        //<?xml version="1.0" encoding="UTF-8"?><response><lst name="responseHeader">  <bool name="zkConnected">true</bool>  <int name="status">0</int>  <int name="QTime">11</int>  <lst name="params">    <str name="q">model:page</str>    <str name="fq"/>    <str name="rows">1</str>    <arr name="wt">      <str>xml</str>      <str>xml</str>    </arr>  </lst></lst><result maxScore="0.023735158" name="response" numFound="13085991" start="0">  <doc>    <str name="pid">uuid:2abf3eaa-f327-44fa-8cb8-873ce3ecafa7</str>    <date name="created">2017-09-01T10:54:11.441Z</date>    <str name="date.str">2017</str>    <str name="title.search">239</str>    <arr name="titles.search">      <str>239</str>    </arr>    <arr name="ddt">      <str/>    </arr>    <str name="accessibility">private</str>    <str name="model">page</str>    <str name="ds.img_full.mime">image/jpeg</str>    <int name="level">1</int>    <date name="modified">2017-09-29T11:27:02.597Z</date>    <date name="indexed">2017-09-29T11:27:02.597Z</date>    <arr name="foster_parents.pids">      <str>uuid:7df204f2-731c-4e3e-9789-747718c598e3</str>    </arr>    <arr name="pid_paths">      <str>uuid:7df204f2-731c-4e3e-9789-747718c598e3/uuid:2abf3eaa-f327-44fa-8cb8-873ce3ecafa7</str>    </arr>    <str name="root.pid">uuid:7df204f2-731c-4e3e-9789-747718c598e3</str>    <str name="root.title">Neočekávané chování: příběh behaviorální ekonomie</str>        <str name="title.sort">239</str>    <str name="root.title.sort">NEOC|EKAVANE H|OVANI  PR|IBEH BEHAVIORALNI EKONOMIE</str>    <str name="own_model_path">monograph/page</str>    <str name="own_parent.pid">uuid:7df204f2-731c-4e3e-9789-747718c598e3</str>    <str name="own_pid_path">uuid:7df204f2-731c-4e3e-9789-747718c598e3/uuid:2abf3eaa-f327-44fa-8cb8-873ce3ecafa7</str>    <int name="rels_ext_index.sort">242</int>    <date name="date.min">2017-01-01T00:00:00.001Z</date>    <date name="date.max">2017-01-01T00:00:00.001Z</date>    <int name="date_range_start.year">2017</int>    <int name="date_range_end.year">2017</int>    <arr name="licenses_of_ancestors">      <str>onsite</str>    </arr>    <arr name="cdk.k5.license.translated">      <str>onsite</str>    </arr>    <str name="compositeId">uuid:7df204f2-731c-4e3e-9789-747718c598e3!uuid:2abf3eaa-f327-44fa-8cb8-873ce3ecafa7</str>    <arr name="cdk.licenses_of_ancestors">      <str>knav_onsite</str>    </arr>    <arr name="cdk.collection">          <str>knav</str></arr>    <str name="cdk.leader">knav</str>    <long name="_version_">1793326640172892160</long><bool name="cdk.collection.sorted">false</bool></doc></result></response>
        Assert.assertEquals(oaiRec.getSolrIdentifier(), "uuid:2abf3eaa-f327-44fa-8cb8-873ce3ecafa7");
        Assert.assertEquals(oaiRec.getIdentifier(), "oai:localhost:uuid:2abf3eaa-f327-44fa-8cb8-873ce3ecafa7");

        List<OAIRecordSupplement> supplements = oaiRec.getSupplements();

        Optional<OAIRecordSupplement> found = supplements.stream().filter(s -> {
            return s.supplementType() == SupplementType.ROOT_PID;
        }).findAny();

        Assert.assertTrue(found.isPresent());
        Assert.assertEquals(found.get().data(), "uuid:7df204f2-731c-4e3e-9789-747718c598e3");

        found = supplements.stream().filter(s -> {
            return s.supplementType() == SupplementType.OWN_PARENT_PID;
        }).findAny();

        Assert.assertTrue(found.isPresent());
        Assert.assertEquals(found.get().data(), "uuid:7df204f2-731c-4e3e-9789-747718c598e3");

        found = supplements.stream().filter(s -> {
            return s.supplementType() == SupplementType.REPRESENTATIVE_PAGE_PID;
        }).findAny();

        Assert.assertTrue(found.isPresent());
        Assert.assertEquals(found.get().data(), "uuid:2abf3eaa-f327-44fa-8cb8-873ce3ecafa7");

        found = supplements.stream().filter(s -> {
            return s.supplementType() == SupplementType.REPRESENTATIVE_PAGE_MIME_TYPE;
        }).findAny();

        Assert.assertTrue(found.isPresent());
        Assert.assertEquals(found.get().data(), "image/jpeg");

    }
}
