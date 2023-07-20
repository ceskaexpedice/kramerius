package cz.incad.kramerius.security.impl.criteria;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaDNNTUtils;
import cz.incad.kramerius.security.licenses.License;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


//TODO: Rename
public class PDFDNNTLabels extends AbstractCriterium implements RightCriteriumLabelAware{

    public transient  static final Logger LOGGER = Logger.getLogger(PDFDNNTLabels.class.getName());

    private License license;

    @Override
    public EvaluatingResultState evalute() throws RightCriteriumException {
        String requestedPid = null;
        try {
            requestedPid = this.getEvaluateContext().getRequestedPid();
            if (requestedPid != null && !SpecialObjects.isSpecialObject(requestedPid)) {
                RightsResolver rightsResolver = this.getEvaluateContext().getRightsResolver();
                ObjectPidsPath[] paths = this.getEvaluateContext().getSolrAccessNewIndex().getPidPaths(requestedPid);
                for (ObjectPidsPath path : paths) {
                    RightsReturnObject obj = rightsResolver.isActionAllowed(SecuredActions.A_READ.getFormalName(), requestedPid, null, path);
                    if (CriteriaDNNTUtils.allowedByReadDNNTLabelsRight(obj, getLicense())) return EvaluatingResultState.FALSE;
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }

        // not applicable
        return EvaluatingResultState.NOT_APPLICABLE;
    }

    @Override
    public License getLicense() {
        return this.license;
    }

    @Override
    public void setLicense(License license) {
        this.license = license;
    }

    @Override
    public EvaluatingResultState mockEvaluate(DataMockExpectation dataMockExpectation) throws RightCriteriumException {
        return  EvaluatingResultState.NOT_APPLICABLE;
    }

    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.DNNT_EXCLUSIVE_MAX;
    }

    @Override
    public boolean isParamsNecessary() {
        return false;
    }

    @Override
    public SecuredActions[] getApplicableActions() {
        return  new SecuredActions[] {SecuredActions.A_PDF_READ};
    }

    @Override
    public boolean isRootLevelCriterum() {
        return true;
    }

    @Override
    public void checkPrecodition(RightsManager manager) throws CriteriaPrecoditionException {
        //allowedByReadDNNTFlagRight(this.evalContext, manager);
    }


    @Override
    public boolean isLicenseAssignable() {
        return true;
    }

}
