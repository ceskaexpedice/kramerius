package cz.incad.kramerius.security.impl.criteria;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.solr.SolrUtils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PDFDNNTFlag extends AbstractCriterium {

    public static final Logger LOGGER = Logger.getLogger(ReadDNNTFlag.class.getName());


    @Override
    public EvaluatingResultState evalute() throws RightCriteriumException {
        String requestedPid = null;
        try {
            requestedPid = this.getEvaluateContext().getRequestedPid();
            if (requestedPid != null && !SpecialObjects.isSpecialObject(requestedPid)) {
                // only if
                String s = SolrUtils.disectDNNT(this.evalContext.getSolrAccess().getSolrDataDocument(requestedPid).getDocumentElement());
                if (s != null && s.equals("true")) {
                    IsActionAllowed rightsResolver = this.getEvaluateContext().getRightsResolver();
                    ObjectPidsPath[] paths = this.getEvaluateContext().getSolrAccess().getPath(requestedPid);
                    for (ObjectPidsPath path : paths) {
                        RightsReturnObject obj = rightsResolver.isActionAllowed(SecuredActions.READ.getFormalName(), requestedPid, FedoraUtils.IMG_FULL_STREAM, path);
                        if (obj.getRight().getCriteriumWrapper() != null) {
                            if (obj.getRight().getCriteriumWrapper().getRightCriterium().getQName().equals(ReadDNNTFlag.class.getName()) ||
                                    obj.getRight().getCriteriumWrapper().getRightCriterium().getQName().equals(ReadDNNTFlagIPFiltered.class.getName())) {
                                return EvaluatingResultState.FALSE;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }

        // not applicable
        return EvaluatingResultState.NOT_APPLICABLE;
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
        return  new SecuredActions[] {SecuredActions.PDF_RESOURCE};
    }

    @Override
    public boolean isRootLevelCriterum() {
        return true;
    }

}
