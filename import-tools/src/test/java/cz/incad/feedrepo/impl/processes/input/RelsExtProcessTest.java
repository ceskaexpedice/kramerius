package cz.incad.feedrepo.impl.processes.input;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.impl.SPARQLBuilderImpl;
import junit.framework.TestCase;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by pstastny on 10/11/2017.
 */
public class RelsExtProcessTest extends TestCase {

    public void testProcessRelsExt() throws IOException, SAXException, ParserConfigurationException, RepositoryException {
        /*
        InputStream is = ProcessFOXMLTestCase.class.getClassLoader().getResourceAsStream("relsext.xml");
        SPARQLBuilderImpl sparqlBuilder = new SPARQLBuilderImpl();
        String props = sparqlBuilder.sparqlProps(is, S -> {
            System.out.println(S);
        });
        System.out.println(props);
        */

    }
}
