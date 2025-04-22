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
package cz.incad.kramerius.services.transform;

import cz.incad.kramerius.utils.XMLUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;

public class SourceToDestTransformTest {


    @Test
    public void testTransform_v5() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        SourceToDestTransform format = SourceToDestTransform.Format.findTransform("K7");

        Assert.assertNotNull(format);
        InputStream v5 = SourceToDestTransformTest.class.getResourceAsStream("v5.xml");
        Assert.assertNotNull(v5);

        Document source = XMLUtils.parseDocument(v5, false);
        Element sourceDoc = XMLUtils.findElement(source.getDocumentElement(), "doc");
        XMLUtils.print(sourceDoc, System.out);


        // parsed dest doc
        Document dest = XMLUtils.crateDocument("doc");
        Element destDoc = dest.getDocumentElement();
        format.transform(sourceDoc, dest, destDoc, null);
        XMLUtils.print(dest, System.out);

        // -- pid --
        Element srcPid = XMLUtils.findElement(sourceDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "PID".equals(name);
        });
        Assert.assertNotNull(srcPid);
        Assert.assertEquals("uuid:000059eb-d782-4285-857a-41b370dc1afd", srcPid.getTextContent().trim());

        Element destPid = XMLUtils.findElement(destDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "pid".equals(name);
        });
        Assert.assertNotNull(destPid);
        Assert.assertEquals("uuid:000059eb-d782-4285-857a-41b370dc1afd", destPid.getTextContent().trim());

        // -- model --
        Element srcModel = XMLUtils.findElement(sourceDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "fedora.model".equals(name);
        });
        Assert.assertNotNull(srcModel);
        Assert.assertEquals("page", srcModel.getTextContent().trim());

        Element destModel = XMLUtils.findElement(destDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "model".equals(name);
        });
        Assert.assertNotNull(destModel);
        Assert.assertEquals("page", destModel.getTextContent().trim());


        // -- created --
        Element srcCreated = XMLUtils.findElement(sourceDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "created_date".equals(name);
        });
        Assert.assertNotNull(srcCreated);
        Assert.assertEquals("2016-04-19T11:32:15.007Z", srcCreated.getTextContent().trim());

        Element destCreated = XMLUtils.findElement(destDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "created".equals(name);
        });
        Assert.assertNotNull(destModel);
        Assert.assertEquals("2016-04-19T11:32:15.007Z", destCreated.getTextContent().trim());


        // -- modified --
        Element srcModified = XMLUtils.findElement(sourceDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "modified_date".equals(name);
        });
        Assert.assertNotNull(srcModified);
        Assert.assertEquals("2019-03-20T12:18:27.311Z", srcModified.getTextContent().trim());

        Element destModified = XMLUtils.findElement(destDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "modified".equals(name);
        });
        Assert.assertNotNull(destModified);
        Assert.assertEquals("2019-03-20T12:18:27.311Z", destModified.getTextContent().trim());

        // -- translated license
        Element licenseOfAncestors = XMLUtils.findElement(destDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "licenses_of_ancestors".equals(name);
        });
        Assert.assertNotNull(licenseOfAncestors);
        Assert.assertEquals("onsite", licenseOfAncestors.getTextContent().trim());

        // -- translated license
        Element cdkTranslatedLicense = XMLUtils.findElement(destDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "cdk.k5.license.translated".equals(name);
        });
        Assert.assertNotNull(cdkTranslatedLicense);
        Assert.assertEquals("onsite", cdkTranslatedLicense.getTextContent().trim());
    }

    @Test
    public void testTransform_v7() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        SourceToDestTransform format = SourceToDestTransform.Format.findTransform("COPY");

        Assert.assertNotNull(format);
        InputStream v7 = SourceToDestTransformTest.class.getResourceAsStream("v7.xml");
        Assert.assertNotNull(v7);

        Document source = XMLUtils.parseDocument(v7, false);
        Element sourceDoc = XMLUtils.findElement(source.getDocumentElement(), "doc");


        // parsed dest doc
        Document dest = XMLUtils.crateDocument("doc");
        Element destDoc = dest.getDocumentElement();
        format.transform(sourceDoc, dest, destDoc, null);

        // -- pid --
        Element srcPid = XMLUtils.findElement(sourceDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "pid".equals(name);
        });
        Assert.assertNotNull(srcPid);
        Assert.assertEquals("uuid:3f29741c-3921-11ef-a7a7-001b63bd97ba", srcPid.getTextContent().trim());

        Element destPid = XMLUtils.findElement(destDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "pid".equals(name);
        });
        Assert.assertNotNull(destPid);
        Assert.assertEquals("uuid:3f29741c-3921-11ef-a7a7-001b63bd97ba", destPid.getTextContent().trim());

        // -- model --
        Element srcModel = XMLUtils.findElement(sourceDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "model".equals(name);
        });
        Assert.assertNotNull(srcModel);
        Assert.assertEquals("page", srcModel.getTextContent().trim());

        Element destModel = XMLUtils.findElement(destDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "model".equals(name);
        });
        Assert.assertNotNull(destModel);
        Assert.assertEquals("page", destModel.getTextContent().trim());


        // -- created --
        Element srcCreated = XMLUtils.findElement(sourceDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "created".equals(name);
        });
        Assert.assertNotNull(srcCreated);
        Assert.assertEquals("2024-07-03T09:47:22.920Z", srcCreated.getTextContent().trim());

        Element destCreated = XMLUtils.findElement(destDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "created".equals(name);
        });
        Assert.assertNotNull(destModel);
        Assert.assertEquals("2024-07-03T09:47:22.920Z", destCreated.getTextContent().trim());


        // -- modified --
        Element srcModified = XMLUtils.findElement(sourceDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "modified".equals(name);
        });
        Assert.assertNotNull(srcModified);
        Assert.assertEquals("2024-08-06T08:43:26.471Z", srcModified.getTextContent().trim());

        Element destModified = XMLUtils.findElement(destDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "modified".equals(name);
        });
        Assert.assertNotNull(destModified);
        Assert.assertEquals("2024-08-06T08:43:26.471Z", destModified.getTextContent().trim());

        // -- translated license
        Element licenseOfAncestors = XMLUtils.findElement(destDoc, (elm) -> {
            String name = elm.getAttribute("name");
            return "licenses_of_ancestors".equals(name);
        });
        Assert.assertNotNull(licenseOfAncestors);
        Assert.assertEquals("onsite", licenseOfAncestors.getTextContent().trim());
    }
}
