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
import java.util.logging.Level;
import java.util.regex.Pattern;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression;

import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SecuredActions;

public abstract class AbstractIPAddressFilter extends AbstractCriterium implements RightCriterium {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AbstractIPAddressFilter.class.getName());
    
    protected boolean matchIPAddresses(Object[] objs) {
        for (Object pattern : objs) {
            boolean negativePattern = false;
            String remoteAddr = this.getEvaluateContext().getRemoteAddr();
            String patternStr = pattern.toString();
            if (patternStr.startsWith("!")) {
                patternStr = patternStr.substring(1);
                negativePattern = true;
            }
            boolean matched = remoteAddr.matches(patternStr);
            
            if ((matched) && (!negativePattern)) { 
                return true;
            } else if ((!matched) && (negativePattern)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean isParamsNecessary() {
        return true;
    }


    @Override
    public boolean validateParams(Object[] vals) {
        
        try {
            for (Object pattern : vals) {
                String patternStr = pattern.toString();
                Pattern compiled = Pattern.compile(patternStr);
                if (compiled == null) return false;
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
    }
    
}
