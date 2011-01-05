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

import java.util.Calendar;

import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriterium;

public abstract class AbstractIPAddressFilter extends AbstractCriterium implements RightCriterium {

    protected boolean matchIPAddresses(Object[] objs) {
        for (Object pattern : objs) {
            if (pattern.toString().matches(this.getEvaluateContext().getRemoteAddr())) return true;
            if (pattern.toString().matches(this.getEvaluateContext().getRemoteHost())) return true;
        }
        return false;
    }


}
