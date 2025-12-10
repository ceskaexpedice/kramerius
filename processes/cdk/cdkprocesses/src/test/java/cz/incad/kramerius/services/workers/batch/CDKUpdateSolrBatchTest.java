package cz.incad.kramerius.services.workers.batch;

import cz.incad.kramerius.services.workers.copy.cdk.CDKCopyContext;
import cz.incad.kramerius.services.workers.copy.cdk.model.CDKWorkerIndexedItem;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.config.ProcessConfigParser;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class CDKUpdateSolrBatchTest {

    @Test
    public void testBatchInsertSolr() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        InputStream conf = CDKUpdateSolrBatchTest.class.getResourceAsStream("migrate.conf");
        Document parsedConf = XMLUtils.parseDocument(conf);
        ProcessConfig config = ProcessConfigParser.parse(parsedConf.getDocumentElement());
        Assert.assertNotNull(config);

        InputStream solrData = CDKUpdateSolrBatchTest.class.getResourceAsStream("solr-source-insert.xml");
        Assert.assertNotNull(solrData);
        Document parsedSolrData = XMLUtils.parseDocument(solrData);
        Element result = XMLUtils.findElement(parsedSolrData.getDocumentElement(), (elm) -> {
            if (elm.getNodeName().equals("result")) {
                return true;
            } else return false;
        });

        CDKUpdateSolrBatchCreator updateSolrBatch = new CDKUpdateSolrBatchCreator(null, config,result);
        Document batch = updateSolrBatch.createBatchForInsert();


        Assert.assertEquals("add", batch.getDocumentElement().getNodeName());
        Assert.assertEquals(1, XMLUtils.getElements( batch.getDocumentElement()).size());

        // compositeId
        List<Element> compositeIdElm = XMLUtils.getElementsRecursive(batch.getDocumentElement(), (e) -> {
            String name = e.getAttribute("name");
            boolean cLicensesFound = name.equals("compositeId");
            boolean f = e.getNodeName().equals("field");
            return f && cLicensesFound ;
        });

        // contains license
        List<Element> cdkContainsLicenses = XMLUtils.getElementsRecursive(batch.getDocumentElement(), (e) -> {
            String name = e.getAttribute("name");
            boolean cLicensesFound = name.equals("cdk.contains_licenses");
            boolean f = e.getNodeName().equals("field");
            return f && cLicensesFound ;
        });

        List<Element> cdkLeader = XMLUtils.getElementsRecursive(batch.getDocumentElement(), (e) -> {
            String name = e.getAttribute("name");
            boolean cLeaderFound = name.equals("cdk.leader");
            boolean f = e.getNodeName().equals("field");
            return f &&  cLeaderFound;
        });

        List<Element> cdkCollection = XMLUtils.getElementsRecursive(batch.getDocumentElement(), (e) -> {
            String name = e.getAttribute("name");
            boolean cCollectionFound = name.equals("cdk.collection");
            boolean f = e.getNodeName().equals("field");
            return f &&  cCollectionFound;
        });

        List<Element> cdkHasTiles = XMLUtils.getElementsRecursive(batch.getDocumentElement(), (e) -> {
            String name = e.getAttribute("name");
            boolean cCollectionFound = name.equals("cdk.has_tiles_knav");
            boolean f = e.getNodeName().equals("field");
            return f &&  cCollectionFound;
        });

        List<Element> cdkHasMonographUnit = XMLUtils.getElementsRecursive(batch.getDocumentElement(), (e) -> {
            String name = e.getAttribute("name");
            boolean cCollectionFound = name.equals("cdk.count_monograph_unit_knav");
            boolean f = e.getNodeName().equals("field");
            return f &&  cCollectionFound;
        });

        Assert.assertTrue(!compositeIdElm.isEmpty());
        Assert.assertTrue(compositeIdElm.size() == 1);

        Assert.assertTrue(!cdkContainsLicenses.isEmpty());
        Assert.assertTrue(!cdkLeader.isEmpty());
        Assert.assertTrue(!cdkCollection.isEmpty());
        Assert.assertTrue(!cdkHasTiles.isEmpty());
        Assert.assertTrue(!cdkHasMonographUnit.isEmpty());
    }


    @Test
    public void testBatchCloudInsertSolr() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        InputStream conf = CDKUpdateSolrBatchTest.class.getResourceAsStream("migrate.conf");
        Document parsedConf = XMLUtils.parseDocument(conf);
        ProcessConfig config = ProcessConfigParser.parse(parsedConf.getDocumentElement());
        Assert.assertNotNull(config);

        InputStream solrData = CDKUpdateSolrBatchTest.class.getResourceAsStream("solr-cloud-source-insert.xml");
        Assert.assertNotNull(solrData);
        Document parsedSolrData = XMLUtils.parseDocument(solrData);
        Element result = XMLUtils.findElement(parsedSolrData.getDocumentElement(), (elm) -> {
            if (elm.getNodeName().equals("result")) {
                return true;
            } else return false;
        });

        CDKUpdateSolrBatchCreator updateSolrBatch = new CDKUpdateSolrBatchCreator(null, config,result);
        Document batch = updateSolrBatch.createBatchForInsert();

        Assert.assertEquals("add", batch.getDocumentElement().getNodeName());
        Assert.assertEquals(1, XMLUtils.getElements( batch.getDocumentElement()).size());

        // compositeId
        List<Element> compositeIdElm = XMLUtils.getElementsRecursive(batch.getDocumentElement(), (e) -> {
            String name = e.getAttribute("name");
            boolean cLicensesFound = name.equals("compositeId");
            boolean f = e.getNodeName().equals("field");
            return f && cLicensesFound ;
        });

        Assert.assertTrue(!compositeIdElm.isEmpty());
        Assert.assertTrue(compositeIdElm.size() == 1);

    }

    @Test
    public void testBatchUpdateSolr() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        InputStream conf = CDKUpdateSolrBatchTest.class.getResourceAsStream("migrate.conf");
        Document parsedConf = XMLUtils.parseDocument(conf);
        ProcessConfig config = ProcessConfigParser.parse(parsedConf.getDocumentElement());
        Assert.assertNotNull(config);

        InputStream solrData = CDKUpdateSolrBatchTest.class.getResourceAsStream("solr-source-update.xml");
        Assert.assertNotNull(solrData);
        Document parsedSolrData = XMLUtils.parseDocument(solrData);
        Element result = XMLUtils.findElement(parsedSolrData.getDocumentElement(), (elm) -> {
            if (elm.getNodeName().equals("result")) {
                return true;
            } else return false;
        });

        // iteration doc
        Map<String, Object> iteratedDoc = new HashMap<>();
        iteratedDoc.put("pid", "uuid:50be5fb2-0694-47b4-be28-e6e189d9a6cf");
        IterationItem item = new IterationItem("uuid:50be5fb2-0694-47b4-be28-e6e189d9a6cf","nkp", iteratedDoc);

        // cdk doc
        Map<String, Object> cdkDoc = new HashMap<>();
        cdkDoc.put("pid", "uuid:50be5fb2-0694-47b4-be28-e6e189d9a6cf");
        cdkDoc.put("root.pid", "uuid:50be5fb2-0694-47b4-be28-e6e189d9a6cf");
        cdkDoc.put("compositeId", "uuid:50be5fb2-0694-47b4-be28-e6e189d9a6cf!uuid:50be5fb2-0694-47b4-be28-e6e189d9a6cf");
        cdkDoc.put("contains_licenses", Arrays.asList("onsite"));
        cdkDoc.put("cdk.contains_licenses", Arrays.asList("nkp_onsite"));
        cdkDoc.put("cdk.collections", Arrays.asList("nkp"));


        CDKWorkerIndexedItem indexedItem = new CDKWorkerIndexedItem("uuid:50be5fb2-0694-47b4-be28-e6e189d9a6cf!uuid:50be5fb2-0694-47b4-be28-e6e189d9a6cf", cdkDoc);
        CDKCopyContext ctx = new CDKCopyContext(Arrays.asList(item), Arrays.asList(indexedItem), new ArrayList<>(), new ArrayList<>());
        CDKUpdateSolrBatchCreator updateSolrBatch = new CDKUpdateSolrBatchCreator(ctx, config,result);
        Document batch = updateSolrBatch.createBatchForUpdate();


        List<Element> allNoUpdateFields = XMLUtils.getElementsRecursive(batch.getDocumentElement(), (e) -> {
            String update = e.getAttribute("update");
            boolean f = e.getNodeName().equals("field");
            return f && !StringUtils.isAnyString(update);
        });
        Set<String> allNoUpdateFieldsNames = new HashSet<>();
        allNoUpdateFields.stream().forEach(elm -> {
            String name = elm.getAttribute("name");
            allNoUpdateFieldsNames.add(name);
        });
        Assert.assertTrue(allNoUpdateFieldsNames.size() == 1);
        Assert.assertTrue(allNoUpdateFieldsNames.iterator().next().equals("compositeId"));

        // contains license
        List<Element> cdkContainsLicensesElm = XMLUtils.getElementsRecursive(batch.getDocumentElement(), (e) -> {
            String name = e.getAttribute("name");
            boolean cLicensesFound = name.equals("cdk.contains_licenses");
            boolean f = e.getNodeName().equals("field");
            return f && cLicensesFound ;
        });
        Assert.assertTrue(!cdkContainsLicensesElm.isEmpty());
        Assert.assertTrue(cdkContainsLicensesElm.size() == 2);
        List<String> cdkContainsLicenses =  cdkContainsLicensesElm.stream().map(Element::getTextContent).collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList("knav_public","nkp_onsite"), cdkContainsLicenses);


        List<Element> containsLicensesElm = XMLUtils.getElementsRecursive(batch.getDocumentElement(), (e) -> {
            String name = e.getAttribute("name");
            boolean cLicensesFound = name.equals("contains_licenses");
            boolean f = e.getNodeName().equals("field");
            return f && cLicensesFound ;
        });
        List<String> collectedLicenses = containsLicensesElm.stream().map(Element::getTextContent).collect(Collectors.toList());
        Assert.assertTrue(!collectedLicenses.isEmpty());
        Assert.assertTrue(collectedLicenses.size() == 2);
        Assert.assertEquals(Arrays.asList("public", "onsite"), collectedLicenses);



        List<Element> cdkLeader = XMLUtils.getElementsRecursive(batch.getDocumentElement(), (e) -> {
            String name = e.getAttribute("name");
            boolean cLeaderFound = name.equals("cdk.leader");
            boolean f = e.getNodeName().equals("field");
            return f &&  cLeaderFound;
        });

        Assert.assertTrue(cdkLeader.isEmpty());

        List<Element> cdkCollection = XMLUtils.getElementsRecursive(batch.getDocumentElement(), (e) -> {
            String name = e.getAttribute("name");
            boolean cCollectionFound = name.equals("cdk.collection");
            boolean f = e.getNodeName().equals("field");
            return f &&  cCollectionFound;
        });

        Assert.assertTrue(!cdkCollection.isEmpty());
        Assert.assertTrue(cdkCollection.size() == 1);
        Assert.assertEquals(Arrays.asList("knav"), cdkCollection.stream().map(Element::getTextContent).collect(Collectors.toList()));


        List<Element> cdkHasTiles = XMLUtils.getElementsRecursive(batch.getDocumentElement(), (e) -> {
            String name = e.getAttribute("name");
            boolean cCollectionFound = name.equals("cdk.has_tiles_knav");
            boolean f = e.getNodeName().equals("field");
            return f &&  cCollectionFound;
        });
        Assert.assertTrue(!cdkHasTiles.isEmpty());
        Assert.assertTrue(cdkHasTiles.size() == 1);

        List<Element> cdkHasMonographUnit = XMLUtils.getElementsRecursive(batch.getDocumentElement(), (e) -> {
            String name = e.getAttribute("name");
            boolean cCollectionFound = name.equals("cdk.count_monograph_unit_knav");
            boolean f = e.getNodeName().equals("field");
            return f &&  cCollectionFound;
        });

        Assert.assertTrue(!cdkHasMonographUnit.isEmpty());
        Assert.assertTrue(cdkHasMonographUnit.size() == 1);
    }
}
