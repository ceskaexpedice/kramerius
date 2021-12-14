package cz.incad.kramerius.security.impl.criteria;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaDNNTUtils;
import cz.incad.kramerius.security.licenses.License;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO: Rename to ReadLicense
public class ReadDNNTLabels extends AbstractCriterium implements RightCriteriumLabelAware{

    public static final String PROVIDED_BY_DNNT_LABEL = "providedByLabel";

    public transient static final Logger LOGGER = Logger.getLogger(ReadDNNTLabels.class.getName());

    private License license;

    @Override
    public EvaluatingResultState evalute() throws RightCriteriumException {
        try {
            RightCriteriumContext ctx =  getEvaluateContext();
            String pid = ctx.getRequestedPid();
            // only for READ action
            if (!SpecialObjects.isSpecialObject(pid)) {

                if (!pid.equals(SpecialObjects.REPOSITORY.getPid())) {
                    SolrAccess solrAccess = ctx.getSolrAccessNewIndex();
                    Document doc = solrAccess.getSolrDataByPid(pid);

                    boolean applied =  CriteriaDNNTUtils.matchLicense(doc, getLicense());
                    if (applied) {
                        // select label
                        getEvaluateContext().getEvaluateInfoMap().put(ReadDNNTLabels.PROVIDED_BY_DNNT_LABEL, getLicense().getName());
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
    public EvaluatingResultState mockEvaluate(DataMockExpectation dataMockExpectation) throws RightCriteriumException {
        switch (dataMockExpectation) {
            case EXPECT_DATA_VAUE_EXISTS: return EvaluatingResultState.TRUE;
            case EXPECT_DATA_VALUE_DOESNTEXIST: return EvaluatingResultState.NOT_APPLICABLE;
        }
        return EvaluatingResultState.NOT_APPLICABLE;
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
        return true;
    }

    @Override
    public void checkPrecodition(RightsManager manager) throws CriteriaPrecoditionException {
        //checkContainsCriteriumPDFDNNT(this.evalContext, manager);
    }


    @Override
    public boolean isLabelAssignable() {
        return true;
    }

    @Override
    public void setLicense(License license) {
        this.license = license;
    }

    @Override
    public License getLicense() {
        return this.license;
    }
}
