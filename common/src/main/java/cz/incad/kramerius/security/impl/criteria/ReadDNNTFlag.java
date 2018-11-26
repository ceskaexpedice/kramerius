package cz.incad.kramerius.security.impl.criteria;

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaDNNTUtils.*;


import cz.incad.kramerius.security.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

// dnnt pro zobrazeni
public class ReadDNNTFlag extends AbstractCriterium {

    public static final Logger LOGGER = Logger.getLogger(ReadDNNTFlag.class.getName());

    @Override
    public EvaluatingResultState evalute() throws RightCriteriumException {
        RightCriteriumContext ctx =  getEvaluateContext();
        // only for READ action
        if (ctx.getAction().equals(SecuredActions.READ)) {
            String pid = ctx.getRequestedPid();
            if (!pid.equals(SpecialObjects.REPOSITORY.getPid())) {
                // log event.
                EvaluatingResultState dnnt = checkDnnt(ctx);
                if (dnnt.equals(EvaluatingResultState.TRUE)) {
                    try {
                        logDnntAccess(ctx);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        return EvaluatingResultState.FALSE;
                    }
                }
                return dnnt;
            } else return EvaluatingResultState.FALSE;
        } else return EvaluatingResultState.FALSE;
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

        super.checkPrecodition(manager);
    }
}
