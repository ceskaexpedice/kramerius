package cz.incad.kramerius.rest.api.k5.client.pdf;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import cz.incad.kramerius.pdf.impl.ConfigurationUtils;

public class AbstractPDFResourceTest {


    

    @Test
    public void testConfiguration() {
        Configuration configuration = EasyMock.createMock(Configuration.class);
        EasyMock.expect(configuration.getString("generatePdfMaxRange")).andReturn("20").anyTimes();
        EasyMock.expect(configuration.getBoolean("turnOffPdfCheck")).andReturn(false).anyTimes();
        EasyMock.replay(configuration);

        try {
            ConfigurationUtils.checkNumber(21, configuration);
        } catch (Exception e) {
            // ok
        }

        try {
            int val = ConfigurationUtils.checkNumber(19, configuration);
            Assert.assertTrue(val == 19);
        } catch (Exception e) {
            Assert.fail();
        }
        try {
            int val = ConfigurationUtils.checkNumber(20, configuration);
            Assert.assertTrue(val == 20);
        } catch (Exception e) {
            Assert.fail();
        }
    }
    

    @Test
    public void testConfiguration2() {
        Configuration configuration = EasyMock.createMock(Configuration.class);
        EasyMock.expect(configuration.getString("generatePdfMaxRange")).andReturn("20").anyTimes();
        EasyMock.expect(configuration.getBoolean("turnOffPdfCheck")).andReturn(true).anyTimes();
        EasyMock.replay(configuration);

        try {
            ConfigurationUtils.checkNumber(21, configuration);
        } catch (Exception e) {
            Assert.fail();
        }

        try {
            int val = ConfigurationUtils.checkNumber(19, configuration);
            Assert.assertTrue(val == 19);
        } catch (Exception e) {
            Assert.fail();
        }
        try {
            int val = ConfigurationUtils.checkNumber(20, configuration);
            Assert.assertTrue(val == 20);
        } catch (Exception e) {
            Assert.fail();
        }
    }

}
