package cz.incad.kramerius.security.impl.criteria.utils;

import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.SpecialObjects;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.core.repository.KnownDatastreams;
import org.ceskaexpedice.akubra.core.repository.RepositoryNamespaceContext;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.w3c.dom.Document;

import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CriteriaRELSEXTUtils {
	
	public static final Logger LOGGER = Logger.getLogger(CriteriaRELSEXTUtils.class.getName());

    protected static Object valueFromRELSEXT(Document relsExt, String path) throws XPathExpressionException {
        XPathFactory xpfactory = XPathFactory.newInstance();
        XPath xpath = xpfactory.newXPath();
        xpath.setNamespaceContext(new RepositoryNamespaceContext());
        XPathExpression expr = xpath.compile(path);
        return expr.evaluate(relsExt, XPathConstants.STRING);
    }

    static EvaluatingResultState checkValue(Document relsExt, String path, String expectedValue) throws IOException {
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
            AkubraRepository akubraRepository = ctx.getAkubraRepository();
            String requestedPID = ctx.getRequestedPid();
            if (!requestedPID.equals(SpecialObjects.REPOSITORY.getPid())) {
                Document relsExt = akubraRepository.getDatastreamContent(requestedPID, KnownDatastreams.RELS_EXT).asDom(false);
                return checkValue(relsExt, path,expectedValue);
            } else return EvaluatingResultState.NOT_APPLICABLE;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return EvaluatingResultState.NOT_APPLICABLE;
        }
    }
}