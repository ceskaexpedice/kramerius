package cz.incad.kramerius.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Ignore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import junit.framework.TestCase;

public class SolrMigrationThreadsTest extends TestCase {

    
    public void testBatch1() throws ParserConfigurationException, SAXException, IOException, TransformerException, MigrateSolrIndexException {
        KConfiguration.getInstance().getConfiguration().setProperty(".migration.build.composite", true);
        
    	InputStream resourceAsStream = SolrMigrationThreadsTest.class.getResourceAsStream("solr.xml");
        
        Document parsed = XMLUtils.parseDocument(resourceAsStream);
        Element result = XMLUtils.findElement(parsed.getDocumentElement(), "result");
        
        List<Document> batches = BatchUtils.batches(result, 1);
        Assert.assertTrue(batches.size() == 10);

        Set<String> pids = new HashSet<String>();
        for (Document document : batches) {
            Element add =document.getDocumentElement();
            List<Element> docs = XMLUtils.getElements(add);
            Assert.assertTrue(docs.size() == 1);
            for (Element d : docs) {
                Element pidElm = XMLUtils.findElement(d, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element elm) {
                        if (elm.getNodeName().equals("field")) {
                            return elm.getAttribute("name").equals("PID");
                        }
                        return false;
                    }
                });
                Element compositeIdElm = XMLUtils.findElement(d, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element elm) {
                        if (elm.getNodeName().equals("field")) {
                            return elm.getAttribute("name").equals("compositeId");
                        }
                        return false;
                    }
                });

                Element browseAutorElm = XMLUtils.findElement(d, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element elm) {
                        if (elm.getNodeName().equals("field")) {
                            return elm.getAttribute("name").equals("browse_autor");
                        }
                        return false;
                    }
                });
                Element browseTitle = XMLUtils.findElement(d, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element elm) {
                        if (elm.getNodeName().equals("field")) {
                            return elm.getAttribute("name").equals("browse_title");
                        }
                        return false;
                    }
                });
                
                Assert.assertNotNull(pidElm);
                Assert.assertNotNull(compositeIdElm);
                pids.add(pidElm.getTextContent());
                if (pidElm.getTextContent().equals("uuid:2ba4984c-a8d9-4486-bd0e-d43d390b0723")) {
                    Assert.assertNotNull(browseAutorElm);
                    Assert.assertNotNull(browseTitle);
                }
            }
        }
        
            
        Assert.assertTrue(pids.size() == 10);
    }

    public void testBatch1_ChangedCompsoiteIdName() throws ParserConfigurationException, SAXException, IOException, TransformerException, MigrateSolrIndexException {
        KConfiguration.getInstance().getConfiguration().setProperty(".migration.build.composite", true);

    	System.setProperty("compositeId.field.name", "routingField");
        InputStream resourceAsStream = SolrMigrationThreadsTest.class.getResourceAsStream("solr.xml");
        
        Document parsed = XMLUtils.parseDocument(resourceAsStream);
        Element result = XMLUtils.findElement(parsed.getDocumentElement(), "result");
        
        List<Document> batches = BatchUtils.batches(result, 1);
        Assert.assertTrue(batches.size() == 10);

        Set<String> pids = new HashSet<String>();
        for (Document document : batches) {
            Element add =document.getDocumentElement();
            List<Element> docs = XMLUtils.getElements(add);
            Assert.assertTrue(docs.size() == 1);
            for (Element d : docs) {
                Element pidElm = XMLUtils.findElement(d, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element elm) {
                        if (elm.getNodeName().equals("field")) {
                            return elm.getAttribute("name").equals("PID");
                        }
                        return false;
                    }
                });
                Element oldCompositeIdElm = XMLUtils.findElement(d, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element elm) {
                        if (elm.getNodeName().equals("field")) {
                            return elm.getAttribute("name").equals("compositeId");
                        }
                        return false;
                    }
                });
                Element newCompositeIdElm = XMLUtils.findElement(d, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element elm) {
                        if (elm.getNodeName().equals("field")) {
                            return elm.getAttribute("name").equals("routingField");
                        }
                        return false;
                    }
                });
                Assert.assertNotNull(pidElm);
                Assert.assertNull(oldCompositeIdElm);
                Assert.assertNotNull(newCompositeIdElm);
                pids.add(pidElm.getTextContent());
            }
        }
        
        Assert.assertTrue(pids.size() == 10);
        System.getProperties().remove("compositeId.field.name");
    }

    public void testBatch2() throws ParserConfigurationException, SAXException, IOException, TransformerException, MigrateSolrIndexException {
        KConfiguration.getInstance().getConfiguration().setProperty(".migration.build.composite", true);

    	InputStream resourceAsStream = SolrMigrationThreadsTest.class.getResourceAsStream("solr.xml");
        
        Document parsed = XMLUtils.parseDocument(resourceAsStream);
        Element result = XMLUtils.findElement(parsed.getDocumentElement(), "result");
        
        List<Document> batches = BatchUtils.batches(result, 2);
        Assert.assertTrue(batches.size() == 5);

        Set<String> pids = new HashSet<String>();
        for (Document document : batches) {
 
            Element add =document.getDocumentElement();
            List<Element> docs = XMLUtils.getElements(add);
            Assert.assertTrue(docs.size() == 2);
            for (Element d : docs) {
                Element pidElm = XMLUtils.findElement(d, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element elm) {
                        if (elm.getNodeName().equals("field")) {
                            return elm.getAttribute("name").equals("PID");
                        }
                        return false;
                    }
                });
                Assert.assertNotNull(pidElm);
                pids.add(pidElm.getTextContent());
            }
 
        }
        
        Assert.assertTrue(pids.size() == 10);
    }

    public void testBatch3() throws ParserConfigurationException, SAXException, IOException, TransformerException, MigrateSolrIndexException {
        KConfiguration.getInstance().getConfiguration().setProperty(".migration.build.composite", true);

    	InputStream resourceAsStream = SolrMigrationThreadsTest.class.getResourceAsStream("solr.xml");
        
        Document parsed = XMLUtils.parseDocument(resourceAsStream);
        Element result = XMLUtils.findElement(parsed.getDocumentElement(), "result");
        
        List<Document> batches = BatchUtils.batches(result, 3);
        Assert.assertTrue(batches.size() == 4);

        Set<String> pids = new HashSet<String>();
        for (int i = 0; i < batches.size(); i++) {
            Document document = batches.get(i);
            Element add =document.getDocumentElement();
            List<Element> docs = XMLUtils.getElements(add);
            if (i == batches.size() - 1 ) {
                Assert.assertTrue(docs.size() == 1);
            } else {
                Assert.assertTrue(docs.size() == 3);
            }
            
            for (Element d : docs) {
                Element pidElm = XMLUtils.findElement(d, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element elm) {
                        if (elm.getNodeName().equals("field")) {
                            return elm.getAttribute("name").equals("PID");
                        }
                        return false;
                    }
                });
                Assert.assertNotNull(pidElm);
                pids.add(pidElm.getTextContent());
            }
        }
        
        Assert.assertTrue(pids.size() == 10);
    }


//    @Ignore
//    public void testConstructQuery() throws MigrateSolrIndexException {
//    	KConfiguration.getInstance().getConfiguration().setProperty(".migration.solr.query", "*:*");
//        String url =MigrationUtils.queryBaseURL();
//        Assert.assertTrue(url.endsWith("select?q=*%3A*&fl=PID+timestamp+fedora.model+document_type+handle+status+created_date+modified_date+parent_model+parent_pid+parent_pid+parent_title+root_model+root_pid+root_title+text_ocr+pages_count+datum_str+datum+rok+datum_begin+datum_end+datum_page+issn+mdt+ddt+dostupnost+keywords+geographic_names+collection+sec+model_path+pid_path+rels_ext_index+level+dc.title+title_sort+title_sort+dc.creator+dc.identifier+language+dc.description+details+facet_title+browse_title+browse_autor+img_full_mime+viewable+virtual+location+range+mods.shelfLocator+mods.physicalLocation+text&sort=modified_date+asc"));
//        KConfiguration.getInstance().getConfiguration().setProperty(".migration.solr.query", "*:* AND parent_pid:uuid\\:xxxx");
//        KConfiguration.getInstance().getConfiguration().setProperty(".migration.solr.fieldlist", "*:* AND parent_pid:uuid\\:xxxx&fl=PID");
//        url =MigrationUtils.queryBaseURL();
//        Assert.assertTrue(url.endsWith("select?q=*%3A*+AND+parent_pid%3Auuid%5C%3Axxxx&fl=*%3A*+AND+parent_pid%3Auuid%5C%3Axxxx%26fl%3DPID&sort=modified_date+asc"));
//    }
    
    
}
