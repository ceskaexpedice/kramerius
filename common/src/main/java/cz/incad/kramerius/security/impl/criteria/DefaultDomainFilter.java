/*
 * Copyright (C) 2012 Pavel Stastny
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

import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.Level;

import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumException;

public class DefaultDomainFilter extends AbstractDomainFilter  {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DefaultIPAddressFilter.class.getName());

    @Override
    public EvaluatingResult evalute() throws RightCriteriumException {
        try {
            EvaluatingResult result = matchDomain(getObjects()) ? EvaluatingResult.TRUE : EvaluatingResult.NOT_APPLICABLE;
            LOGGER.fine("\t benevolent domain filter - "+result);
            return result ;
        } catch (UnknownHostException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return EvaluatingResult.NOT_APPLICABLE;
        }
    }


}
