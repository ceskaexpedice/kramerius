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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.service.impl.ReplicationServiceImpl;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;

import org.antlr.stringtemplate.StringTemplate;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.fedora.api.FedoraAPIM;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static cz.incad.kramerius.fedora.impl.DataPrepare.drobnustkyRelsExt;
import static cz.incad.kramerius.fedora.impl.DataPrepare.narodniListyRelsExt;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

/**
 * @author pavels
 *
 */
public class ReplicationsTest {

	private Injector injector = null;
	
	public Injector getInjector() {
		return injector;
	}

    @Test
    public void testExportPageDrobnystky() throws IOException, ParserConfigurationException, SAXException, LexerException, ReplicateException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);

        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
            .withConstructor(KConfiguration.getInstance(),acLog)
            .addMockedMethod("getAPIM")
        .createMock();
        
        
        FedoraAPIM fedoraApiM = EasyMock.createMock(FedoraAPIM.class);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        URL resource  = ReplicationsTest.class.getResource("impl/foxml_ext.xml");

        File file = new File(resource.getFile());

        StringTemplate template = new StringTemplate(IOUtils.readAsString(resource.openConnection().getInputStream(), Charset.forName("UTF-8"),true));
        template.setAttribute("imgfile", new File(file.getParentFile(), "img.jpeg").toURI().toURL().toString());

        IOUtils.copyStreams(new ByteArrayInputStream(template.toString().getBytes("UTF-8")), bos);
        
        EasyMock
            .expect(fedoraApiM.export("uuid:43101770-b03b-11dd-8673-000d606f5dc6", "info:fedora/fedora-system:FOXML-1.1", "archive"))
            .andReturn(bos.toByteArray()).anyTimes();
        
        
        EasyMock.expect(fa.getAPIM()).andReturn(fedoraApiM).anyTimes();

        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);

        ServletContext scontext = scontext();
        HttpServletRequest request = request();
        
        
        replay(fa, fedoraApiM, solrAccess,acLog, scontext, request);
        
        
        this.injector = Guice.createInjector(new _Module(fa,  solrAccess,scontext, request));
        ReplicationService replicationService = injector.getInstance(ReplicationService.class);

        byte[] exportedFOXML = replicationService.getExportedFOXML("uuid:43101770-b03b-11dd-8673-000d606f5dc6");

        Document document = XMLUtils.parseDocument(new StringReader(new String(exportedFOXML)), true);
        Element dataStreamVersion = XMLUtils.findElement(document.getDocumentElement(), new XMLUtils.ElementsFilter() {

            @Override
            public boolean acceptElement(Element element) {
                String id = element.getAttribute("ID");
                return id != null && id.equals("SOME_IMAGE.0");
            }
        });
        
        //obsahuje atribut size
        Assert.assertNotNull(dataStreamVersion.hasAttribute("SIZE"));
        
        // neni pritomny element location ale je pritomny elemnet binaryContent
        Assert.assertNull(XMLUtils.findElement(dataStreamVersion, "contentLocation",  dataStreamVersion.getNamespaceURI()));
        Assert.assertNotNull(XMLUtils.findElement(dataStreamVersion, "binaryContent",  dataStreamVersion.getNamespaceURI()));
        
        dataStreamVersion = XMLUtils.findElement(document.getDocumentElement(), new XMLUtils.ElementsFilter() {
            
            @Override
            public boolean acceptElement(Element element) {
                String id = element.getAttribute("ID");
                return id != null && id.equals("SOME_IMAGE.1");
            }
        });
        //obsahuje atribut size
        Assert.assertNotNull(dataStreamVersion.hasAttribute("SIZE"));

        // neni pritomny element location ale je pritomny elemnet binaryContent
        Assert.assertNull(XMLUtils.findElement(dataStreamVersion, "contentLocation",  dataStreamVersion.getNamespaceURI()));
        Assert.assertNotNull(XMLUtils.findElement(dataStreamVersion, "binaryContent",  dataStreamVersion.getNamespaceURI()));

        //Assert.assertNotNull(XMLUtils.findElement(document.getDocumentElement(), "replicatedFrom",  FedoraUtils.RELS_EXT_STREAM));
    }

	private ServletContext scontext() {
		ServletContext scontext = EasyMock.createMock(ServletContext.class);
        EasyMock.expect(scontext.getAttribute(Injector.class.getName())).andAnswer(new IAnswer<Injector>() {

			@Override
			public Injector answer() throws Throwable {
				return ReplicationsTest.this.getInjector();
			}
		}).anyTimes();
		return scontext;
	}

	private HttpServletRequest request() {
		HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getAttribute("PID")).andReturn("uuid:43101770-b03b-11dd-8673-000d606f5dc6").anyTimes();
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost:8080/search")).anyTimes();
        EasyMock.expect(request.getHeader("x-forwarded-host")).andReturn(null).anyTimes();
        return request;
	}

    
    @Test
    public void testPrepareExportDrobnustky() throws IOException, ParserConfigurationException, SAXException, LexerException, ReplicateException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(),acLog)
        .addMockedMethod("getRelsExt")
        .createMock();
        
        drobnustkyRelsExt(fa);

        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        Set<String> keys = DataPrepare.PATHS_MAPPING.keySet();
        for (String key : keys) {
            EasyMock.expect(solrAccess.getPath(key)).andReturn(new ObjectPidsPath[] { DataPrepare.PATHS_MAPPING.get(key) }).anyTimes();
        }

        ServletContext scontext = scontext();
        HttpServletRequest request = request();
        
        replay(fa, solrAccess,acLog, scontext, request);

        this.injector = Guice.createInjector(new _Module(fa,  solrAccess,scontext, request));
        ReplicationService replicationService = injector.getInstance(ReplicationService.class);

        List<String> prepareExport = replicationService.prepareExport("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", false);
        Assert.assertTrue(DataPrepare.DROBNUSTKY_PIDS.length == prepareExport.size());
        while(!prepareExport.isEmpty()) {
            String topPid = prepareExport.remove(0);
            Assert.assertTrue(Arrays.asList(DataPrepare.DROBNUSTKY_PIDS).contains(topPid));
        }
    }

    @Test
    public void testPrepareExportNarodniListy() throws IOException, ParserConfigurationException, SAXException, LexerException, ReplicateException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(),acLog)
        .addMockedMethod("getRelsExt")
        .createMock();
        
        narodniListyRelsExt(fa);

        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        Set<String> keys = DataPrepare.PATHS_MAPPING.keySet();
        for (String key : keys) {
            EasyMock.expect(solrAccess.getPath(key)).andReturn(new ObjectPidsPath[] { DataPrepare.PATHS_MAPPING.get(key) }).anyTimes();
        }

        ServletContext scontext = scontext();
        HttpServletRequest request = request();
        
        
        replay(fa, solrAccess,acLog, scontext, request);

        injector = Guice.createInjector(new _Module(fa,  solrAccess,scontext, request));
        ReplicationService replicationService = injector.getInstance(ReplicationService.class);

        // exportuju pouze jedno cislo periodika 
        List<String> prepareExport = replicationService.prepareExport("uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6", false);
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
        private ServletContext servletContext;
        private HttpServletRequest request;
        
        public _Module( FedoraAccess fedoraAccess, SolrAccess solrAccess, ServletContext servletContext, HttpServletRequest request) {
            super();
            this.fedoraAccess = fedoraAccess;
            this.solrAccess = solrAccess;
            this.servletContext = servletContext;
            this.request = request;
        }

        @Override
        protected void configure() {
            bind(FedoraAccess.class).annotatedWith(Names.named("securedFedoraAccess")).toInstance(this.fedoraAccess);
            bind(SolrAccess.class).toInstance(this.solrAccess);
            bind(ReplicationService.class).to(ReplicationServiceImpl.class);
            bind(ServletContext.class).toInstance(this.servletContext);
        }

        @Provides
        HttpServletRequest getRequest() {
        	return this.request;
        }
    }

}
