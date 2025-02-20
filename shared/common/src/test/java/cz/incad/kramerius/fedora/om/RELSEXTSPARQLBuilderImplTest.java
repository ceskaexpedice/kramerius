package cz.incad.kramerius.fedora.om;

import cz.incad.kramerius.fedora.om.impl.RELSEXTSPARQLBuilderImpl;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;

/**
 * Created by pstastny on 11/1/2017.
 */
public class RELSEXTSPARQLBuilderImplTest extends TestCase{


    public void testRelsExtSParql() throws IOException, RepositoryException, SAXException, ParserConfigurationException {
        URL resource = RELSEXTSPARQLBuilderImplTest.class.getClassLoader().getResource("cz/incad/kramerius/fedora/om/5035a48a-5e2e-486c-8127-2fa650842e46.xml");
        String s = IOUtils.toString(resource.openStream(), "UTF-8");
        RELSEXTSPARQLBuilderImpl impl = new RELSEXTSPARQLBuilderImpl();
        String sparqlProps = impl.sparqlProps(s, null);
        System.out.println(sparqlProps);
        Assert.assertTrue(sparqlProps.contains("<> <info:fedora/fedora-system:def/model#hasModel> <model:monograph>."));
        Assert.assertTrue(sparqlProps.contains("<> <http://www.openarchives.org/OAI/2.0/#itemID> \"uuid:5035a48a-5e2e-486c-8127-2fa650842e46\"."));

        for(int i=1;i<=36;i++) {
            Assert.assertTrue(sparqlProps.contains("<> <http://www.nsdl.org/ontologies/relationships#hasPage> <#page"+i+">."));
        }
    }

}
