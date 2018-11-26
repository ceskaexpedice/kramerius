package cz.incad.kramerius.indexer.dnnt;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.security.impl.RightCriteriumContextFactoryImpl;
import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DnntSingletonTest extends TestCase {

    public void testSingleton() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        InputStream is = DnntSingletonTest.class.getResourceAsStream("res/rels-ext.xml");
        FedoraAccess fa = EasyMock.createMock(FedoraAccess.class);
        EasyMock.expect(fa.getRelsExt("uuid:xxxx")).andReturn(XMLUtils.parseDocument(is,true)).anyTimes();

        EasyMock.replay(fa);
        String dnnt = DnntSingleton.getInstance().dnnt("uuid:xxxx", fa);
        System.out.println(dnnt);
    }
}
