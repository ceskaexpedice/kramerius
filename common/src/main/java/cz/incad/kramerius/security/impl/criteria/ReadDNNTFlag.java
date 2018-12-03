package cz.incad.kramerius.security.impl.criteria;

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaDNNTUtils.*;


import cz.incad.kramerius.security.*;

import java.util.logging.Logger;

// dnnt pro zobrazeni
public class ReadDNNTFlag extends AbstractCriterium {

    public static final Logger LOGGER = Logger.getLogger(ReadDNNTFlag.class.getName());

    @Override
    public EvaluatingResultState evalute() throws RightCriteriumException {
        RightCriteriumContext ctx =  getEvaluateContext();
        String pid = ctx.getRequestedPid();
        // only for READ action
        if (!SpecialObjects.isSpecialObject(pid)) {

                if (!pid.equals(SpecialObjects.REPOSITORY.getPid())) {
                    // log event.
                    EvaluatingResultState dnnt = checkDnnt(ctx);
                    return dnnt;
                } else return EvaluatingResultState.NOT_APPLICABLE;

        } else return EvaluatingResultState.NOT_APPLICABLE;
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
        return  new SecuredActions[] {SecuredActions.READ};
    }

    @Override
    public boolean isRootLevelCriterum() {
        return true;
    }

    @Override
    public void checkPrecodition(RightsManager manager) throws CriteriaPrecoditionException {
        checkContainsCriteriumPDFDNNT(this.evalContext, manager);
    }

}
