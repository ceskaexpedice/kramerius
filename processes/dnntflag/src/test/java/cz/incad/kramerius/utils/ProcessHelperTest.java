package cz.incad.kramerius.utils;

import cz.incad.kramerius.ProcessHelper;
import cz.incad.kramerius.SolrAccess;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class ProcessHelperTest {

    @Test
    public void testQueryOfPidsOfDescendantsProducer2() {
        String onlyDescendantsQuery = new ProcessHelper.PidsOfDescendantsProducer("uuid:3b708d50-2b21-11dd-80c8-000d606f5dc6", null, true).getQ();
        System.out.println(onlyDescendantsQuery);
    }

    @Test
    public void testQueryOfPidsOfDescendantsProducer() {
        String onlyDescendantsQuery = new ProcessHelper.PidsOfDescendantsProducer("uuid:4b2d6a2d-e531-4871-bf3b-16f30769e7ed", null, true).getQ();
        Assert.assertEquals("own_pid_path:uuid\\:4b2d6a2d-e531-4871-bf3b-16f30769e7ed/* OR own_pid_path:*/uuid\\:4b2d6a2d-e531-4871-bf3b-16f30769e7ed/* ", onlyDescendantsQuery);

        String allPaths = new ProcessHelper.PidsOfDescendantsProducer("uuid:4b2d6a2d-e531-4871-bf3b-16f30769e7ed", null, false).getQ();
        Assert.assertEquals("pid_paths:uuid\\:4b2d6a2d-e531-4871-bf3b-16f30769e7ed/* OR pid_paths:*/uuid\\:4b2d6a2d-e531-4871-bf3b-16f30769e7ed/* ", allPaths);
    }


    @Test
    public void testEffectiveQueryOfPidsOfDescendantsProducer() throws ParserConfigurationException, IOException, SAXException {
        InputStream is = ProcessHelperTest.class.getClassLoader().getResourceAsStream("solr.xml");
        Assert.assertNotNull(is);
        Document parseDocument = XMLUtils.parseDocument(is);
        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        EasyMock.expect(solrAccess.getSolrDataByPid("uuid:4b2d6a2d-e531-4871-bf3b-16f30769e7ed", "own_pid_path,pid_paths")).andReturn(parseDocument).anyTimes();
        EasyMock.replay(solrAccess);

        String onlyDescendantsQuery = new ProcessHelper.EffectivePidsOfDescendantsProducer("uuid:4b2d6a2d-e531-4871-bf3b-16f30769e7ed", solrAccess, true).getQ();
        Assert.assertEquals("own_pid_path.children:\"uuid:8f4e58de-b27b-4cf4-b8ef-c4d428f593d4/uuid:4b2d6a2d-e531-4871-bf3b-16f30769e7ed\"", onlyDescendantsQuery);


        String allPaths = new ProcessHelper.EffectivePidsOfDescendantsProducer("uuid:4b2d6a2d-e531-4871-bf3b-16f30769e7ed", solrAccess, false).getQ();
        Assert.assertEquals("pid_paths.children:\"uuid:8f4e58de-b27b-4cf4-b8ef-c4d428f593d4/uuid:4b2d6a2d-e531-4871-bf3b-16f30769e7ed\" OR pid_paths.children:\"uuid:005704f1-613e-497c-b683-4ab50be13792/uuid:8f4e58de-b27b-4cf4-b8ef-c4d428f593d4/uuid:4b2d6a2d-e531-4871-bf3b-16f30769e7ed\" OR pid_paths.children:\"uuid:038179fc-80ae-4842-9a43-12577e8407b9/uuid:005704f1-613e-497c-b683-4ab50be13792/uuid:8f4e58de-b27b-4cf4-b8ef-c4d428f593d4/uuid:4b2d6a2d-e531-4871-bf3b-16f30769e7ed\" OR pid_paths.children:\"uuid:71174c16-98ca-4d39-8893-543b6fd2af74/uuid:038179fc-80ae-4842-9a43-12577e8407b9/uuid:005704f1-613e-497c-b683-4ab50be13792/uuid:8f4e58de-b27b-4cf4-b8ef-c4d428f593d4/uuid:4b2d6a2d-e531-4871-bf3b-16f30769e7ed\"", allPaths);
    }
}
