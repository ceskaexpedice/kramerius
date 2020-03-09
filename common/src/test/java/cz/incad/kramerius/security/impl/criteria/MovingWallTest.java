/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.security.impl.criteria;
 
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
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
import cz.incad.kramerius.utils.solr.SolrUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;

public class MovingWallTest {

    

    // Drobnustky
    //@Test
    public void testMW1() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "70";
        String modeFromGUI = "year";
        String requestedPID = DataPrepare.DROBNUSTKY_PIDS[0];
        EvaluatingResult evaluated = mw(movingWallFromGUI, modeFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.TRUE);
    }
    
    //@Test
    public void testMW2() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "270";
        String modeFromGUI = "year";
        String requestedPID = DataPrepare.DROBNUSTKY_PIDS[0];
        EvaluatingResult evaluated = mw(movingWallFromGUI, modeFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.FALSE);
    }

    //Drobnustky stranka
    //@Test
    public void testMW3() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "70";
        String modeFromGUI = "year";
        String requestedPID = DataPrepare.DROBNUSTKY_PIDS[2];
        EvaluatingResult evaluated = mw(movingWallFromGUI, modeFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.TRUE);
    }

    //Drobnustky stranka
    //@Test
    public void testMW4() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "270";
        String modeFromGUI = "year";
        String requestedPID = DataPrepare.DROBNUSTKY_PIDS[2];
        EvaluatingResult evaluated = mw(movingWallFromGUI, modeFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.FALSE);
    }
    
    
    //@Test
    public void testMW5() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "270";
        String modeFromGUI = "year";
        String requestedPID = "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6";// volume;
        EvaluatingResult evaluated = mw(movingWallFromGUI, modeFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.FALSE);
    }
    
    //@Test
    public void testMW6() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "1884";
        String modeFromGUI = "month";
        String requestedPID = DataPrepare.DROBNUSTKY_PIDS[0];
        EvaluatingResult evaluated = mw(movingWallFromGUI, modeFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.TRUE);
    }
 
    //@Test
    public void testMW7() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "5000";
        String modeFromGUI = "month";
        String requestedPID = DataPrepare.DROBNUSTKY_PIDS[0];
        EvaluatingResult evaluated = mw(movingWallFromGUI, modeFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.FALSE);
    }
    
    //@Test
    public void testMW8() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "5";
        String modeFromGUI = "month";
        String requestedPID = "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6";// volume;
        EvaluatingResult evaluated = mw(movingWallFromGUI, modeFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.TRUE);
    }
    
    //@Test
    public void testMW9() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "5000";
        String modeFromGUI = "month";
        String requestedPID = "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6";// volume;
        EvaluatingResult evaluated = mw(movingWallFromGUI, modeFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.FALSE);
    }

    public EvaluatingResult mw(String movingWallFromGUI, String modeFromGUI, String requestedPID) throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
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
        MovingWall wall = new MovingWall();
        
        String firstPid = requestedPID;
        
        wall.setCriteriumParamValues(new Object[] {movingWallFromGUI, modeFromGUI, "test", firstPid});
        wall.setEvaluateContext(context);
        
        EvaluatingResult evaluated = wall.evalute();
        return evaluated;
    }

}

