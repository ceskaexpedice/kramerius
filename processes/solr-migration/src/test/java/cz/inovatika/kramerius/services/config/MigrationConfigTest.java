package cz.inovatika.kramerius.services.config;

import cz.incad.kramerius.utils.XMLUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MigrationConfigTest {

    @Test
    public void testProcessConfig() throws ParserConfigurationException, IOException, SAXException {
        InputStream resourceAsStream = MigrationConfigTest.class.getResourceAsStream("config1.xml");
        Document document = XMLUtils.parseDocument(resourceAsStream);
        MigrationConfig config = MigrationConfigParser.parse(document.getDocumentElement());
        Assert.assertEquals(config.getIteratorConfig().getIdField(), "$iteration.id$");
        Assert.assertEquals(config.getFeederConfig().getRequestConfig().getIdIdentifier(), "$iteration.id$");

        String filterQuery = config.getIteratorConfig().getFilterQuery();
        Assert.assertEquals(filterQuery, "$iteration.fquery$");

        String workingTime = config.getWorkingTime();
        Assert.assertEquals(workingTime, "$iteration.workingtime$");
    }

    @Test
    public void testTimestampUrlIsNormalizedAndPassedToIterator() throws ParserConfigurationException, IOException, SAXException {
        Document document = XMLUtils.parseDocument(new ByteArrayInputStream((
                "<cdkprocess>" +
                        "<source-name>tul</source-name>" +
                        "<name>tul-update</name>" +
                        "<type>update</type>" +
                        "<timestamp>http://localhost:8080/search/api/admin/v7.0/connected</timestamp>" +
                        "<iteration>" +
                        "<iteratorFactory class=\"cz.inovatika.kramerius.services.iterators.factories.SolrIteratorFactory\"></iteratorFactory>" +
                        "<url>http://source/solr</url>" +
                        "<endpoint>select</endpoint>" +
                        "<id>pid</id>" +
                        "<timestamp_field>indexed</timestamp_field>" +
                        "</iteration>" +
                        "<feeder>" +
                        "<feederFactory class=\"cz.inovatika.kramerius.services.workers.copy.simple.SimpleCopySolrMigrationIndexFeederFactory\"></feederFactory>" +
                        "<request>" +
                        "<url>http://source/solr</url>" +
                        "<endpoint>select</endpoint>" +
                        "<batchsize>10</batchsize>" +
                        "<fieldlist>pid title</fieldlist>" +
                        "</request>" +
                        "<destination><url>http://destination/solr/update</url></destination>" +
                        "</feeder>" +
                        "</cdkprocess>").getBytes(StandardCharsets.UTF_8)));

        MigrationConfig config = MigrationConfigParser.parse(document.getDocumentElement());

        String expected = "http://localhost:8080/search/api/admin/v7.0/connected/tul/timestamp";
        Assert.assertEquals(expected, config.getTimestampUrl());
        Assert.assertEquals(expected, config.getIteratorConfig().getTimestampUrl());
        Assert.assertEquals("indexed", config.getIteratorConfig().getTimestampField());
    }

    @Test
    public void testAppendTimestampFilter() {
        Assert.assertEquals(
                "model:page AND indexed:[2026-06-15T10:00:00Z TO NOW]",
                EffectiveMigrationConfigRenderer.appendTimestampFilter(
                        "model:page", "indexed", "2026-06-15T10:00:00Z"));
        Assert.assertEquals(
                "indexed:[2026-06-15T10:00:00Z TO NOW]",
                EffectiveMigrationConfigRenderer.appendTimestampFilter(
                        "", "indexed", "2026-06-15T10:00:00Z"));
    }
}
