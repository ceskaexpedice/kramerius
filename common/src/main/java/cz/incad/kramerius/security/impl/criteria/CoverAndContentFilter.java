package cz.incad.kramerius.security.impl.criteria;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CoverAndContentFilter
 *
 * Page types FrontCover and TableOfContents are copyright free (uncommercial and library usage)
 *
 * @author Martin Rumanek
 */
public class CoverAndContentFilter extends AbstractCriterium implements RightCriterium {

    Logger LOGGER = java.util.logging.Logger.getLogger(CoverAndContentFilter.class.getName());

    @Override
    public EvaluatingResult evalute() throws RightCriteriumException {
        try {
            FedoraAccess fedoraAccess = getEvaluateContext().getFedoraAccess();
            getEvaluateContext().getSolrAccess();
            String pid = getEvaluateContext().getRequestedPid();
            if (!pid.equals(SpecialObjects.REPOSITORY.getPid())) {
                if ("page".equals(fedoraAccess.getKrameriusModelName(pid))) {
                    Document mods = XMLUtils.parseDocument(
                            fedoraAccess.getDataStream(pid, "BIBLIO_MODS"), true);
                    return checkTypeElement(mods);
                } else {
                    return EvaluatingResult.NOT_APPLICABLE;
                }
            } else {
                return EvaluatingResult.TRUE;
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return EvaluatingResult.NOT_APPLICABLE;
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return EvaluatingResult.NOT_APPLICABLE;
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return EvaluatingResult.NOT_APPLICABLE;
        }
    }

    private EvaluatingResult checkTypeElement(Document relsExt) throws IOException {
        try {
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();
            xpath.setNamespaceContext(new FedoraNamespaceContext());
            XPathExpression expr = xpath.compile("/mods:modsCollection/mods:mods/mods:part/@type");
            String type = expr.evaluate(relsExt);
            if (Arrays.asList("FrontCover", "TableOfContents", "FrontJacket", "jacket").contains(type)) {
                return EvaluatingResult.TRUE;
            } else {
                return EvaluatingResult.NOT_APPLICABLE;
            }
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.NORMAL;
    }

    @Override
    public boolean isParamsNecessary() {
        return false;
    }

    @Override
    public SecuredActions[] getApplicableActions() {
        return new SecuredActions[]{SecuredActions.READ};
    }

}
