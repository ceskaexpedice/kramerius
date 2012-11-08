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
package cz.incad.kramerius.service;

import static cz.incad.kramerius.fedora.impl.DataPrepare.drobnustkyRelsExt;
import static cz.incad.kramerius.fedora.impl.DataPrepare.narodniListyRelsExt;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.service.impl.ReplicationServiceImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;

/**
 * @author pavels
 *
 */
public class ReplicationsTest {

    @Test
    public void testPrepareExportDrobnustky() throws IOException, ParserConfigurationException, SAXException, LexerException, ReplicateException {
        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance())
        .addMockedMethod("getRelsExt")
        .createMock();
        
        drobnustkyRelsExt(fa);

        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        Set<String> keys = DataPrepare.PATHS_MAPPING.keySet();
        for (String key : keys) {
            EasyMock.expect(solrAccess.getPath(key)).andReturn(new ObjectPidsPath[] { DataPrepare.PATHS_MAPPING.get(key) }).anyTimes();
        }

        replay(fa, solrAccess);

        Injector injector = Guice.createInjector(new _Module(fa,  solrAccess));
        ReplicationService replicationService = injector.getInstance(ReplicationService.class);

        List<String> prepareExport = replicationService.prepareExport("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6");
        Assert.assertTrue(DataPrepare.DROBNUSTKY_PIDS.length == prepareExport.size());
        while(!prepareExport.isEmpty()) {
            String topPid = prepareExport.remove(0);
            Assert.assertTrue(Arrays.asList(DataPrepare.DROBNUSTKY_PIDS).contains(topPid));
        }
    }

    @Test
    public void testPrepareExportNarodniListy() throws IOException, ParserConfigurationException, SAXException, LexerException, ReplicateException {
        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance())
        .addMockedMethod("getRelsExt")
        .createMock();
        
        narodniListyRelsExt(fa);

        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        Set<String> keys = DataPrepare.PATHS_MAPPING.keySet();
        for (String key : keys) {
            EasyMock.expect(solrAccess.getPath(key)).andReturn(new ObjectPidsPath[] { DataPrepare.PATHS_MAPPING.get(key) }).anyTimes();
        }

        replay(fa, solrAccess);

        Injector injector = Guice.createInjector(new _Module(fa,  solrAccess));
        ReplicationService replicationService = injector.getInstance(ReplicationService.class);

        // exportuju pouze jedno cislo periodika 
        List<String> prepareExport = replicationService.prepareExport("uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6");
        Assert.assertTrue(prepareExport.contains("uuid:ae876087-435d-11dd-b505-00145e5790ea"));
        Assert.assertTrue(prepareExport.contains("uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6"));
        Assert.assertTrue(prepareExport.contains("uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6"));

        Assert.assertTrue(prepareExport.contains("uuid:94a3ed60-92d6-11dc-beb4-000d606f5dc6"));
        Assert.assertTrue(prepareExport.contains("uuid:b3987e10-91f6-11dc-96f6-000d606f5dc6"));
        Assert.assertTrue(prepareExport.contains("uuid:94a3ed60-92d6-11dc-93df-000d606f5dc6"));
        Assert.assertTrue(prepareExport.contains("uuid:b3a21b00-91f6-11dc-b8b2-000d606f5dc6"));
        Assert.assertTrue(prepareExport.contains("uuid:94a68570-92d6-11dc-be5a-000d606f5dc6"));
        Assert.assertTrue(prepareExport.contains("uuid:b3ab42c0-91f6-11dc-b83a-000d606f5dc6"));
        Assert.assertTrue(prepareExport.contains("uuid:94a8f670-92d6-11dc-9104-000d606f5dc6"));
        Assert.assertTrue(prepareExport.contains("uuid:b3d89450-91f6-11dc-98d0-000d606f5dc6"));
        Assert.assertTrue(prepareExport.contains("uuid:94b4b640-92d6-11dc-b0a2-000d606f5dc6"));
        Assert.assertTrue(prepareExport.contains("uuid:b3e23140-91f6-11dc-a406-000d606f5dc6"));
        Assert.assertTrue(prepareExport.contains("uuid:94b74e50-92d6-11dc-8465-000d606f5dc6"));
        Assert.assertTrue(prepareExport.contains("uuid:b3ebce30-91f6-11dc-812e-000d606f5dc6"));
        Assert.assertTrue(prepareExport.contains("uuid:94b9bf50-92d6-11dc-82e2-000d606f5dc6"));
        Assert.assertTrue(prepareExport.contains("uuid:b3f2fa20-91f6-11dc-9cd2-000d606f5dc6"));
        Assert.assertTrue(prepareExport.contains("uuid:94bbe230-92d6-11dc-924c-000d606f5dc6"));
    }

    class _Module extends AbstractModule {

        private FedoraAccess fedoraAccess;
        private SolrAccess solrAccess;


        public _Module( FedoraAccess fedoraAccess, SolrAccess solrAccess) {
            super();
            this.fedoraAccess = fedoraAccess;
            this.solrAccess = solrAccess;
        }

        @Override
        protected void configure() {
            bind(FedoraAccess.class).annotatedWith(Names.named("securedFedoraAccess")).toInstance(this.fedoraAccess);
            bind(SolrAccess.class).toInstance(this.solrAccess);
            bind(ReplicationService.class).to(ReplicationServiceImpl.class);
        }

    }

}
