package cz.incad.kramerius.security.impl.criteria;

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaDNNTUtils.*;

import cz.incad.kramerius.security.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaIPAddrUtils.matchIPAddresses;

public class ReadDNNTFlagIPFiltered extends AbstractCriterium {

    public static final Logger LOGGER = Logger.getLogger(ReadDNNTFlagIPFiltered.class.getName());

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
        Thread.dumpStack();

        EvaluatingResultState state = checkDnnt(getEvaluateContext());
        String pid = getEvaluateContext().getRequestedPid();
        if (!pid.equals(SpecialObjects.REPOSITORY.getPid())) {
            if (state == EvaluatingResultState.TRUE) {
                EvaluatingResultState result = matchIPAddresses(super.getEvaluateContext(), getObjects()) ?  EvaluatingResultState.TRUE : EvaluatingResultState.NOT_APPLICABLE;
                if(result.equals(EvaluatingResultState.TRUE)) {
                    try {
                        logDnntAccess(getEvaluateContext());
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        return EvaluatingResultState.FALSE;
                    }
                }
                return result;
            } else {
                return state;
            }
        } else return EvaluatingResultState.FALSE;
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
