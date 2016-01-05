package cz.cas.lib.knav;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.impl.criteria.MovingWall;
import cz.incad.kramerius.security.impl.criteria.mw.DateLexer;
import cz.incad.kramerius.security.impl.criteria.mw.DatesParser;
import cz.incad.kramerius.utils.XMLUtils;

public class ApplyMovingWallTest {
    
    @Test
    public void testConfiguration1() throws IOException {
        SolrAccess sa = EasyMock.createMock(SolrAccess.class);
        ObjectPidsPath pidPath = new ObjectPidsPath("uuid:045b1250-7e47-11e0-add1-000d606f5dc6");
        ObjectModelsPath modelPAth = new ObjectModelsPath("periodical");
        EasyMock.expect(sa.getPath("uuid:045b1250-7e47-11e0-add1-000d606f5dc6")).andReturn(new ObjectPidsPath[] {pidPath});
        EasyMock.expect(sa.getPathOfModels("uuid:045b1250-7e47-11e0-add1-000d606f5dc6")).andReturn(new ObjectModelsPath[] {modelPAth});
        
        Configuration configuration = EasyMock.createMock(Configuration.class);
        EasyMock.expect(configuration.getInt("mwprocess.wall",70)).andReturn(70);
        EasyMock.expect(configuration.containsKey("mwprocess.model.periodical.wall")).andReturn(false);
        EasyMock.replay(sa,configuration);
        
        int configuredWall = ApplyMWUtils.configuredWall(sa, "uuid:045b1250-7e47-11e0-add1-000d606f5dc6", configuration);
        Assert.assertTrue(configuredWall == 70);
    }


    @Test
    public void testConfiguration2() throws IOException {
        SolrAccess sa = EasyMock.createMock(SolrAccess.class);
        ObjectPidsPath pidPath = new ObjectPidsPath("uuid:045b1250-7e47-11e0-add1-000d606f5dc6");
        ObjectModelsPath modelPAth = new ObjectModelsPath("periodical");
        EasyMock.expect(sa.getPath("uuid:045b1250-7e47-11e0-add1-000d606f5dc6")).andReturn(new ObjectPidsPath[] {pidPath});
        EasyMock.expect(sa.getPathOfModels("uuid:045b1250-7e47-11e0-add1-000d606f5dc6")).andReturn(new ObjectModelsPath[] {modelPAth});
        
        Configuration configuration = EasyMock.createMock(Configuration.class);
        EasyMock.expect(configuration.getInt("mwprocess.wall",70)).andReturn(70);
        EasyMock.expect(configuration.containsKey("mwprocess.model.periodical.wall")).andReturn(true);
        EasyMock.expect(configuration.getInt("mwprocess.model.periodical.wall")).andReturn(20);
        EasyMock.replay(sa,configuration);
        
        int configuredWall = ApplyMWUtils.configuredWall(sa, "uuid:045b1250-7e47-11e0-add1-000d606f5dc6", configuration);
        Assert.assertTrue(configuredWall == 20);
    }
    
    @Test
    public void testPattern() throws IOException {
        Date date = MovingWall.customizedDates("2001.07.04 at 12:08:56 PDT", Arrays.asList("yyyy.MM.dd 'at' HH:mm:ss 'PDT'"));
        Assert.assertNotNull(date);

        date = MovingWall.customizedDates("2001.07.04 at 12:08:56 PDT", Arrays.asList("yyyy HH:mm:ss 'PDT'","yyyy.MM HH:mm:ss 'PDT'"));
        Assert.assertNull(date);
        
    }
    @Test
    public void testFlags() {
        Assert.assertTrue(ApplyMWUtils.detectChange(false, null));
        Assert.assertTrue(ApplyMWUtils.detectChange(true, null));
        Assert.assertTrue(ApplyMWUtils.detectChange(false, "policy:public"));
        Assert.assertTrue(ApplyMWUtils.detectChange(true, "policy:private"));

        Assert.assertFalse(ApplyMWUtils.detectChange(false, "policy:private"));
        Assert.assertFalse(ApplyMWUtils.detectChange(true, "policy:public"));

    }
    

}
