package cz.incad.kramerius.security.impl.criteria;

import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaRELSEXTUtils;

import java.util.logging.Logger;

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaRELSEXTUtils.*;

public class DNNTFlag extends AbstractCriterium {

    public static final Logger LOGGER = Logger.getLogger(DNNTFlag.class.getName());

    @Override
    public EvaluatingResultState evalute() throws RightCriteriumException {
        String path = "//kramerius:dnnt/text()";
        String expectedValue = "true";
        return evaluateState(getEvaluateContext(),  path, expectedValue);
    }

    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.MIN;
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
}
