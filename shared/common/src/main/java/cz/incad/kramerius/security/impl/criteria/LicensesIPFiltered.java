package cz.incad.kramerius.security.impl.criteria;

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaIPAddrUtils.matchIPAddresses;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.DataMockExpectation;
import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumLabelAware;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaLicenseUtils;
import cz.incad.kramerius.security.licenses.License;

public class LicensesIPFiltered extends AbstractCriterium implements RightCriteriumLabelAware{

    public transient static final Logger LOGGER = Logger.getLogger(LicensesIPFiltered.class.getName());

    private License license;


    @Override
    public EvaluatingResultState evalute(Right right) throws RightCriteriumException {
        try {
            RightCriteriumContext ctx =  getEvaluateContext();
            String pid = ctx.getRequestedPid();
            if (!SpecialObjects.isSpecialObject(pid)) {
                if (!pid.equals(SpecialObjects.REPOSITORY.getPid())) {
                    SolrAccess solrAccess = ctx.getSolrAccessNewIndex();
                    Document doc = solrAccess.getSolrDataByPid(pid);
                    License lic = getLicense(); 
                    boolean applied = CriteriaLicenseUtils.matchLicense(doc,  lic);
                    if (applied)  {
                        EvaluatingResultState result = matchIPAddresses(super.getEvaluateContext(), getObjects()) ?  EvaluatingResultState.TRUE : EvaluatingResultState.NOT_APPLICABLE;
                        if (result.equals(EvaluatingResultState.TRUE)) {
                            if (lic.exclusiveLockPresent()) {
                                return CriteriaLicenseUtils.licenseLock(right, ctx, pid, lic);
                            } else {
                                getEvaluateContext().getEvaluateInfoMap().put(Licenses.PROVIDED_BY_LABEL, getLicense().getName());
                                getEvaluateContext().getEvaluateInfoMap().put(Licenses.PROVIDED_BY_LICENSE, getLicense().getName());
                                return EvaluatingResultState.TRUE;
                            }

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
    public EvaluatingResultState mockEvaluate(Right right, DataMockExpectation dataMockExpectation) throws RightCriteriumException {
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

