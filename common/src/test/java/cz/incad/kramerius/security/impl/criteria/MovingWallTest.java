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
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;

public class MovingWallTest {

    
    public static Map<String, ObjectPidsPath> PATHS_MAPPING = new HashMap<String, ObjectPidsPath>(); static {
        // monograph -> page
        PATHS_MAPPING.put(DataPrepare.DROBNUSTKY_PIDS[0], new ObjectPidsPath(DataPrepare.DROBNUSTKY_PIDS[0]));
        for (int i = 0; i < DataPrepare.DROBNUSTKY_PIDS.length; i++) {
            String pid = DataPrepare.DROBNUSTKY_PIDS[i];
            if (i > 0) {
                PATHS_MAPPING.put(pid, new ObjectPidsPath(DataPrepare.DROBNUSTKY_PIDS[0],pid));
            }
        }
    }

    // Drobnustky
    @Test
    public void testMW1() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "70";
        String requestedPID = DataPrepare.DROBNUSTKY_PIDS[0];
        EvaluatingResult evaluated = mw(movingWallFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.TRUE);
    }

    @Test
    public void testMW2() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "270";
        String requestedPID = DataPrepare.DROBNUSTKY_PIDS[0];
        EvaluatingResult evaluated = mw(movingWallFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.FALSE);
    }

    //Drobnustky stranka
    @Test
    public void testMW3() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "70";
        String requestedPID = DataPrepare.DROBNUSTKY_PIDS[2];
        EvaluatingResult evaluated = mw(movingWallFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.TRUE);
    }

    //Drobnustky stranka
    @Test
    public void testMW4() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        String movingWallFromGUI = "270";
        String requestedPID = DataPrepare.DROBNUSTKY_PIDS[2];
        EvaluatingResult evaluated = mw(movingWallFromGUI, requestedPID);
        Assert.assertEquals(evaluated, EvaluatingResult.FALSE);
    }

    public EvaluatingResult mw(String movingWallFromGUI, String requestedPID) throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        FedoraAccessImpl fa33 = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance())
        .addMockedMethod("getFedoraDescribeStream")
        .addMockedMethod("getBiblioMods")
        .addMockedMethod("getDC")
        .createMock();
        
        EasyMock.expect(fa33.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile33());
        DataPrepare.drobnustkyMODS(fa33);
        DataPrepare.drobnustkyDCS(fa33);
        
 
        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        Set<String> keys = PATHS_MAPPING.keySet();
        for (String key : keys) {
            EasyMock.expect(solrAccess.getPath(key)).andReturn(new ObjectPidsPath[] { PATHS_MAPPING.get(key)}).anyTimes();
        }
        
        replay(fa33, solrAccess);

        RightCriteriumContextFactoryImpl contextFactory = new RightCriteriumContextFactoryImpl();
        contextFactory.setFedoraAccess(fa33);
        contextFactory.setSolrAccess(solrAccess);
        
        RightCriteriumContext context = contextFactory.create(requestedPID, null, null, "localhost", "127.0.0.1");
        MovingWall wall = new MovingWall();
        wall.setCriteriumParamValues(new Object[] {movingWallFromGUI});
        wall.setEvaluateContext(context);
        
        EvaluatingResult evaluated = wall.evalute();
        return evaluated;
    }
    
    

}

