package cz.incad.kramerius.fedora.impl;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import junit.framework.Assert;
import org.easymock.EasyMock;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.core.EhcacheManager;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

import static cz.incad.kramerius.fedora.impl.DataPrepare.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

/**
 * Created by pstastny on 11/5/2017.
 */
public class FedoraAccess4xImplTest {

    @Test
    public void testFindFirstViewablePid_good() throws IOException, ParserConfigurationException, SAXException, LexerException {
        ProcessingIndexFeeder feeder = createMock(ProcessingIndexFeeder.class);
        StatisticsAccessLog aclog = EasyMock.createMock(StatisticsAccessLog.class);
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();
        // test correct data - IMG_FULL in pages
        FedoraAccessAkubraImpl fa = createMockBuilder(FedoraAccessAkubraImpl.class)
                .withConstructor(KConfiguration.getInstance(), feeder, aclog, cacheManager)
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
        ProcessingIndexFeeder feeder = createMock(ProcessingIndexFeeder.class);
        StatisticsAccessLog aclog = EasyMock.createMock(StatisticsAccessLog.class);
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();

        // test correct data - IMG_FULL in pages
        FedoraAccessAkubraImpl fa = createMockBuilder(FedoraAccessAkubraImpl.class)
                .withConstructor(KConfiguration.getInstance(), feeder, aclog, cacheManager)
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
        ProcessingIndexFeeder feeder = createMock(ProcessingIndexFeeder.class);
        final Map<String, Integer> mapping = new HashMap<>();

        final List<Integer> order = new ArrayList<>();

        StatisticsAccessLog aclog = EasyMock.createMock(StatisticsAccessLog.class);
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();

        FedoraAccessAkubraImpl fa = createMockBuilder(FedoraAccessAkubraImpl.class)
                .withConstructor(KConfiguration.getInstance(), feeder, aclog, cacheManager)
                .addMockedMethod("getRelsExt")
                .addMockedMethod("isStreamAvailable")
                .createMock();


        narodniListyRelsExt(fa);

        replay(fa,feeder,aclog);

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
        ProcessingIndexFeeder feeder = createMock(ProcessingIndexFeeder.class);
        StatisticsAccessLog aclog = EasyMock.createMock(StatisticsAccessLog.class);
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();

        final Map<String, Integer> mapping = new HashMap<String, Integer>();

        final List<Integer> order = new ArrayList<Integer>();

        FedoraAccessAkubraImpl fa = createMockBuilder(FedoraAccessAkubraImpl.class)
                .withConstructor(KConfiguration.getInstance(), feeder, aclog,cacheManager)
                .addMockedMethod("getRelsExt")
                .addMockedMethod("isStreamAvailable")
                .createMock();

        narodniListyRelsExt(fa);

        replay(fa,feeder, aclog);

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
                return false;
            }
        });
    }
}
