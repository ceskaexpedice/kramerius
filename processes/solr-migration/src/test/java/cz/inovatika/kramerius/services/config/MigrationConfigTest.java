package cz.inovatika.kramerius.services.config;

import cz.incad.kramerius.utils.XMLUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

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
}
