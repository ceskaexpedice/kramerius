package cz.incad.kramerius.security.impl.criteria.utils;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.impl.RightCriteriumContextFactoryImpl;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

import static org.easymock.EasyMock.replay;

public class CriteriaDNNTUtilsTest extends TestCase  {


    @Test
    public void testCriteriaDNNTUtilsTest() throws LexerException, ParserConfigurationException, SAXException, RightCriteriumException, IOException {
        RightCriteriumContext ctx = ctx("uuid:aaa","solr_dnnt.xml");
        EvaluatingResultState evaluatingResultState = CriteriaDNNTUtils.checkDnnt(ctx);
        Assert.assertTrue(evaluatingResultState.equals(EvaluatingResultState.TRUE));
    }

    @Test
    public void testCriteriaDNNTUtilsTest_2() throws LexerException, ParserConfigurationException, SAXException, RightCriteriumException, IOException {
        RightCriteriumContext ctx = ctx("uuid:aaa","solr_wo_dnnt.xml");
        EvaluatingResultState evaluatingResultState = CriteriaDNNTUtils.checkDnnt(ctx);
        Assert.assertTrue(evaluatingResultState.equals(EvaluatingResultState.NOT_APPLICABLE));
    }

    public RightCriteriumContext ctx(String requestedPID, String file) throws IOException, LexerException, ParserConfigurationException, SAXException, RightCriteriumException {
        InputStream is  = CriteriaDNNTUtilsTest.class.getResourceAsStream(file);
        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        EasyMock.expect(solrAccess.getSolrDataDocument(requestedPID)).andReturn(XMLUtils.parseDocument(is)).anyTimes();

        replay(solrAccess);

        RightCriteriumContextFactoryImpl contextFactory = new RightCriteriumContextFactoryImpl();
        contextFactory.setSolrAccess(solrAccess);

        RightCriteriumContext context = contextFactory.create(requestedPID, null, null, "localhost", "127.0.0.1", null);
        return context;

    }


}
