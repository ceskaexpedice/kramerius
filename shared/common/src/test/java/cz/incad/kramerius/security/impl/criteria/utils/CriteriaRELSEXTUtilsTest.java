package cz.incad.kramerius.security.impl.criteria.utils;

import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class CriteriaRELSEXTUtilsTest {

    @Test
    public void testCheckValuePublicDocument() throws IOException, SAXException, ParserConfigurationException {

        InputStream resource = CriteriaRELSEXTUtilsTest.class.getResourceAsStream("relsext1.xml");
        Document document = XMLUtils.parseDocument(resource, true);

        String path = "//kramerius:policy/text()";
        String expectedValue = "policy:private";

        // TODO AK_NEW EvaluatingResultState evaluatingResultState = CriteriaRELSEXTUtils.checkValue(document, path, expectedValue);
        //Assert.assertTrue(EvaluatingResultState.TRUE.equals(evaluatingResultState));
    }

    @Test
    public void testCheckValuePrivateDocument() throws IOException, SAXException, ParserConfigurationException {

        InputStream resource = CriteriaRELSEXTUtilsTest.class.getResourceAsStream("relsext2.xml");
        Document document = XMLUtils.parseDocument(resource, true);

        String path = "//kramerius:policy/text()";
        String expectedValue = "policy:private";

        // TODO AK_NEW EvaluatingResultState evaluatingResultState = CriteriaRELSEXTUtils.checkValue(document, path, expectedValue);
        //Assert.assertTrue(EvaluatingResultState.FALSE.equals(evaluatingResultState));
    }

    @Test
    public void testCheckValueNoPolicyDocument() throws IOException, SAXException, ParserConfigurationException {

        InputStream resource = CriteriaRELSEXTUtilsTest.class.getResourceAsStream("relsext3.xml");
        Document document = XMLUtils.parseDocument(resource, true);

        String path = "//kramerius:policy/text()";
        String expectedValue = "policy:private";

        // TODO AK_NEW EvaluatingResultState evaluatingResultState = CriteriaRELSEXTUtils.checkValue(document, path, expectedValue);
        //Assert.assertTrue(EvaluatingResultState.TRUE.equals(evaluatingResultState));
    }

}
