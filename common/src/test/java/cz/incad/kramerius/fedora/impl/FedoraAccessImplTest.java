/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.fedora.impl;

import static cz.incad.kramerius.fedora.impl.DataPrepare.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.xml.sax.SAXException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessStackAware;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;

public class FedoraAccessImplTest {
    

    /** Test isFullThumbnailAvailable method (page pid)
     * @throws LexerException */
    @Test
    public void testIsFullthumbnailAvailableWithPage() throws IOException, ParserConfigurationException, SAXException, LexerException {
        StatisticsAccessLog aclog = EasyMock.createMock(StatisticsAccessLog.class);

        FedoraAccessImpl fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), aclog)
        .addMockedMethod("getFedoraVersion")
        .addMockedMethod("getFedoraDataStreamsList")
        .createMock();
        

        EasyMock.expect(fa.getFedoraVersion()).andReturn("3.4.2");
        dataStreams(fa, "uuid:3ee97ce8-e548-11e0-9867-005056be0007");
        
        
        replay(fa, aclog);

        boolean flag = fa.isFullthumbnailAvailable("uuid:3ee97ce8-e548-11e0-9867-005056be0007/@886");
        Assert.assertTrue(flag);
    }

    /** Test isFullThumbnailAvailable method 
     * @throws LexerException */
    @Test
    public void testIsFullThumbnailAvailableWithoutPage() throws IOException, ParserConfigurationException, SAXException, LexerException {
        StatisticsAccessLog aclog = EasyMock.createMock(StatisticsAccessLog.class);

        FedoraAccessImpl fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), aclog)
        .addMockedMethod("getFedoraVersion")
        .addMockedMethod("getFedoraDataStreamsList")
        .createMock();
        
        EasyMock.expect(fa.getFedoraVersion()).andReturn("3.4.2");
        dataStreams(fa, "uuid:3ee97ce8-e548-11e0-9867-005056be0007");
        
        replay(fa, aclog);

        boolean flag = fa.isFullthumbnailAvailable("uuid:3ee97ce8-e548-11e0-9867-005056be0007");
        Assert.assertTrue(flag);
    }


    /** Test isFullImageAvailable method (page pid)
     * @throws LexerException */
    @Test
    public void testIsFullImageAvailableWithPage() throws IOException, ParserConfigurationException, SAXException, LexerException {
        StatisticsAccessLog aclog = EasyMock.createMock(StatisticsAccessLog.class);

        FedoraAccessImpl fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), aclog)
        .addMockedMethod("getFedoraVersion")
        .addMockedMethod("getFedoraDataStreamsList")
        .createMock();
        

        EasyMock.expect(fa.getFedoraVersion()).andReturn("3.4.2");
        dataStreams(fa, "uuid:3ee97ce8-e548-11e0-9867-005056be0007");
        
        replay(fa,aclog);

        boolean flag = fa.isFullthumbnailAvailable("uuid:3ee97ce8-e548-11e0-9867-005056be0007/@886");
        Assert.assertTrue(flag);
    }
    

    /** Test isFullImageAvailable method 
     * @throws LexerException */
    @Test
    public void testIsFullImageAvailableWithoutPage() throws IOException, ParserConfigurationException, SAXException, LexerException {
        StatisticsAccessLog aclog = EasyMock.createMock(StatisticsAccessLog.class);
        FedoraAccessImpl fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), aclog)
        .addMockedMethod("getFedoraVersion")
        .addMockedMethod("getFedoraDataStreamsList")
        .createMock();
        
        EasyMock.expect(fa.getFedoraVersion()).andReturn("3.4.2");
        dataStreams(fa, "uuid:3ee97ce8-e548-11e0-9867-005056be0007");
        
        replay(fa,aclog);

        boolean flag = fa.isFullthumbnailAvailable("uuid:3ee97ce8-e548-11e0-9867-005056be0007");
        Assert.assertTrue(flag);
    }
    

    
    /** Test getModelName method 
     * @throws LexerException */
    @Test
    public void testGetKrameriusModelName() throws IOException, ParserConfigurationException, SAXException, LexerException {
        StatisticsAccessLog aclog = EasyMock.createMock(StatisticsAccessLog.class);

        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), aclog)
        .addMockedMethod("getRelsExt")
        .createMock();
        
        drobnustkyRelsExt(fa);
        
        replay(fa,aclog);

        String monographModel = fa.getKrameriusModelName(fa.getRelsExt("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6"));
        assertEquals("monograph", monographModel);
        for (String page : DataPrepare.DROBNUSTKY_PIDS) {
            if (!page.equals("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6")) {
                String pageModel = fa.getKrameriusModelName(fa.getRelsExt(page));
                assertEquals("page", pageModel);
            }
        }
    }


    

    /** Test getDonator method 
     * @throws LexerException */
    @Test
    public void testGetDonator() throws IOException, ParserConfigurationException, SAXException, LexerException {
        StatisticsAccessLog aclog = EasyMock.createMock(StatisticsAccessLog.class);

        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), aclog)
        .addMockedMethod("getRelsExt")
        .createMock();
        
        drobnustkyRelsExt(fa);
        
        replay(fa,aclog);

        String donator = fa.getDonator(fa.getRelsExt("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6"));
        assertEquals("norway", donator);
    }
    
    /** Test correct data - IMG_FULL present 
     * @throws LexerException */
    @Test
    public void testFindFirstViewablePid_good() throws IOException, ParserConfigurationException, SAXException, LexerException {
        StatisticsAccessLog aclog = EasyMock.createMock(StatisticsAccessLog.class);
        // test correct data - IMG_FULL in pages
        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), aclog)
        .addMockedMethod("isStreamAvailable")
        .addMockedMethod("getRelsExt")
        .createMock();
        
        drobnustkyWithIMGFULL(fa);
        drobnustkyRelsExt(fa);
        
        replay(fa,aclog);
        
        String firstPageForDrobnustky = fa.findFirstViewablePid("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6");
        // accept right page
        assertEquals("uuid:4308eb80-b03b-11dd-a0f6-000d606f5dc6", firstPageForDrobnustky);
    }
    
    
    
    /** Test bad data - IMG_FULL not present 
     * @throws LexerException */
    @Test
    public void testFindFirstViewablePid_bad() throws IOException, ParserConfigurationException, SAXException, LexerException {
        StatisticsAccessLog aclog = EasyMock.createMock(StatisticsAccessLog.class);

        // test correct data - IMG_FULL in pages
        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), aclog)
        .addMockedMethod("isStreamAvailable")
        .addMockedMethod("getRelsExt")
        .createMock();
        
        drobnustkyWithOutIMGFULL(fa);
        drobnustkyRelsExt(fa);
        
        replay(fa,aclog);
        
        String firstPageForDrobnustky = fa.findFirstViewablePid("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6");
        // nic nenalezeno.. 
        assertNull(firstPageForDrobnustky);
    }
    
    
    @Test
    public void testProcessTree_SkipBranch() throws IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException {
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        
        final List<Integer> order = new ArrayList<Integer>();

        StatisticsAccessLog aclog = EasyMock.createMock(StatisticsAccessLog.class);

        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(),aclog)
        .addMockedMethod("getRelsExt")
        .createMock();
        
        narodniListyRelsExt(fa);
        
        replay(fa,aclog);

        fa.processSubtree("uuid:ae876087-435d-11dd-b505-00145e5790ea", new TreeNodeProcessor() {
            
            @Override
            public void process(String pid, int level) throws ProcessSubtreeException {
                mapping.put(pid, new Integer(level));
                order.add(new Integer(level));
            }


            @Override
            public boolean skipBranch(String pid, int level) {
                return  (level >= 3) ? true: false;
            }

            @Override
            public boolean breakProcessing(String pid, int level) {
                return  false;
            }
        });


        Assert.assertEquals(Arrays.asList(new Integer(0),new Integer(1), new Integer(2),new Integer(2),new Integer(2)), order);
        Assert.assertEquals(new Integer(0),mapping.get("uuid:ae876087-435d-11dd-b505-00145e5790ea"));
        Assert.assertEquals(new Integer(1),mapping.get("uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6"));
        Assert.assertEquals(new Integer(2), mapping.get("uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6"));
        Assert.assertEquals(new Integer(2), mapping.get("uuid:983a4660-938d-11dc-913a-000d606f5dc6"));
        Assert.assertEquals(new Integer(2),mapping.get("uuid:53255e00-938a-11dc-8b44-000d606f5dc6"));
    }


    
    @Test
    public void testProcessTree_SkipBranch2() throws IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException {
        StatisticsAccessLog aclog = EasyMock.createMock(StatisticsAccessLog.class);

        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        
        final List<Integer> order = new ArrayList<Integer>();
        
        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), aclog)
        .addMockedMethod("getRelsExt")
        .createMock();
        
        narodniListyRelsExt(fa);
        
        replay(fa,aclog);

        fa.processSubtree("uuid:ae876087-435d-11dd-b505-00145e5790ea", new TreeNodeProcessor() {
            
            @Override
            public void process(String pid, int level) throws ProcessSubtreeException {
                mapping.put(pid, new Integer(level));
                order.add(new Integer(level));
            }


            @Override
            public boolean skipBranch(String pid, int level) {
                // vyzobu dve cisla - jedno necham
                return ("uuid:983a4660-938d-11dc-913a-000d606f5dc6".equals(pid) || "uuid:53255e00-938a-11dc-8b44-000d606f5dc6".equals(pid));
            }

            @Override
            public boolean breakProcessing(String pid, int level) {
                return  false;
            }
        });
        

        Assert.assertEquals(Arrays.asList(new Integer(0),new Integer(1), new Integer(2),
                new Integer(3),
                new Integer(3),
                new Integer(3),
                new Integer(3),

                new Integer(3),
                new Integer(3),
                new Integer(3),
                new Integer(3),

                new Integer(3),
                new Integer(3),
                new Integer(3),
                new Integer(3),

                new Integer(3),
                new Integer(3),
                new Integer(3),
                new Integer(3)
            ), order);
        
    }

    @Test
    public void testProcessTree_StackAware() throws IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException {
        StatisticsAccessLog aclog = EasyMock.createMock(StatisticsAccessLog.class);
        final List<String> alist = new ArrayList<String>();
        
        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), aclog)
        .addMockedMethod("getRelsExt")
        .createMock();
        
        narodniListyRelsExt(fa);
        
        replay(fa,aclog);

        class T implements TreeNodeProcessor, TreeNodeProcessStackAware {

            @Override
            public void changeProcessingStack(Stack<String> pidStack) {
                alist.add(pidStack.toString());
            }

            @Override
            public void process(String pid, int level) throws ProcessSubtreeException {
                // TODO Auto-generated method stub
            }

            @Override
            public boolean skipBranch(String pid, int level) {
                // TODO Auto-generated method stub
                return false;
            }
            
            @Override
            public boolean breakProcessing(String pid, int level) {
                // TODO Auto-generated method stub
                return false;
            }
        }
        fa.processSubtree("uuid:ae876087-435d-11dd-b505-00145e5790ea", new T());
        Assert.assertTrue(alist.remove("[]"));

        Assert.assertFalse(alist.remove("[]"));
        
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea]"));
        
        Assert.assertFalse(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea]"));

        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6]"));
        
        Assert.assertFalse(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6]"));

        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));
        
        Assert.assertFalse(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6]"));

        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:983a4660-938d-11dc-913a-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:983a4660-938d-11dc-913a-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:983a4660-938d-11dc-913a-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:983a4660-938d-11dc-913a-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:983a4660-938d-11dc-913a-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:983a4660-938d-11dc-913a-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:983a4660-938d-11dc-913a-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:983a4660-938d-11dc-913a-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:983a4660-938d-11dc-913a-000d606f5dc6]"));
        
        Assert.assertFalse(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:983a4660-938d-11dc-913a-000d606f5dc6]"));
        
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:53255e00-938a-11dc-8b44-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:53255e00-938a-11dc-8b44-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:53255e00-938a-11dc-8b44-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:53255e00-938a-11dc-8b44-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:53255e00-938a-11dc-8b44-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:53255e00-938a-11dc-8b44-000d606f5dc6]"));
        Assert.assertTrue(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:53255e00-938a-11dc-8b44-000d606f5dc6]"));
        
        Assert.assertFalse(alist.remove("[uuid:ae876087-435d-11dd-b505-00145e5790ea, uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6, uuid:53255e00-938a-11dc-8b44-000d606f5dc6]"));
    }

}
