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

import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;

/**
 * Predplatitele
 * @author pavels
 */
public class Abonents extends AbstractCriterium implements RightCriterium {

    @Override
    public EvaluatingResult evalute() throws RightCriteriumException {
        Object[] groups = this.getCriteriumParams().getObjects();
        for (Object oneGroup : groups) {
            if (isUserInGroup(oneGroup.toString())) return EvaluatingResult.TRUE;
        }
        return EvaluatingResult.FALSE;
    }

    private boolean isUserInGroup(String expectedGroup) {
        Group[] groupAssociatedWithUser = this.getEvaluateContext().getUser().getGroups();
        for (Group group : groupAssociatedWithUser) {
            if(group.getName().equals(expectedGroup)) return true;
        }
        return false;
    }


    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.MAX;
    }

    @Override
    public boolean isParamsNecessary() {
        return true;
    }

    
    
}
