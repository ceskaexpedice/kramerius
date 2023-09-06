/*
 * Copyright (C) Jul 18, 2023 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.security.impl.criteria;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.DataMockExpectation;
import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumLabelAware;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.RightsReturnObject;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaDNNTUtils;
import cz.incad.kramerius.security.licenses.License;

public class PDFProtectedByLicense extends AbstractCriterium implements RightCriteriumLabelAware{

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
                    if (CriteriaDNNTUtils.allowedByReadLicenseRight(obj, getLicense())) return EvaluatingResultState.FALSE;
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
