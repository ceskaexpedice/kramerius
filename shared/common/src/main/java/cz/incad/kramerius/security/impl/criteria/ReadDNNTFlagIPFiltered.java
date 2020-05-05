package cz.incad.kramerius.security.impl.criteria;

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaDNNTUtils.*;

import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaDNNTUtils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaIPAddrUtils.matchIPAddresses;

public class ReadDNNTFlagIPFiltered extends AbstractCriterium {

    public transient  static final Logger LOGGER = Logger.getLogger(ReadDNNTFlagIPFiltered.class.getName());

    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.DNNT_EXCLUSIVE_MIN;
    }

    @Override
    public boolean isParamsNecessary() {
        return true;
    }

    @Override
    public EvaluatingResultState evalute() throws RightCriteriumException {
        String pid = getEvaluateContext().getRequestedPid();
        if (!SpecialObjects.isSpecialObject(pid)) {

                EvaluatingResultState state = checkDnnt(getEvaluateContext());
                if (!pid.equals(SpecialObjects.REPOSITORY.getPid())) {
                    if (state == EvaluatingResultState.TRUE) {
                        EvaluatingResultState result = matchIPAddresses(super.getEvaluateContext(), getObjects()) ?  EvaluatingResultState.TRUE : EvaluatingResultState.NOT_APPLICABLE;
                        return result;
                    } else {
                        return state;
                    }
                } else return EvaluatingResultState.NOT_APPLICABLE;

        } else return EvaluatingResultState.NOT_APPLICABLE;
    }

    @Override
    public void checkPrecodition(RightsManager manager) throws CriteriaPrecoditionException {
        checkContainsCriteriumPDFDNNT(this.evalContext, manager);
    }


    @Override
    public SecuredActions[] getApplicableActions() {
        return new SecuredActions[]{SecuredActions.READ};
    }

    @Override
    public boolean isRootLevelCriterum() {
        return true;
    }
}
