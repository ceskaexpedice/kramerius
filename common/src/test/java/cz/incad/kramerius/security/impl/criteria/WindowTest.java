package cz.incad.kramerius.security.impl.criteria;

import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.xml.sax.SAXException;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.security.EvaluatingResult;
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
        EvaluatingResult evaluated = window(firstYearWallFromGUI, secondYearWallFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.TRUE);
    }
    
    //Drobnustky stranka
    @Test
    public void testW3() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String firstYearWallFromGUI = "1770";
        String secondYearWallFromGUI = "1980";
        String requestedPID = DataPrepare.DROBNUSTKY_PIDS[2];
        EvaluatingResult evaluated = window(firstYearWallFromGUI, secondYearWallFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.TRUE);
    }


    public EvaluatingResult window(String firstYearFromGUI,String secondYearFromGUI, String requestedPID) throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        FedoraAccessImpl fa33 = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), acLog)
        .addMockedMethod("getFedoraDescribeStream")
        .addMockedMethod("getBiblioMods")
        .addMockedMethod("getDC")
        .createMock();
        
        EasyMock.expect(fa33.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile33());
        DataPrepare.drobnustkyMODS(fa33);
        DataPrepare.drobnustkyDCS(fa33);
 
        DataPrepare.narodniListyMods(fa33);
        DataPrepare.narodniListyDCs(fa33);
 
        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        Set<String> keys = DataPrepare.PATHS_MAPPING.keySet();
        for (String key : keys) {
            EasyMock.expect(solrAccess.getPath(key)).andReturn(new ObjectPidsPath[] { DataPrepare.PATHS_MAPPING.get(key)}).anyTimes();
        }
        
        replay(fa33, solrAccess,acLog);

        RightCriteriumContextFactoryImpl contextFactory = new RightCriteriumContextFactoryImpl();
        contextFactory.setFedoraAccess(fa33);
        contextFactory.setSolrAccess(solrAccess);
        
        RightCriteriumContext context = contextFactory.create(requestedPID, null, null, "localhost", "127.0.0.1");

        Window window = new Window();
        window.setCriteriumParamValues(new Object[] {firstYearFromGUI,secondYearFromGUI});
        window.setEvaluateContext(context);
        
        EvaluatingResult evaluated = window.evalute();
        return evaluated;
    }

    
}
