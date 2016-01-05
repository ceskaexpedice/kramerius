package cz.incad.kramerius.document;

import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class _DocumentServiceTestPrepare {

    public static Injector prepareInjector(String pages, boolean turnOffPdfCheck) throws NoSuchMethodException,
            IOException, ParserConfigurationException, SAXException,
            LexerException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        Locale locale = Locale.getDefault();
        FedoraAccessImpl fa33 = _DocumentServiceTestPrepare.prepareFedoraAccess(acLog);
        ResourceBundleService bundleService = _DocumentServiceTestPrepare.prepareBundleService(locale);
        SolrAccess solrAccess = _DocumentServiceTestPrepare.prepareSolrAccess();
    
        Object[] confObjects = _DocumentServiceTestPrepare.prepareConfiguration(pages, turnOffPdfCheck);
        replay(fa33, solrAccess, bundleService,acLog,confObjects[0],confObjects[1]);
        
        Injector injector = Guice.createInjector(new _DocumentServiceModule(locale, fa33, bundleService,solrAccess,(KConfiguration) confObjects[1]));
        return injector;
    }

    public static ResourceBundleService prepareBundleService(Locale locale)
            throws IOException {
        ResourceBundleService bundleService = EasyMock.createMock(ResourceBundleService.class);
        EasyMock.expect(bundleService.getResourceBundle("labels", locale)).andReturn(new PropertyResourceBundle(new InputStreamReader(new ByteArrayInputStream(DocumentServiceTest.BUNLDE.getBytes()), Charset.forName("UTF-8")))).anyTimes();
        EasyMock.expect(bundleService.getResourceBundle("base", locale)).andReturn(new PropertyResourceBundle(new InputStreamReader(new ByteArrayInputStream(DocumentServiceTest.BUNLDE.getBytes()), Charset.forName("UTF-8")))).anyTimes();
        return bundleService;
    }

    public static SolrAccess prepareSolrAccess() throws IOException {
        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        Set<String> keys = DocumentServiceTest.PATHS_MAPPING.keySet();
        for (String key : keys) {
            EasyMock.expect(solrAccess.getPath(key)).andReturn(new ObjectPidsPath[] { DocumentServiceTest.PATHS_MAPPING.get(key)}).anyTimes();
        }
        return solrAccess;
    }

    public static FedoraAccessImpl prepareFedoraAccess(StatisticsAccessLog acLog)
            throws NoSuchMethodException, IOException,
            ParserConfigurationException, SAXException, LexerException {
        FedoraAccessImpl fa33 = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(),acLog)
        .addMockedMethod("getFedoraDescribeStream")
        .addMockedMethod("getRelsExt")
        .addMockedMethod("isImageFULLAvailable")
        .addMockedMethod("getDC")
        .addMockedMethod("getBiblioMods")
        .addMockedMethod(FedoraAccessImpl.class.getMethod("getKrameriusModelName", String.class))
        .createMock();
        
        
        
        EasyMock.expect(fa33.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile33());
        
        DataPrepare.drobnustkyRelsExt(fa33);
        DataPrepare.drobnustkyWithIMGFULL(fa33);
        DataPrepare.drobnustkyDCS(fa33);
        DataPrepare.drobnustkyMODS(fa33);
        
        
        Set<String> keySet = DocumentServiceTest.MODELS_MAPPING.keySet();
        for (String key : keySet) {
            String model = DocumentServiceTest.MODELS_MAPPING.get(key);
            PIDParser pidParser = new PIDParser(model);
            pidParser.disseminationURI();
            String modelK4Name  = pidParser.getObjectId();
            EasyMock.expect(fa33.getKrameriusModelName(key)).andReturn(modelK4Name).anyTimes();
        }
        return fa33;
    }

    public static Object[] prepareConfiguration(String pages, boolean turnOff) {
        Configuration conf = EasyMock.createMock(Configuration.class);
        EasyMock.expect(conf.getString("generatePdfMaxRange")).andReturn(pages).anyTimes();
        EasyMock.expect(conf.getBoolean("turnOffPdfCheck")).andReturn(turnOff).anyTimes();
        KConfiguration kConfiguration = EasyMock.createMock(KConfiguration.class);
        EasyMock.expect(kConfiguration.getConfiguration()).andReturn(conf).anyTimes();
        return new Object[] {conf,kConfiguration};
    }

}
