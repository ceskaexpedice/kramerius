package cz.incad.kramerius.service.replication;

import com.google.inject.Provider;
import cz.incad.kramerius.service.ReplicateException;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static org.easymock.EasyMock.*;

public class CDKFormatTest {


    @Test
    public void testCDKFormat() throws IOException, SAXException, ParserConfigurationException, ReplicateException, TransformerException {
        HttpServletRequest req = createMock(HttpServletRequest.class);

        EasyMock.expect(req.getHeader("x-forwarded-host")).andReturn(null).anyTimes();
        EasyMock.expect(req.getRequestURL()).andReturn(new StringBuffer("https://k7.inovatika.dev/search/api/v4.6/cdk/solr/select?q=*:*")).anyTimes();

        EasyMock.replay(req);

        InputStream resourceAsStream = CDKFormatTest.class.getResourceAsStream("info.xml");
        Assert.assertNotNull(resourceAsStream);

        CDKFormat format = new CDKFormat();
        format.requestProvider = new TestRequestProvider(req);

        byte[] input = IOUtils.toByteArray(resourceAsStream);
        byte[] output = format.formatFoxmlData(input);

        Document document = XMLUtils.parseDocument(new ByteArrayInputStream(output));

        List<Element> imgThumbs = XMLUtils.getElementsRecursive(document.getDocumentElement(), (elm) -> {
            if (elm.getNodeName().equals("datastream")) {
                String id = elm.getAttribute("ID");
                return id != null && id.equals("IMG_THUMB");
            } else {
                return false;
            }
        });
        Assert.assertTrue(imgThumbs.size() > 0);
        imgThumbs.stream().forEach((thmb) ->{
            List<Element> binaryContent = XMLUtils.getElementsRecursive(thmb, (thmbChld) -> {
                return thmbChld.getNodeName().equals("binaryContent");
            });
            Assert.assertTrue(binaryContent.size() > 0 );
        });


        List<Element> imgFulls = XMLUtils.getElementsRecursive(document.getDocumentElement(), (elm) -> {
            if (elm.getNodeName().equals("datastream")) {
                String id = elm.getAttribute("ID");
                return id != null && id.equals("IMG_FULL");
            } else {
                return false;
            }
        });
        Assert.assertTrue(imgFulls.size() > 0);
        imgFulls.stream().forEach((thmb) ->{

            List<Element> refContent = XMLUtils.getElementsRecursive(thmb, (thmbChld) -> {
                return thmbChld.getNodeName().equals("contentLocation");
            });

            List<String> refs = refContent.stream().map(e -> {
                return e.getAttribute("REF");
            }).collect(Collectors.toList());

            refs.stream().forEach(r-> {
                Assert.assertTrue("https://k7.inovatika.dev/search/api/client/v7.0/items/uuid:09a627a3-73b9-4f14-b437-f75c9f60cd96/image".equals(r));
            });

        });
    }

    class TestRequestProvider implements Provider<HttpServletRequest> {

        HttpServletRequest req;

        public TestRequestProvider(HttpServletRequest req) {
            this.req = req;
        }

        @Override
        public HttpServletRequest get() {
            return req;
        }
    }
}
