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

import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.workers.replicate.BatchUtils;
import cz.incad.kramerius.services.workers.replicate.CDKReplicateContext;
import cz.incad.kramerius.services.workers.replicate.copy.CopyReplicateConsumer;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BatchUtilsTest {

    @Test
    public void testBatchUtilsV5() throws ParserConfigurationException, IOException, SAXException, TransformerException, MigrateSolrIndexException {
        SourceToDestTransform format = SourceToDestTransform.Format.findTransform("K7");

        Assert.assertNotNull(format);
        InputStream v5 = SourceToDestTransformTest.class.getResourceAsStream("v5.xml");
        Assert.assertNotNull(v5);

        Document source = XMLUtils.parseDocument(v5, false);
        Element sourceResultElm = XMLUtils.findElement(source.getDocumentElement(), "result");

        CDKReplicateContext context = new CDKReplicateContext(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        Document batch = BatchUtils.batch(context, sourceResultElm, true, "root.pid", "pid", format, null);
        XMLUtils.print(batch, System.out);

        Assert.assertNotNull(batch);
        Assert.assertEquals(batch.getDocumentElement().getNodeName(), "add");
        List<Element> elms = XMLUtils.getElementsRecursive(batch.getDocumentElement(), elm -> {
            if (elm.getNodeName().equals("doc")) return true;
            else return false;
        });
        Assert.assertTrue(elms.size() == 20);

        List<String> expectingPids = Arrays.asList("uuid:000059eb-d782-4285-857a-41b370dc1afd", "uuid:00003962-b232-4055-9a77-8d8db9487b90", "uuid:00000834-c06c-49ed-91e9-d54a0f8c8571", "uuid:000026fa-3695-4af4-aaaa-50557d8c2c6d", "uuid:00007e8c-3f3a-4f73-ab70-0f95b1024f59", "uuid:00007ca2-054e-4fde-b73c-81f55a282ebc", "uuid:0000329c-2940-89be-717b-82cac72948a8", "uuid:000047d7-3b67-4f08-a7cf-e843db917097", "uuid:000040ee-eda5-49d3-ae14-510a74921cda", "uuid:0000a0c8-41b2-4fdd-9597-619b3a7ff965", "uuid:000091fb-dc07-4594-9300-8a503dc09210", "uuid:00006174-e3da-488b-9a18-eef7b3d5e527", "uuid:00006bd9-100d-4c89-97e6-3529e99c45be", "uuid:00001fe4-1d09-42e2-845a-0201dd900e87", "uuid:0000211b-7c66-4730-b343-8ea2ef427f99", "uuid:00006e78-04c4-4fa0-9a9d-d7ba78f7c341", "uuid:0000338c-4dd1-4d11-bda5-4c1b44d83628", "uuid:0000338c-4dd1-4d11-bda5-4c1b44d83628@1", "uuid:0000338c-4dd1-4d11-bda5-4c1b44d83628@2", "uuid:00003bb1-429d-11e2-849c-005056a60003");

        elms.forEach(batchDoc -> {

            Element pidElm = XMLUtils.findElement(batchDoc, (field) -> {
                String name = field.getAttribute("name");
                return name.equals("pid");
            });
            Assert.assertNotNull(pidElm);
            Assert.assertTrue(expectingPids.contains(pidElm.getTextContent()));


            Element modelElm = XMLUtils.findElement(batchDoc, (field) -> {
                String name = field.getAttribute("name");
                return name.equals("model");
            });
            Assert.assertNotNull(modelElm);

            Element createdElm = XMLUtils.findElement(batchDoc, (field) -> {
                String name = field.getAttribute("name");
                return name.equals("created");
            });
            Assert.assertNotNull(createdElm);

            Element modifiedElm = XMLUtils.findElement(batchDoc, (field) -> {
                String name = field.getAttribute("name");
                return name.equals("modified");
            });
            Assert.assertNotNull(modifiedElm);

            Element indexedElm = XMLUtils.findElement(batchDoc, (field) -> {
                String name = field.getAttribute("name");
                return name.equals("indexed");
            });
            Assert.assertNotNull(indexedElm);
        });
    }


    @Test
    public void testBatchUtilsV5Consumer() throws ParserConfigurationException, IOException, SAXException, TransformerException, MigrateSolrIndexException {
        SourceToDestTransform format = SourceToDestTransform.Format.findTransform("K7");

        Assert.assertNotNull(format);
        InputStream v5 = SourceToDestTransformTest.class.getResourceAsStream("v5.xml");
        Assert.assertNotNull(v5);

        Document source = XMLUtils.parseDocument(v5, false);
        Element sourceResultElm = XMLUtils.findElement(source.getDocumentElement(), "result");

        CDKReplicateContext context = new CDKReplicateContext(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        Document batch = BatchUtils.batch(context, sourceResultElm, true, "root.pid", "pid", format, new CopyReplicateConsumer() {
            @Override
            public ModifyFieldResult modifyField(Element field) {
                System.out.println("\tModify element "+field.getAttribute("name"));
                return ModifyFieldResult.none;
            }

            @Override
            public void changeDocument(String rootPid, String pid, Element doc) {
                System.out.println("Change whole document");
            }
        });
    }

    @Test
    public void testBatchUtilsV7() throws ParserConfigurationException, IOException, SAXException, TransformerException, MigrateSolrIndexException {
        SourceToDestTransform format = SourceToDestTransform.Format.findTransform("COPY");

        Assert.assertNotNull(format);
        InputStream v7 = SourceToDestTransformTest.class.getResourceAsStream("v7.xml");
        Assert.assertNotNull(v7);

        Document source = XMLUtils.parseDocument(v7, false);
        Element sourceResultElm = XMLUtils.findElement(source.getDocumentElement(), "result");

        CDKReplicateContext context = new CDKReplicateContext(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        Document batch = BatchUtils.batch(context, sourceResultElm, true, "root.pid", "pid", format, null);
        XMLUtils.print(batch, System.out);

        Assert.assertNotNull(batch);
        Assert.assertEquals(batch.getDocumentElement().getNodeName(), "add");
        List<Element> elms = XMLUtils.getElementsRecursive(batch.getDocumentElement(), elm -> {
            if (elm.getNodeName().equals("doc")) return true;
            else return false;
        });

        Assert.assertTrue(elms.size() == 10);

        List<String> expectingPids = Arrays.asList("uuid:3f29741c-3921-11ef-a7a7-001b63bd97ba","uuid:3f29741d-3921-11ef-a7a7-001b63bd97ba","uuid:3f29741e-3921-11ef-a7a7-001b63bd97ba","uuid:3f299b2f-3921-11ef-a7a7-001b63bd97ba","uuid:3f299b30-3921-11ef-a7a7-001b63bd97ba","uuid:422f88b5-3921-11ef-a7a7-001b63bd97ba","uuid:422fafc6-3921-11ef-a7a7-001b63bd97ba","uuid:422fafc7-3921-11ef-a7a7-001b63bd97ba","uuid:422fafc8-3921-11ef-a7a7-001b63bd97ba","uuid:422fafc9-3921-11ef-a7a7-001b63bd97ba");

        elms.forEach(batchDoc -> {

            Element pidElm = XMLUtils.findElement(batchDoc, (field) -> {
                String name = field.getAttribute("name");
                return name.equals("pid");
            });
            Assert.assertNotNull(pidElm);
            Assert.assertTrue(expectingPids.contains(pidElm.getTextContent()));


            Element modelElm = XMLUtils.findElement(batchDoc, (field) -> {
                String name = field.getAttribute("name");
                return name.equals("model");
            });
            Assert.assertNotNull(modelElm);

            Element createdElm = XMLUtils.findElement(batchDoc, (field) -> {
                String name = field.getAttribute("name");
                return name.equals("created");
            });
            Assert.assertNotNull(createdElm);

            Element modifiedElm = XMLUtils.findElement(batchDoc, (field) -> {
                String name = field.getAttribute("name");
                return name.equals("modified");
            });
            Assert.assertNotNull(modifiedElm);

            Element indexedElm = XMLUtils.findElement(batchDoc, (field) -> {
                String name = field.getAttribute("name");
                return name.equals("indexed");
            });
            Assert.assertNotNull(indexedElm);
        });

    }
}