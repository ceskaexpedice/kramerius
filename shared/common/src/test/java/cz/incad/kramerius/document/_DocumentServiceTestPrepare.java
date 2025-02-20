package cz.incad.kramerius.document;

import static org.easymock.EasyMock.createMock;
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

import cz.incad.kramerius.fedora.om.impl.HazelcastServerNode;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;

import org.apache.commons.configuration.Configuration;
import org.ceskaexpedice.akubra.utils.pid.LexerException;
import org.ceskaexpedice.akubra.utils.pid.PIDParser;
import org.easymock.EasyMock;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class _DocumentServiceTestPrepare {

    public static Injector prepareInjector(String pages, boolean turnOffPdfCheck) throws NoSuchMethodException,
            IOException, ParserConfigurationException, SAXException,
            LexerException {
        AggregatedAccessLogs acLog = EasyMock.createMock(AggregatedAccessLogs.class);
        Locale locale = Locale.getDefault();
        FedoraAccessAkubraImpl fa4 = _DocumentServiceTestPrepare.prepareFedoraAccess(acLog);


        ResourceBundleService bundleService = _DocumentServiceTestPrepare.prepareBundleService(locale);
        SolrAccess solrAccess = _DocumentServiceTestPrepare.prepareSolrAccess();
    
        Object[] confObjects = _DocumentServiceTestPrepare.prepareConfiguration(pages, turnOffPdfCheck);
        replay(fa4, solrAccess, bundleService,acLog,confObjects[0],confObjects[1]);
        
        Injector injector = Guice.createInjector(new _DocumentServiceModule(locale, fa4, bundleService,solrAccess));
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
            EasyMock.expect(solrAccess.getPidPaths(key)).andReturn(new ObjectPidsPath[] { DocumentServiceTest.PATHS_MAPPING.get(key)}).anyTimes();
        }
        return solrAccess;
    }

    public static FedoraAccessAkubraImpl prepareFedoraAccess(StatisticsAccessLog acLog)
            throws NoSuchMethodException, IOException,
            ParserConfigurationException, SAXException, LexerException, org.ceskaexpedice.akubra.utils.pid.LexerException {

        ProcessingIndexFeeder feeder = createMock(ProcessingIndexFeeder.class);
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();
        HazelcastServerNode.ensureHazelcastNode();

        FedoraAccessAkubraImpl fa4 = createMockBuilder(FedoraAccessAkubraImpl.class)

        .withConstructor( feeder ,acLog, cacheManager)
        //.addMockedMethod("getFedoraDescribeStream")
        .addMockedMethod("getRelsExt")
        .addMockedMethod("isImageFULLAvailable")
        .addMockedMethod("isStreamAvailable")
        .addMockedMethod("getDC")
        .addMockedMethod("getBiblioMods")
        .addMockedMethod(FedoraAccessAkubraImpl.class.getMethod("getKrameriusModelName", String.class))
        .createMock();
        
        
        
        //EasyMock.expect(fa4.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile33());
        
        DataPrepare.drobnustkyRelsExt(fa4);
        DataPrepare.drobnustkyWithIMGFULL(fa4);
        DataPrepare.drobnustkyDCS(fa4);
        DataPrepare.drobnustkyMODS(fa4);
        

        Set<String> keySet = DocumentServiceTest.MODELS_MAPPING.keySet();
        for (String key : keySet) {
            String model = DocumentServiceTest.MODELS_MAPPING.get(key);
            PIDParser pidParser = new PIDParser(model);
            pidParser.disseminationURI();
            String modelK4Name  = pidParser.getObjectId();
            EasyMock.expect(fa4.getKrameriusModelName(key)).andReturn(modelK4Name).anyTimes();
        }
        return fa4;
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
