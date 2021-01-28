package cz.incad.kramerius.security.impl.criteria;

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaDNNTUtils.*;


import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.utils.solr.SolrUtils;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

// dnnt pro zobrazeni
public class ReadDNNTFlag extends AbstractCriterium {

    public transient static final Logger LOGGER = Logger.getLogger(ReadDNNTFlag.class.getName());

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
                        String val = SolrUtils.disectDNNTFlag(doc.getDocumentElement());
                        return (val !=  null && val.equals("true")) ? EvaluatingResultState.TRUE : EvaluatingResultState.NOT_APPLICABLE;

                    } else return EvaluatingResultState.NOT_APPLICABLE;

            } else return EvaluatingResultState.NOT_APPLICABLE;
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
        return false;
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
