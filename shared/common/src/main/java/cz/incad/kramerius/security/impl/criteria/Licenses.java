package cz.incad.kramerius.security.impl.criteria;

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

/**
 * The {@code Licenses} criterium evaluates whether a user has access rights based on an assigned license.
 * This criterium links user roles with specific licenses.
 *
 * It checks if the requested document has a license, either as a runtime license or one indexed in Solr.
 * If the document has a matching license and the license allows access, the access is granted.
 *
 * Access is evaluated only for the READ action and is not applied to special or repository objects.
 * If the license includes an exclusive lock, additional validation is performed to enforce it.
 */
public class Licenses extends AbstractCriterium implements RightCriteriumLabelAware{
    
    // backward compatibility
    public static final String PROVIDED_BY_LABEL = "providedByLabel";
    public static final String PROVIDED_BY_LICENSE = "providedByLicense";

    public transient static final Logger LOGGER = Logger.getLogger(Licenses.class.getName());

    private License license;

    @Override
    public EvaluatingResultState evalute(Right right) throws RightCriteriumException {
        try {
            RightCriteriumContext ctx =  getEvaluateContext();
            String pid = ctx.getRequestedPid();
            // only for READ action
            if (!SpecialObjects.isSpecialObject(pid)) {

                if (!pid.equals(SpecialObjects.REPOSITORY.getPid())) {
                    // runtime licence
                    SolrAccess solrAccess = ctx.getSolrAccessNewIndex();
                    // Licence a
                    Document doc = solrAccess.getSolrDataByPid(pid);

                    License lic = getLicense();
                    if (lic.isRuntimeLicense() && lic.acceptByLicense(doc)) {
                        // akceptuji runtime licenci
                        getEvaluateContext().getEvaluateInfoMap().put(ReadDNNTLabels.PROVIDED_BY_LABEL, getLicense().getName());
                        getEvaluateContext().getEvaluateInfoMap().put(ReadDNNTLabels.PROVIDED_BY_LICENSE, getLicense().getName());
                        return EvaluatingResultState.TRUE;
                    } else {
                        boolean applied =  CriteriaLicenseUtils.matchLicense(doc, lic);
                        if (applied) {
                            if (lic.exclusiveLockPresent()) {
                                return CriteriaLicenseUtils.licenseLock(right, ctx, pid, lic);
                            } else {
                                getEvaluateContext().getEvaluateInfoMap().put(ReadDNNTLabels.PROVIDED_BY_LABEL, getLicense().getName());
                                getEvaluateContext().getEvaluateInfoMap().put(ReadDNNTLabels.PROVIDED_BY_LICENSE, getLicense().getName());
                                return EvaluatingResultState.TRUE;
                            }
                        }
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
    public void setLicense(License license) {
        this.license = license;
    }

    @Override
    public License getLicense() {
        return this.license;
    }
}
