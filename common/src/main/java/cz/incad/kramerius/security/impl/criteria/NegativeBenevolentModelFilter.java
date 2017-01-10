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

import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SecuredActions;

/**
 * @author pavels
 *
 */
public class NegativeBenevolentModelFilter  extends AbstractCriterium implements RightCriterium {

    /* (non-Javadoc)
     * @see cz.incad.kramerius.security.RightCriterium#evalute()
     */
    @Override
    public EvaluatingResult evalute() throws RightCriteriumException {
        EvaluatingResult result = BenevolentModelFilter.evaluateInternal(getObjects(), getEvaluateContext());
        return result.equals(EvaluatingResult.TRUE) ? EvaluatingResult.NOT_APPLICABLE : EvaluatingResult.TRUE;
    }

    /* (non-Javadoc)
     * @see cz.incad.kramerius.security.RightCriterium#getApplicableActions()
     */
    @Override
    public SecuredActions[] getApplicableActions() {
        return new SecuredActions[] { 
                SecuredActions.READ, 
                SecuredActions.SHOW_CLIENT_PRINT_MENU,
                SecuredActions.SHOW_CLIENT_PDF_MENU
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
