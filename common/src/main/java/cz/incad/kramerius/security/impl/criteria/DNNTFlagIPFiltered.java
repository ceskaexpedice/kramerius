package cz.incad.kramerius.security.impl.criteria;

import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SecuredActions;

import java.util.logging.Logger;

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaIPAddrUtils.matchIPAddresses;
import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaRELSEXTUtils.evaluateState;

public class DNNTFlagIPFiltered extends AbstractCriterium {

    public static final Logger LOGGER = Logger.getLogger(DNNTFlagIPFiltered.class.getName());

    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.MIN;
    }

    @Override
    public boolean isParamsNecessary() {
        return true;
    }

    @Override
    public EvaluatingResultState evalute() throws RightCriteriumException {
        String path = "//kramerius:dnnt/text()";
        String expectedValue = "true";
        EvaluatingResultState evaluatingResultState = evaluateState(getEvaluateContext(), path, expectedValue);
        if (evaluatingResultState == EvaluatingResultState.TRUE) {
            EvaluatingResultState result = matchIPAddresses(super.getEvaluateContext(), getObjects()) ?  EvaluatingResultState.TRUE : EvaluatingResultState.NOT_APPLICABLE;
            LOGGER.fine("\t benevolent filter - "+result);
            return result ;
        } else {
            return EvaluatingResultState.FALSE;
        }
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
