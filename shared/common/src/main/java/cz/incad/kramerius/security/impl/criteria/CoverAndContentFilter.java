package cz.incad.kramerius.security.impl.criteria;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.security.*;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * CoverAndContentFilter
 *
 * Page types FrontCover, TableOfContents, FrontJacket, TitlePage and jacket
 * are copyright free (uncommercial and library usage)
 *
 * @author Martin Rumanek
 */
public class CoverAndContentFilter extends AbstractCriterium implements RightCriterium {

    private static final Logger LOGGER = java.util.logging.Logger.getLogger(CoverAndContentFilter.class.getName());
    private static XPathExpression modsTypeExpr = null;
    private static final List<String> allowedPageTypes = Arrays.asList(
            "FrontCover", "TableOfContents", "FrontJacket", "TitlePage", "jacket"
    ).stream().map(String::toLowerCase).collect(Collectors.toList());

    @Override
    public EvaluatingResultState evalute() throws RightCriteriumException {
        try {
            FedoraAccess fedoraAccess = getEvaluateContext().getFedoraAccess();
            //getEvaluateContext().getSolrAccess();
            String pid = getEvaluateContext().getRequestedPid();
            if (!pid.equals(SpecialObjects.REPOSITORY.getPid())) {
                if ("page".equals(fedoraAccess.getKrameriusModelName(pid))) {
                    Document mods = XMLUtils.parseDocument(
                            fedoraAccess.getDataStream(pid, "BIBLIO_MODS"), true);
                    return checkTypeElement(mods);
                } else {
                    return EvaluatingResultState.NOT_APPLICABLE;
                }
            } else {
                return EvaluatingResultState.TRUE;
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return EvaluatingResultState.NOT_APPLICABLE;
        }
    }

    private EvaluatingResultState checkTypeElement(Document mods) throws IOException {
        try {
            if (modsTypeExpr == null)
                initModsTypeExpr();
            String type = modsTypeExpr.evaluate(mods);
            if (allowedPageTypes.contains(type.toLowerCase())) {
                return EvaluatingResultState.TRUE;
            } else {
                return EvaluatingResultState.NOT_APPLICABLE;
            }
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    private void initModsTypeExpr() throws IOException {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new FedoraNamespaceContext());
            modsTypeExpr = xpath.compile("/mods:modsCollection/mods:mods/mods:part/@type");
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public EvaluatingResultState mockEvaluate(DataMockExpectation dataMockExpectation) throws RightCriteriumException {
        switch (dataMockExpectation) {
            case EXPECT_DATA_VAUE_EXISTS: return EvaluatingResultState.TRUE;
            case EXPECT_DATA_VALUE_DOESNTEXIST: return EvaluatingResultState.NOT_APPLICABLE;
        }
        return EvaluatingResultState.NOT_APPLICABLE;
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
