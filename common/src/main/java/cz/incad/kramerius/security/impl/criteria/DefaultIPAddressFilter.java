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

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaIPAddrUtils.*;

import java.util.Calendar;

import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaIPAddrUtils;

/**
 * Default IP Filter... pokud je z daneho rozsahu, pusti dal, pokud ne.. nevi
 * @author pavels
 *
 */
public class DefaultIPAddressFilter extends AbstractIPAddressFilter implements RightCriterium {

    static transient java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DefaultIPAddressFilter.class.getName());

    @Override
    public EvaluatingResultState evalute() throws RightCriteriumException {
        EvaluatingResultState result = matchIPAddresses(super.getEvaluateContext(), getObjects()) ?  EvaluatingResultState.TRUE : EvaluatingResultState.NOT_APPLICABLE;
        LOGGER.fine("\t benevolent filter - "+result);
        return result ;
    }

    public EvaluatingResultState createResult(Calendar calFromMetadata, Calendar calFromConf) {
        return calFromMetadata.before(calFromConf) ?  EvaluatingResultState.TRUE:EvaluatingResultState.FALSE;
    }


    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.MAX;
    }
    
    
    @Override
    public SecuredActions[] getApplicableActions() {
        return SecuredActions.values();
    }

}
