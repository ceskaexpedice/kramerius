/*
 * Copyright (C) 2010 Pavel Stastny
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

import cz.incad.kramerius.security.impl.criteria.utils.CriteriaRELSEXTUtils;

import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SecuredActions;

/**
 * Kontroluje priznak v metadatech RELS-EXT. 
 * Pokud 'kramerius:policy' ma hodnotu private, je dokument privatni a pristup je odpepren. 
 * V opacnem pripade je dokument verejny 
 * @author pavels
 *
 */
public class PolicyFlag extends AbstractRELSExtCriterium {

    java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PolicyFlag.class.getName());
    
    @Override
    public EvaluatingResultState evalute() throws RightCriteriumException {
        String path = "//kramerius:policy/text()";
        String expectedValue = "policy:private";
        return CriteriaRELSEXTUtils.evaluateState(getEvaluateContext(), path, expectedValue);

    }

    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.NORMAL;
    }

    @Override
    public boolean isParamsNecessary() {
        return false;
    }

    @Override
    public SecuredActions[] getApplicableActions() {
        return  new SecuredActions[] {SecuredActions.READ};
    }
    
    
}
