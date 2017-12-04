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

import java.util.logging.Level;

import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SecuredActions;

/**
 * Omezujici filter -> Pokud je z daneho rozsahu - pusti dal. Pokud neni - zakaze
 * @author pavels
 *
 */
public class StrictIPAddresFilter extends AbstractIPAddressFilter implements RightCriterium {
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(MovingWall.class.getName());
    

    @Override
    public EvaluatingResult evalute() throws RightCriteriumException {
        EvaluatingResult result = matchIPAddresses(getObjects()) ?  EvaluatingResult.TRUE : EvaluatingResult.FALSE;
        LOGGER.fine("\t strict filter - "+result);
        return result;
    }


//    @Override
//    public boolean validate(Object[] objs) {
//        if ((objs != null) && (objs.length == 1)) {
//            String val = (String) objs[0];
//            try {
//                Integer.parseInt(val);
//                return true;
//            } catch (NumberFormatException e) {
//                LOGGER.log(Level.SEVERE, e.getMessage(), e);
//                return false;
//            }
//        } else return false;
//    }


    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.MAX;
    }
    
    
    @Override
    public SecuredActions[] getApplicableActions() {
        return SecuredActions.values();
    }

}
