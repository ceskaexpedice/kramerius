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
/**
 * 
 */
package org.kramerius.consistency;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;
import static org.kramerius.fedora.impl.ImportDataPrepare.narodniListyRelsExt;
import static org.kramerius.fedora.impl.ImportDataPrepare.notConsistentNarodniListyRelsExt;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import com.google.inject.Provides;
import com.google.inject.name.Named;
import cz.incad.kramerius.fedora.impl.FedoraAccessAkubraImpl;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.impl.HazelcastServerNode;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import junit.framework.Assert;

import org.easymock.EasyMock;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.kramerius.consistency.Consistency.NotConsistentRelation;
import org.kramerius.fedora.impl.ImportDataPrepare;
import org.xml.sax.SAXException;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.pid.LexerException;

/**
 * @author pavels
 */
@Ignore
public class ConsistencyTest {

    @Test
    public void shouldPassProcess() throws IOException, ProcessSubtreeException, LexerException, ParserConfigurationException, SAXException, RepositoryException {
        AggregatedAccessLogs acLog = EasyMock.createMock(AggregatedAccessLogs.class);
        ProcessingIndexFeeder feeder = createMock(ProcessingIndexFeeder.class);
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();

        HazelcastServerNode.ensureHazelcastNode();
        FedoraAccessAkubraImpl fa4 = createMockBuilder(FedoraAccessAkubraImpl.class)
                .withConstructor( feeder, acLog, cacheManager)
                .addMockedMethod("getRelsExt")
                .addMockedMethod("isStreamAvailable")
                .addMockedMethod("isObjectAvailable")
                .createMock();


        narodniListyRelsExt(fa4);

        for (int i = 0; i < ImportDataPrepare.NARODNI_LISTY.length; i++) {
            EasyMock.expect(fa4.isObjectAvailable(ImportDataPrepare.NARODNI_LISTY[i])).andReturn(true).anyTimes();
        }


        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        Set<String> keys = ImportDataPrepare.PATHS_MAPPING.keySet();
        for (String key : keys) {
            EasyMock.expect(solrAccess.getPidPaths(key)).andReturn(new ObjectPidsPath[] { ImportDataPrepare.PATHS_MAPPING.get(key) }).anyTimes();
        }
        

        replay(fa4,feeder, solrAccess);

        Consistency consistency = new Consistency();
        Injector injector = Guice.createInjector(new _Module(fa4,  solrAccess));
        injector.injectMembers(consistency);
        
        List<NotConsistentRelation> notConsitent = consistency.checkConsitency("uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6", false);

        Assert.assertTrue(notConsitent.isEmpty());
    }


    @Test
    public void shouldFailProcess() throws IOException, ProcessSubtreeException, LexerException, ParserConfigurationException, SAXException, RepositoryException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        ProcessingIndexFeeder feeder = createMock(ProcessingIndexFeeder.class);
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();
        HazelcastServerNode.ensureHazelcastNode();

        FedoraAccessAkubraImpl fa4 = createMockBuilder(FedoraAccessAkubraImpl.class)
                .withConstructor( feeder, acLog, cacheManager)
                .addMockedMethod("getRelsExt")
                .addMockedMethod("isStreamAvailable")
                .addMockedMethod("isObjectAvailable")
                .createMock();

        notConsistentNarodniListyRelsExt(fa4);

        for (int i = 0; i < ImportDataPrepare.NARODNI_LISTY.length; i++) {
            EasyMock.expect(fa4.isObjectAvailable(ImportDataPrepare.NARODNI_LISTY[i])).andReturn(true).anyTimes();
        }


        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        Set<String> keys = ImportDataPrepare.PATHS_MAPPING.keySet();
        for (String key : keys) {
            EasyMock.expect(solrAccess.getPidPaths(key)).andReturn(new ObjectPidsPath[] { ImportDataPrepare.PATHS_MAPPING.get(key) }).anyTimes();
        }
        

        replay(fa4,feeder, solrAccess);

        Consistency consistency = new Consistency();
        Injector injector = Guice.createInjector(new _Module(fa4,  solrAccess));
        injector.injectMembers(consistency);
        
        List<NotConsistentRelation> notConsitent = consistency.checkConsitency("uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6", false);
        Assert.assertTrue(!notConsitent.isEmpty());
        Assert.assertTrue(notConsitent.size() == ImportDataPrepare.NARODNI_LISTY_NOT_EXISTS.length); 
        
    }


    static class _Module extends AbstractModule {

        private FedoraAccess fedoraAccess;
        private SolrAccess solrAccess;
        
        
        public _Module(FedoraAccess fedoraAccess, SolrAccess solrAccess) {
            super();
            this.fedoraAccess = fedoraAccess;
            this.solrAccess = solrAccess;
        }


        @Provides
        @Named("rawFedoraAccess")
        public FedoraAccess getFedoraAccess() {
            return this.fedoraAccess;
        }

        @Provides
        public SolrAccess getSolrAccess() {
            return solrAccess;
        }

        @Override
        protected void configure() {
        }
    }



}
