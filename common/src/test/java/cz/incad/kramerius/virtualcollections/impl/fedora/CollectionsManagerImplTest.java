package cz.incad.kramerius.virtualcollections.impl.fedora;

import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.Collection.Description;
import cz.incad.kramerius.virtualcollections.impl.fedora.FedoraCollectionsManagerImpl;
import junit.framework.TestCase;

public class CollectionsManagerImplTest extends TestCase {

    public void testVirtualCollections() throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);

        FedoraAccessImpl fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), acLog)
        .addMockedMethod("getDataStream")
        .addMockedMethod("isStreamAvailable")
        .addMockedMethod("getDC")
        .createMock();

        FedoraCollectionsManagerImpl col = createMockBuilder(FedoraCollectionsManagerImpl.class)
        .withConstructor()
        .addMockedMethod("getCollectionListFromResourceIndex")
        .addMockedMethod("languages")
        .createMock();

        SolrAccessImpl sa = createMockBuilder(SolrAccessImpl.class)
        		.withConstructor()
        		.addMockedMethod("request", new Class[]{String.class})
        		.createMock();

        
        InputStream stream = CollectionsManagerImplTest.class.getResourceAsStream("sparql.xml");
        Document document = XMLUtils.parseDocument(stream,true);
        EasyMock.expect(col.getCollectionListFromResourceIndex()).andReturn(document);
        EasyMock.expect(col.languages()).andReturn(Arrays.asList("cs","en")).anyTimes();

        EasyMock.expect(fa.getDC("vc:3d466a99-6dca-4113-87d9-831673bae580")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("3d466a99-6dca-4113-87d9-831673bae580.dc.xml"), true)).anyTimes();
        EasyMock.expect(fa.getDC("vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef.dc.xml"), true)).anyTimes();
        EasyMock.expect(fa.getDC("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("64b95a45-6ead-4bf1-aa93-c31b0ccbf646.dc.xml"), true)).anyTimes();
        EasyMock.expect(fa.getDC("vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("a9dd018c-32ed-474b-9ee5-071ebecfdef5.dc.xml"), true)).anyTimes();
        EasyMock.expect(fa.getDC("vc:25463364-b86b-4f2b-8fb3-598b55efa09f")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("25463364-b86b-4f2b-8fb3-598b55efa09f.dc.xml"), true)).anyTimes();

        EasyMock.expect(sa.request("fq=level:0&q=collection:(\"vc:3d466a99-6dca-4113-87d9-831673bae580\")&rows=0")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("solrresponse.xml"), true)).anyTimes();
        EasyMock.expect(sa.request("fq=level:0&q=collection:(\"vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef\")&rows=0")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("solrresponse.xml"), true)).anyTimes();
        EasyMock.expect(sa.request("fq=level:0&q=collection:(\"vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646\")&rows=0")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("solrresponse.xml"), true)).anyTimes();
        EasyMock.expect(sa.request("fq=level:0&q=collection:(\"vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5\")&rows=0")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("solrresponse.xml"), true)).anyTimes();
        EasyMock.expect(sa.request("fq=level:0&q=collection:(\"vc:25463364-b86b-4f2b-8fb3-598b55efa09f\")&rows=0")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("solrresponse.xml"), true)).anyTimes();

        
        EasyMock.expect(fa.isStreamAvailable("vc:3d466a99-6dca-4113-87d9-831673bae580", "TEXT_cs")).andReturn(true).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:3d466a99-6dca-4113-87d9-831673bae580", "TEXT_en")).andReturn(true).anyTimes();

        EasyMock.expect(fa.isStreamAvailable("vc:3d466a99-6dca-4113-87d9-831673bae580", "LONG_TEXT_cs")).andReturn(false).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:3d466a99-6dca-4113-87d9-831673bae580", "LONG_TEXT_en")).andReturn(false).anyTimes();

        EasyMock.expect(fa.isStreamAvailable("vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef", "TEXT_cs")).andReturn(true).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef", "TEXT_en")).andReturn(true).anyTimes();

        EasyMock.expect(fa.isStreamAvailable("vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef", "LONG_TEXT_cs")).andReturn(false).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef", "LONG_TEXT_en")).andReturn(false).anyTimes();

        EasyMock.expect(fa.isStreamAvailable("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646", "TEXT_cs")).andReturn(true).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646", "TEXT_en")).andReturn(true).anyTimes();

        EasyMock.expect(fa.isStreamAvailable("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646", "LONG_TEXT_cs")).andReturn(false).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646", "LONG_TEXT_en")).andReturn(false).anyTimes();

        
        EasyMock.expect(fa.isStreamAvailable("vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5", "TEXT_cs")).andReturn(true).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5", "TEXT_en")).andReturn(true).anyTimes();

        EasyMock.expect(fa.isStreamAvailable("vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5", "LONG_TEXT_cs")).andReturn(false).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5", "LONG_TEXT_en")).andReturn(false).anyTimes();

        EasyMock.expect(fa.isStreamAvailable("vc:25463364-b86b-4f2b-8fb3-598b55efa09f", "TEXT_cs")).andReturn(true).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:25463364-b86b-4f2b-8fb3-598b55efa09f", "TEXT_en")).andReturn(true).anyTimes();

        EasyMock.expect(fa.isStreamAvailable("vc:25463364-b86b-4f2b-8fb3-598b55efa09f", "LONG_TEXT_cs")).andReturn(false).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:25463364-b86b-4f2b-8fb3-598b55efa09f", "LONG_TEXT_en")).andReturn(false).anyTimes();
        
        
        EasyMock.expect(fa.getDataStream("vc:3d466a99-6dca-4113-87d9-831673bae580", "TEXT_cs")).andReturn(new ByteArrayInputStream("cesky text".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef", "TEXT_cs")).andReturn(new ByteArrayInputStream("cesky text".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646", "TEXT_cs")).andReturn(new ByteArrayInputStream("cesky text".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5", "TEXT_cs")).andReturn(new ByteArrayInputStream("cesky text".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:25463364-b86b-4f2b-8fb3-598b55efa09f", "TEXT_cs")).andReturn(new ByteArrayInputStream("cesky text".getBytes("UTF-8")));

        EasyMock.expect(fa.getDataStream("vc:3d466a99-6dca-4113-87d9-831673bae580", "TEXT_en")).andReturn(new ByteArrayInputStream("english text".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef", "TEXT_en")).andReturn(new ByteArrayInputStream("english text".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646", "TEXT_en")).andReturn(new ByteArrayInputStream("english text".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5", "TEXT_en")).andReturn(new ByteArrayInputStream("english text".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:25463364-b86b-4f2b-8fb3-598b55efa09f", "TEXT_en")).andReturn(new ByteArrayInputStream("english text".getBytes("UTF-8")));


        replay(sa, fa, col);
        
        col.setFedoraAccess(fa);
        col.setSolrAccess(sa);
        
        List<Collection> collections = col.getCollections();
        Assert.assertNotNull(collections);
        Assert.assertTrue(collections.size() == 5);
        
        for (Collection collection : collections) {
            List<Description> descs = collection.getDescriptions();
            for (Description d : descs) {
                Assert.assertFalse(d.hasLongtext());
            }
        }
    }
    
    
    public void testVirtualCollectionsWithLongTexts() throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);

        FedoraAccessImpl fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), acLog)
        .addMockedMethod("getDataStream")
        .addMockedMethod("isStreamAvailable")
        .addMockedMethod("getDC")
        .createMock();
        
        SolrAccessImpl sa = createMockBuilder(SolrAccessImpl.class)
        		.withConstructor()
        		.addMockedMethod("request", new Class[]{String.class})
        		.createMock();
        		

        FedoraCollectionsManagerImpl col = createMockBuilder(FedoraCollectionsManagerImpl.class)
        .withConstructor()
        .addMockedMethod("getCollectionListFromResourceIndex")
        .addMockedMethod("languages")
        .createMock();

        InputStream stream = CollectionsManagerImplTest.class.getResourceAsStream("sparql.xml");
        Document document = XMLUtils.parseDocument(stream,true);
        EasyMock.expect(col.getCollectionListFromResourceIndex()).andReturn(document);
        EasyMock.expect(col.languages()).andReturn(Arrays.asList("cs","en")).anyTimes();

        EasyMock.expect(sa.request("fq=level:0&q=collection:(\"vc:3d466a99-6dca-4113-87d9-831673bae580\")&rows=0")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("solrresponse.xml"), true)).anyTimes();
        EasyMock.expect(sa.request("fq=level:0&q=collection:(\"vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef\")&rows=0")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("solrresponse.xml"), true)).anyTimes();
        EasyMock.expect(sa.request("fq=level:0&q=collection:(\"vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646\")&rows=0")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("solrresponse.xml"), true)).anyTimes();
        EasyMock.expect(sa.request("fq=level:0&q=collection:(\"vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5\")&rows=0")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("solrresponse.xml"), true)).anyTimes();
        EasyMock.expect(sa.request("fq=level:0&q=collection:(\"vc:25463364-b86b-4f2b-8fb3-598b55efa09f\")&rows=0")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("solrresponse.xml"), true)).anyTimes();

        EasyMock.expect(fa.getDC("vc:3d466a99-6dca-4113-87d9-831673bae580")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("3d466a99-6dca-4113-87d9-831673bae580.dc.xml"), true)).anyTimes();
        EasyMock.expect(fa.getDC("vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef.dc.xml"), true)).anyTimes();
        EasyMock.expect(fa.getDC("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("64b95a45-6ead-4bf1-aa93-c31b0ccbf646.dc.xml"), true)).anyTimes();
        EasyMock.expect(fa.getDC("vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("a9dd018c-32ed-474b-9ee5-071ebecfdef5.dc.xml"), true)).anyTimes();
        EasyMock.expect(fa.getDC("vc:25463364-b86b-4f2b-8fb3-598b55efa09f")).andReturn(XMLUtils.parseDocument(CollectionsManagerImplTest.class.getResourceAsStream("25463364-b86b-4f2b-8fb3-598b55efa09f.dc.xml"), true)).anyTimes();

        
        EasyMock.expect(fa.isStreamAvailable("vc:3d466a99-6dca-4113-87d9-831673bae580", "TEXT_cs")).andReturn(true).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:3d466a99-6dca-4113-87d9-831673bae580", "TEXT_en")).andReturn(true).anyTimes();

        EasyMock.expect(fa.isStreamAvailable("vc:3d466a99-6dca-4113-87d9-831673bae580", "LONG_TEXT_cs")).andReturn(true).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:3d466a99-6dca-4113-87d9-831673bae580", "LONG_TEXT_en")).andReturn(true).anyTimes();

        EasyMock.expect(fa.isStreamAvailable("vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef", "TEXT_cs")).andReturn(true).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef", "TEXT_en")).andReturn(true).anyTimes();

        EasyMock.expect(fa.isStreamAvailable("vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef", "LONG_TEXT_cs")).andReturn(true).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef", "LONG_TEXT_en")).andReturn(true).anyTimes();

        EasyMock.expect(fa.isStreamAvailable("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646", "TEXT_cs")).andReturn(true).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646", "TEXT_en")).andReturn(true).anyTimes();

        EasyMock.expect(fa.isStreamAvailable("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646", "LONG_TEXT_cs")).andReturn(true).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646", "LONG_TEXT_en")).andReturn(true).anyTimes();

        
        EasyMock.expect(fa.isStreamAvailable("vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5", "TEXT_cs")).andReturn(true).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5", "TEXT_en")).andReturn(true).anyTimes();

        EasyMock.expect(fa.isStreamAvailable("vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5", "LONG_TEXT_cs")).andReturn(true).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5", "LONG_TEXT_en")).andReturn(true).anyTimes();

        EasyMock.expect(fa.isStreamAvailable("vc:25463364-b86b-4f2b-8fb3-598b55efa09f", "TEXT_cs")).andReturn(true).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:25463364-b86b-4f2b-8fb3-598b55efa09f", "TEXT_en")).andReturn(true).anyTimes();

        EasyMock.expect(fa.isStreamAvailable("vc:25463364-b86b-4f2b-8fb3-598b55efa09f", "LONG_TEXT_cs")).andReturn(true).anyTimes();
        EasyMock.expect(fa.isStreamAvailable("vc:25463364-b86b-4f2b-8fb3-598b55efa09f", "LONG_TEXT_en")).andReturn(true).anyTimes();
        
        
        EasyMock.expect(fa.getDataStream("vc:3d466a99-6dca-4113-87d9-831673bae580", "TEXT_cs")).andReturn(new ByteArrayInputStream("cesky text".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef", "TEXT_cs")).andReturn(new ByteArrayInputStream("cesky text".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646", "TEXT_cs")).andReturn(new ByteArrayInputStream("cesky text".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5", "TEXT_cs")).andReturn(new ByteArrayInputStream("cesky text".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:25463364-b86b-4f2b-8fb3-598b55efa09f", "TEXT_cs")).andReturn(new ByteArrayInputStream("cesky text".getBytes("UTF-8")));

        EasyMock.expect(fa.getDataStream("vc:3d466a99-6dca-4113-87d9-831673bae580", "LONG_TEXT_cs")).andReturn(new ByteArrayInputStream("<html>Dlouhy cesky text takovy, jaky nejde nijak jinak ziskat</html>".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef", "LONG_TEXT_cs")).andReturn(new ByteArrayInputStream("<html>Dlouhy cesky text takovy, jaky nejde nijak jinak ziskat</html>t".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646", "LONG_TEXT_cs")).andReturn(new ByteArrayInputStream("<html>Dlouhy cesky text takovy, jaky nejde nijak jinak ziskat</html>t".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5", "LONG_TEXT_cs")).andReturn(new ByteArrayInputStream("<html>Dlouhy cesky text takovy, jaky nejde nijak jinak ziskat</html>".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:25463364-b86b-4f2b-8fb3-598b55efa09f", "LONG_TEXT_cs")).andReturn(new ByteArrayInputStream("<html>Dlouhy cesky text takovy, jaky nejde nijak jinak ziskat</html>".getBytes("UTF-8")));

        EasyMock.expect(fa.getDataStream("vc:3d466a99-6dca-4113-87d9-831673bae580", "TEXT_en")).andReturn(new ByteArrayInputStream("english text".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef", "TEXT_en")).andReturn(new ByteArrayInputStream("english text".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646", "TEXT_en")).andReturn(new ByteArrayInputStream("english text".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5", "TEXT_en")).andReturn(new ByteArrayInputStream("english text".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:25463364-b86b-4f2b-8fb3-598b55efa09f", "TEXT_en")).andReturn(new ByteArrayInputStream("english text".getBytes("UTF-8")));

        EasyMock.expect(fa.getDataStream("vc:3d466a99-6dca-4113-87d9-831673bae580", "LONG_TEXT_en")).andReturn(new ByteArrayInputStream("<html>long english text</html>".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:217d0320-5b5c-4bbd-8fd3-ee41cb81f1ef", "LONG_TEXT_en")).andReturn(new ByteArrayInputStream("<html>long english text</html>".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646", "LONG_TEXT_en")).andReturn(new ByteArrayInputStream("<html>long english text</html>".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:a9dd018c-32ed-474b-9ee5-071ebecfdef5", "LONG_TEXT_en")).andReturn(new ByteArrayInputStream("<html>long english text</html>".getBytes("UTF-8")));
        EasyMock.expect(fa.getDataStream("vc:25463364-b86b-4f2b-8fb3-598b55efa09f", "LONG_TEXT_en")).andReturn(new ByteArrayInputStream("<html>long english text</html>".getBytes("UTF-8")));

        
        
        replay(sa, fa, col);
        
        col.setFedoraAccess(fa);
        col.setSolrAccess(sa);
        
        List<Collection> collections = col.getCollections();
        Assert.assertNotNull(collections);
        Assert.assertTrue(collections.size() == 5);
        
        for (Collection collection : collections) {
            List<Description> descs = collection.getDescriptions();
            for (Description d : descs) {
                Assert.assertTrue(d.hasLongtext());
                Assert.assertNotNull(d.getLongTextName());
                Assert.assertNotNull(d.getLongText());

                Assert.assertNotNull(d.getName());
                Assert.assertNotNull(d.getText());

            }
        }
    }
    
    
}
