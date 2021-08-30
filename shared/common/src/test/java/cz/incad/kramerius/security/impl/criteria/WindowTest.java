package cz.incad.kramerius.security.impl.criteria;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import cz.incad.kramerius.fedora.impl.FedoraAccessAkubraImpl;
import cz.incad.kramerius.fedora.om.impl.HazelcastServerNode;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import junit.framework.Assert;

import org.easymock.EasyMock;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.junit.Test;
import org.xml.sax.SAXException;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.impl.RightCriteriumContextFactoryImpl;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;

public class WindowTest {

    // Drobnustky
    @Test
    public void testW1() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String firstYearWallFromGUI = "1770";
        String secondYearWallFromGUI = "1980";
        String requestedPID = DataPrepare.DROBNUSTKY_PIDS[0];
        EvaluatingResultState evaluated = window(firstYearWallFromGUI, secondYearWallFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResultState.TRUE);
    }

    //Drobnustky stranka
    @Test
    public void testW3() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String firstYearWallFromGUI = "1770";
        String secondYearWallFromGUI = "1980";
        String requestedPID = DataPrepare.DROBNUSTKY_PIDS[2];
        EvaluatingResultState evaluated = window(firstYearWallFromGUI, secondYearWallFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResultState.TRUE);
    }


    public EvaluatingResultState window(String firstYearFromGUI,String secondYearFromGUI, String requestedPID) throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        AggregatedAccessLogs acLog = EasyMock.createMock(AggregatedAccessLogs.class);
        ProcessingIndexFeeder feeder = createMock(ProcessingIndexFeeder.class);
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();

        HazelcastServerNode.ensureHazelcastNode();
        FedoraAccessAkubraImpl fa4 = createMockBuilder(FedoraAccessAkubraImpl.class)
                .withConstructor(KConfiguration.getInstance(), feeder, acLog, cacheManager)
                .addMockedMethod("getRelsExt")
                .addMockedMethod("isStreamAvailable")
                .addMockedMethod("getDC")
                .addMockedMethod("getBiblioMods")
                .createMock();


        DataPrepare.drobnustkyMODS(fa4);
        DataPrepare.drobnustkyDCS(fa4);

        DataPrepare.narodniListyMods(fa4);
        DataPrepare.narodniListyDCs(fa4);

        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        Set<String> keys = DataPrepare.PATHS_MAPPING.keySet();
        for (String key : keys) {
            EasyMock.expect(solrAccess.getPath(key)).andReturn(new ObjectPidsPath[] { DataPrepare.PATHS_MAPPING.get(key)}).anyTimes();
        }

        replay(fa4,feeder, solrAccess,acLog);

        RightCriteriumContextFactoryImpl contextFactory = new RightCriteriumContextFactoryImpl();
        contextFactory.setFedoraAccess(fa4);
        contextFactory.setSolrAccess(solrAccess);

        RightCriteriumContext context = contextFactory.create(requestedPID, null, null, "localhost", "127.0.0.1", null);

        Window window = new Window();
        window.setCriteriumParamValues(new Object[] {firstYearFromGUI,secondYearFromGUI});
        window.setEvaluateContext(context);

        EvaluatingResultState evaluated = window.evalute();
        return evaluated;
    }


}