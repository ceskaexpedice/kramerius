/*
 * Copyright (C) 2016 Pavel Stastny
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
import cz.incad.kramerius.security.*;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.utils.RelsExtUtils;

/**
 * @author pavels
 *
 */
public class BenevolentModelFilter  extends AbstractCriterium implements RightCriterium {

    public static final Logger LOGGER = Logger.getLogger(BenevolentModelFilter.class.getName());

    
    /* (non-Javadoc)
     * @see cz.incad.kramerius.security.RightCriterium#evaluate()
     */
    @Override
    public EvaluatingResultState evalute(Right right) throws RightCriteriumException {
        return evaluateInternal(getObjects(), getEvaluateContext());
    }

    @Override
    public EvaluatingResultState mockEvaluate(Right right, DataMockExpectation dataMockExpectation) throws RightCriteriumException {
        switch (dataMockExpectation) {
            case EXPECT_DATA_VAUE_EXISTS: return EvaluatingResultState.TRUE;
            case EXPECT_DATA_VALUE_DOESNTEXIST: return EvaluatingResultState.NOT_APPLICABLE;
        }
        return EvaluatingResultState.NOT_APPLICABLE;
    }

    /**
     * @return
     */
    static EvaluatingResultState evaluateInternal(Object[] params, RightCriteriumContext ctx) {
        try {
            AkubraRepository akubraRepository = ctx.getAkubraRepository();
            ObjectPidsPath[] pathsToRoot = ctx
                    .getPathsToRoot();
            for (ObjectPidsPath pth : pathsToRoot) {
                String[] pids = pth.getPathFromLeafToRoot();
                for (String pid : pids) {
                    if (pid.equals(SpecialObjects.REPOSITORY.getPid())) continue;
                    String modelName = RelsExtUtils.getModel(akubraRepository.re().get(pid).asDom(false));
                    if (containsModelName(params,modelName)) return EvaluatingResultState.TRUE;
                }
            }
            return EvaluatingResultState.NOT_APPLICABLE;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return EvaluatingResultState.NOT_APPLICABLE;
        }
    }

    static boolean containsModelName(Object[] objects, String modelName) {
        for (Object object : objects) {
            if (object.toString().equals(modelName)) return true;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see cz.incad.kramerius.security.RightCriterium#getApplicableActions()
     */
    @Override
    public SecuredActions[] getApplicableActions() {
        return new SecuredActions[] { 
                SecuredActions.A_READ
        };
    }

    /* (non-Javadoc)
     * @see cz.incad.kramerius.security.RightCriterium#getPriorityHint()
     */
    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.NORMAL;
    }

    /* (non-Javadoc)
     * @see cz.incad.kramerius.security.RightCriterium#isParamsNecessary()
     */
    @Override
    public boolean isParamsNecessary() {
        return true;
    }

}
