package cz.inovatika.kramerius.services.workers.config;

import cz.incad.kramerius.utils.XMLUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class WorkerConfigurationTest {


    @Test
    public void testWorkerConfig1() throws ParserConfigurationException, IOException, SAXException {
        InputStream resourceAsStream = WorkerConfigurationTest.class.getResourceAsStream("config1.xml");
        Document document = XMLUtils.parseDocument(resourceAsStream);
        WorkerConfig workerConfig =  WorkerConfigParser.parse(XMLUtils.findElement(document.getDocumentElement(),"worker"));
        Assert.assertNotNull(workerConfig);
        Assert.assertNotNull(workerConfig.getDestinationConfig());
        Assert.assertNotNull(workerConfig.getRequestConfig());
        Assert.assertNotNull(workerConfig.getFactoryClz());

        String fieldList = workerConfig.getRequestConfig().getFieldList();
        Assert.assertEquals(fieldList, "indexer_version pid root.pid model created modified indexed keywords.* geographic_* genres.* publishers.* publication_places.* authors authors.* titles.* title.* root.* own_parent.* own_pid_path own_model_path rels_ext_index.sort foster_parents.pids in_collections in_collections.direct level pid_paths date.* date_range_*  date.str part.* issue.*  page.* id_* count_* coords.* languages.* physical_locations.* shelf_locators accessibility has_tiles ds.* collection.* mdt ddt donator text_ocr licenses contains_licenses licenses_of_ancestors");

        Assert.assertEquals("$destination.url$/update", workerConfig.getDestinationConfig().getDestinationUrl());
        Assert.assertEquals("pid root.pid licenses contains_licenses licenses_of_ancestors titles.* collection.* in_collections in_collections.* title.* titles.* text_ocr", workerConfig.getDestinationConfig().getOnUpdateFieldList());

        Assert.assertTrue(workerConfig.getDestinationConfig().getOnIndexEventRemoveElms().size() == 1);
        Assert.assertTrue(workerConfig.getDestinationConfig().getOnIndexEventUpdateElms().size() == 2);

        Assert.assertTrue(workerConfig.getRequestConfig().getBatchSize() == 20);

    }

    @Test
    public void testWorkerConfig2() throws ParserConfigurationException, IOException, SAXException {
        InputStream resourceAsStream = WorkerConfigurationTest.class.getResourceAsStream("config2.xml");
        Document document = XMLUtils.parseDocument(resourceAsStream);
        WorkerConfig workerConfig =  WorkerConfigParser.parse(XMLUtils.findElement(document.getDocumentElement(),"worker"));
        Assert.assertNotNull(workerConfig);
        Assert.assertNotNull(workerConfig.getDestinationConfig());
        Assert.assertNotNull(workerConfig.getRequestConfig());
        Assert.assertNotNull(workerConfig.getFactoryClz());

        String fieldList = workerConfig.getRequestConfig().getFieldList();
        Assert.assertEquals(fieldList, "indexer_version pid model created modified indexed keywords.* geographic_* genres.* publishers.* publication_places.* authors authors.* title.* root.* own_parent.* own_pid_path own_model_path rels_ext_index.sort foster_parents.pids in_collections in_collections.direct level pid_paths date.* date_range_*  date.str part.* issue.*  page.* id_* count_* coords.* languages.* physical_locations.* shelf_locators accessibility has_tiles ds.* collection.* mdt ddt donator text_ocr licenses contains_licenses licenses_of_ancestors");

        Assert.assertEquals("$destination.url$/update", workerConfig.getDestinationConfig().getDestinationUrl());
        Assert.assertEquals("pid root.pid licenses contains_licenses licenses_of_ancestors", workerConfig.getDestinationConfig().getOnUpdateFieldList());

        Assert.assertTrue(workerConfig.getDestinationConfig().getOnIndexEventRemoveElms().size() == 1);
        Assert.assertTrue(workerConfig.getDestinationConfig().getOnIndexEventUpdateElms().size() == 2);

    }
}
