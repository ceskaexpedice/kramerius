package cz.inovatika.kramerius.services.iterators.config;

import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.iterators.MigrationIterator;
import cz.inovatika.kramerius.services.iterators.MigrationIteratorFactory;
import cz.inovatika.kramerius.services.iterators.solr.AbstractSolrIterator;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class MigrationIteratorFactoryTest {

    @Test
    public void testFactory() throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        InputStream resourceAsStream = SolrConfigurationTest.class.getResourceAsStream("config.xml");
        Document document = XMLUtils.parseDocument(resourceAsStream);

        SolrIteratorConfig config = SolrConfigParser.parse(XMLUtils.findElement(document.getDocumentElement(), "migration"), "fq");

        MigrationIteratorFactory migrationIteratorFactory = MigrationIteratorFactory.create(config);
        MigrationIterator migrationIterator = migrationIteratorFactory.createMigrationIterator(config, null);
        Assert.assertTrue(migrationIterator instanceof AbstractSolrIterator);

        AbstractSolrIterator abstractSolrIterator = (AbstractSolrIterator) migrationIterator;
        Assert.assertEquals(abstractSolrIterator.getMasterQuery(), "*:*");
        Assert.assertEquals(abstractSolrIterator.getEndpoint(), "select");
        Assert.assertEquals(abstractSolrIterator.getAddress(), "$iteration.url$");
    }
}
