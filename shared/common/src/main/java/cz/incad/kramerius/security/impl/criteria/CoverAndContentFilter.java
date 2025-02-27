package cz.incad.kramerius.security.impl.criteria;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.utils.solr.SolrUtils;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryNamespaceContext;
import org.ceskaexpedice.akubra.utils.RelsExtUtils;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
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
    public EvaluatingResultState evalute(Right right) throws RightCriteriumException {
        try {
            AkubraRepository akubraRepository = getEvaluateContext().getAkubraRepository();
            //getEvaluateContext().getSolrAccess();
            String pid = getEvaluateContext().getRequestedPid();
            if (!pid.equals(SpecialObjects.REPOSITORY.getPid())) {
                if ("page".equals(RelsExtUtils.getModelName(pid, akubraRepository))) {
                    Document mods = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_MODS).asDom(true);
                    if (checkTypeElement(mods).equals(EvaluatingResultState.TRUE))
                        return isNotPeriodical(pid);
                    return checkTypeElement(mods);
                } else {
                    return EvaluatingResultState.NOT_APPLICABLE;
                }
            } else {
                return EvaluatingResultState.NOT_APPLICABLE;
            }
        } catch (IOException e) {
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

    private EvaluatingResultState isNotPeriodical(String pid) throws IOException {
        try {
            //SolrAccess solrAccess = getEvaluateContext().getSolrAccess();
            SolrAccess solrAccess = getEvaluateContext().getSolrAccessNewIndex();
            Document doc = solrAccess.getSolrDataByPid(pid); //SolrUtils.getSolrDataInternal(SolrUtils.UUID_QUERY + "\"" + pid + "\"");
            String rootPID = SolrUtils.disectRootPid(doc);
            doc = solrAccess.getSolrDataByPid(rootPID);
            String rootFedoraModel = SolrUtils.disectFedoraModel(doc);
            if (rootFedoraModel.equals("periodical"))
                return EvaluatingResultState.NOT_APPLICABLE;
            else
                return EvaluatingResultState.TRUE;
        } catch (XPathExpressionException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return EvaluatingResultState.TRUE;
    }

    private void initModsTypeExpr() throws IOException {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new RepositoryNamespaceContext());
            modsTypeExpr = xpath.compile("/mods:modsCollection/mods:mods/mods:part/@type");
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public EvaluatingResultState mockEvaluate(Right right, DataMockExpectation dataMockExpectation) throws RightCriteriumException {
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
        return new SecuredActions[]{SecuredActions.A_READ};
    }
}
