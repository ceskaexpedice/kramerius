package cz.incad.kramerius.security.impl.criteria;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaDNNTUtils;
import cz.incad.kramerius.utils.solr.SolrUtils;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReadDNNTLabels extends AbstractCriterium {

    public static final String DNNT_LABELS = "dnnt-labels";

    public transient static final Logger LOGGER = Logger.getLogger(ReadDNNTLabels.class.getName());

    @Override
    public EvaluatingResultState evalute() throws RightCriteriumException {
        try {
            RightCriteriumContext ctx =  getEvaluateContext();
            String pid = ctx.getRequestedPid();
            // only for READ action
            if (!SpecialObjects.isSpecialObject(pid)) {

                if (!pid.equals(SpecialObjects.REPOSITORY.getPid())) {
                    SolrAccess solrAccess = ctx.getSolrAccess();
                    Document doc = solrAccess.getSolrDataDocument(pid);
                    String label = CriteriaDNNTUtils.getMatchedLabel(doc,  getObjects());
                    if (label != null) {
                        // select label
                        getEvaluateContext().getEvaluateInfoMap().put(ReadDNNTLabels.DNNT_LABELS, label);
                        return EvaluatingResultState.TRUE;
                    }
                }
            }
            return EvaluatingResultState.NOT_APPLICABLE;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return EvaluatingResultState.NOT_APPLICABLE;
        }
    }

    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.DNNT_EXCLUSIVE_MIN;
    }

    @Override
    public boolean isParamsNecessary() {
        return true;
    }

    @Override
    public SecuredActions[] getApplicableActions() {
        return  new SecuredActions[] {SecuredActions.READ};
    }

    @Override
    public boolean isRootLevelCriterum() {
        return false;
    }

    @Override
    public void checkPrecodition(RightsManager manager) throws CriteriaPrecoditionException {
        //checkContainsCriteriumPDFDNNT(this.evalContext, manager);
    }
}
