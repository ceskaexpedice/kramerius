package cz.incad.kramerius.security.impl.criteria;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaDNNTUtils;
import cz.incad.kramerius.security.licenses.License;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaIPAddrUtils.matchIPAddresses;

// TODO: Rename to ReadLicenseIPFilter
public class ReadDNNTLabelsIPFiltered extends AbstractCriterium implements RightCriteriumLabelAware{

    public transient static final Logger LOGGER = Logger.getLogger(ReadDNNTLabelsIPFiltered.class.getName());

    private License license;


    @Override
    public EvaluatingResultState evalute() throws RightCriteriumException {
        try {
            RightCriteriumContext ctx =  getEvaluateContext();
            String pid = ctx.getRequestedPid();
            if (!SpecialObjects.isSpecialObject(pid)) {
                if (!pid.equals(SpecialObjects.REPOSITORY.getPid())) {
                    SolrAccess solrAccess = ctx.getSolrAccessNewIndex();
                    Document doc = solrAccess.getSolrDataByPid(pid);
                    boolean applied = CriteriaDNNTUtils.matchLicense(doc,  getLicense());
                    if (applied)  {
                        EvaluatingResultState result = matchIPAddresses(super.getEvaluateContext(), getObjects()) ?  EvaluatingResultState.TRUE : EvaluatingResultState.NOT_APPLICABLE;
                        if (result.equals(EvaluatingResultState.TRUE)) {
                            getEvaluateContext().getEvaluateInfoMap().put(ReadDNNTLabels.PROVIDED_BY_DNNT_LABEL, getLicense().getName());
                            getEvaluateContext().getEvaluateInfoMap().put(ReadDNNTLabels.PROVIDED_BY_DNNT_LICENSE, getLicense().getName());
                        }
                        return result;

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
        return matchIPAddresses(super.getEvaluateContext(), getObjects()) ?  EvaluatingResultState.TRUE : EvaluatingResultState.NOT_APPLICABLE;
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
        return  new SecuredActions[] {SecuredActions.A_READ};
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
    public boolean isLicenseAssignable() {
        return true;
    }

    @Override
    public License getLicense() {
        return this.license;
    }

    @Override
    public void setLicense(License license) {
        this.license = license;
    }
}

