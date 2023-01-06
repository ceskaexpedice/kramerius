package cz.incad.kramerius.security.impl.criteria;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.easymock.EasyMock;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.AbstractObjectPath;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.StreamHeadersObserver;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.fedora.impl.FedoraAccessAkubraImpl;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.impl.HazelcastServerNode;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.LexerException;

import static org.easymock.EasyMock.*;


public class CoverAndContentFilterTest {
	
	
	@Test
	public void testCoverAndContent() throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
		EvaluatingResultState state = mockEvaluate();
		Assert.assertTrue(state == EvaluatingResultState.TRUE);
	}
	
	  public EvaluatingResultState mockEvaluate() throws IOException, 
	  			LexerException, 
	  			ParserConfigurationException, 
	  			SAXException, 
	  			RightCriteriumException {
		  
		  
		  Document solr = XMLUtils.parseDocument(CoverAndContentFilterTest.class.getResourceAsStream("coverandcontent/solr.xml"));
		  Document rootSolr = XMLUtils.parseDocument(CoverAndContentFilterTest.class.getResourceAsStream("coverandcontent/root.solr.xml"));
		  
		  AggregatedAccessLogs acLog = EasyMock.createMock(AggregatedAccessLogs.class);
		  ProcessingIndexFeeder feeder = createMock(ProcessingIndexFeeder.class);
		  CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
		  cacheManager.init();
		  
		  HazelcastServerNode.ensureHazelcastNode();
		  
		  
		  FedoraAccessAkubraImpl fa4 = createMockBuilder(FedoraAccessAkubraImpl.class)
		        .withConstructor( feeder, acLog, cacheManager)
		        .addMockedMethod("getDataStream")
		        .addMockedMethod("getKrameriusModelName", String.class)
		        .createMock();

		  CoverAndContentFilter ca = createMockBuilder(CoverAndContentFilter.class)
				  .addMockedMethod("getEvaluateContext")
				  .createMock();
		  
		  SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
		  EasyMock.expect(solrAccess.getSolrDataByPid("uuid:de1f041f-e2e7-49a6-9836-41ba9b7e46db")).andReturn(solr).anyTimes();
		  EasyMock.expect(solrAccess.getSolrDataByPid("uuid:cee971b0-bfa4-11ec-90bf-5ef3fc9bb22f")).andReturn(rootSolr).anyTimes();
		  
		  
		  RightCriteriumContext ctx = EasyMock.createMock(RightCriteriumContext.class);

		  EasyMock.expect(ctx.getFedoraAccess()).andReturn(fa4).anyTimes();
		  EasyMock.expect(ctx.getSolrAccessNewIndex()).andReturn(solrAccess).anyTimes();
		  EasyMock.expect(ctx.getRequestedPid()).andReturn("uuid:de1f041f-e2e7-49a6-9836-41ba9b7e46db").anyTimes();
		 
		  EasyMock.expect(fa4.getKrameriusModelName("uuid:de1f041f-e2e7-49a6-9836-41ba9b7e46db")).andReturn("page").anyTimes();
		  EasyMock.expect(fa4.getDataStream("uuid:de1f041f-e2e7-49a6-9836-41ba9b7e46db", "BIBLIO_MODS")).andReturn(CoverAndContentFilterTest.class.getResourceAsStream("coverandcontent/mods.xml"));;
		  
		  EasyMock.expect(ca.getEvaluateContext()).andReturn(ctx).anyTimes();
		  

		  EasyMock.replay( acLog, feeder,fa4,ca,solrAccess, ctx);
		  
		  return ca.evalute();
	  }

	  private class TestFA extends FedoraAccessAkubraImpl {



		public TestFA( ProcessingIndexFeeder feeder, AggregatedAccessLogs accessLog,
				CacheManager cacheManager) throws IOException {
			super(  feeder, accessLog, cacheManager);
			// TODO Auto-generated constructor stub
		}

		@Override
		public InputStream getDataStream(String pid, String datastreamName) throws IOException {
			if (pid != null && datastreamName != null) {
				if (pid.equals("uuid:de1f041f-e2e7-49a6-9836-41ba9b7e46db") && datastreamName.equals("BIBLIO_MODS")) {
					return CoverAndContentFilterTest.class.getResourceAsStream("coverandcontent/mods.xml");
				}
			}
			return null;
		}

		  
	  }
//  public EvaluatingResultState mw(String movingWallFromGUI, String requestedPID) throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
//  StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
//  ProcessingIndexFeeder feeder = createMock(ProcessingIndexFeeder.class);
//  CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
//  cacheManager.init();
//
//  HazelcastServerNode.ensureHazelcastNode();
//  FedoraAccessAkubraImpl fa4 = createMockBuilder(FedoraAccessAkubraImpl.class)
//          .withConstructor(KConfiguration.getInstance(), feeder, acLog, cacheManager)
//          .addMockedMethod("getRelsExt")
//          .addMockedMethod("isStreamAvailable")
//          .addMockedMethod("getDC")
//          .addMockedMethod("getBiblioMods")
//          .createMock();
//
//
//  DataPrepare.drobnustkyMODS(fa4);
//  DataPrepare.drobnustkyDCS(fa4);
//
//  DataPrepare.narodniListyMods(fa4);
//  DataPrepare.narodniListyDCs(fa4);
//
//  SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
//  Set<String> keys = DataPrepare.PATHS_MAPPING.keySet();
//  for (String key : keys) {
//      EasyMock.expect(solrAccess.getPidPaths(key)).andReturn(new ObjectPidsPath[] { DataPrepare.PATHS_MAPPING.get(key)}).anyTimes();
//  }
//
//  EasyMock.expect(solrAccess.getSolrDataByPid("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6")).andReturn(null).anyTimes();
//
//  replay(fa4,feeder, solrAccess,acLog);
//
//  RightCriteriumContextFactoryImpl contextFactory = new RightCriteriumContextFactoryImpl();
//  contextFactory.setFedoraAccess(fa4);
//  contextFactory.setSolrAccess(solrAccess);
//
//  RightCriteriumContext context = contextFactory.create(requestedPID, null, null, "localhost", "127.0.0.1");
//  MovingWall wall = new MovingWall();
//
//  String firstPid = requestedPID;
//
//  wall.setCriteriumParamValues(new Object[] {movingWallFromGUI, modeFromGUI, "test", firstPid});
//  wall.setEvaluateContext(context);
//
//  EvaluatingResultState evaluated = wall.evalute();
//  return evaluated;
//}

}
