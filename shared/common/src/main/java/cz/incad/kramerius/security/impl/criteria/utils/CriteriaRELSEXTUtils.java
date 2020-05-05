package cz.incad.kramerius.security.impl.criteria.utils;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTFlag;
import org.w3c.dom.Document;

import javax.xml.xpath.*;
import java.io.IOException;
import java.util.logging.Level;

public class CriteriaRELSEXTUtils {

    protected static Object valueFromRELSEXT(Document relsExt, String path) throws XPathExpressionException {
        XPathFactory xpfactory = XPathFactory.newInstance();
        XPath xpath = xpfactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile(path);
        return expr.evaluate(relsExt, XPathConstants.STRING);
    }

    protected static EvaluatingResultState checkValue(Document relsExt, String path, String expectedValue) throws IOException {
        try {
            Object policy = valueFromRELSEXT(relsExt, path);
            if ((policy != null) && (policy.toString().trim().equals(expectedValue))) {
                return EvaluatingResultState.FALSE;
            } else {
                return EvaluatingResultState.TRUE;
            }

        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    public static EvaluatingResultState evaluateState(RightCriteriumContext ctx, String path, String expectedValue) {
        try {
            FedoraAccess fa = ctx.getFedoraAccess();
            String requestedPID = ctx.getRequestedPid();
            if (!requestedPID.equals(SpecialObjects.REPOSITORY.getPid())) {
                Document relsExt = fa.getRelsExt(requestedPID);
                return checkValue(relsExt, path,expectedValue);
            } else return EvaluatingResultState.TRUE;
        } catch (IOException e) {
            ReadDNNTFlag.LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return EvaluatingResultState.TRUE;
        }
    }
}
