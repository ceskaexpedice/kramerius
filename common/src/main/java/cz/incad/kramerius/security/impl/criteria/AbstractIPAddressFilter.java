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
import java.util.regex.Pattern;

import cz.incad.kramerius.security.RightCriterium;

public abstract class AbstractIPAddressFilter extends AbstractCriterium implements RightCriterium {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AbstractIPAddressFilter.class.getName());
    
    protected boolean matchIPAddresses(Object[] objs) {
        String remoteAddr = this.getEvaluateContext().getRemoteAddr();
        return matchIPAddresses(objs, remoteAddr);
    }


    protected boolean matchIPAddresses(Object[] objs, String remoteAddr) {
        for (Object pattern : objs) {
            boolean negativePattern = false;
            String patternStr = pattern.toString();
            if (patternStr.startsWith("!")) {
                patternStr = patternStr.substring(1);
                negativePattern = true;
            }
            
            boolean matched = remoteAddr.matches(patternStr);
            if ((matched) && (!negativePattern)) { 
                LOGGER.fine("\t regexpattern '"+patternStr+"' trying to match with address  '"+remoteAddr+"' - ACCEPTING");
                return true;
            } else if ((!matched) && (negativePattern)) {
                LOGGER.fine("\t regexpattern '"+patternStr+"' trying to match with address  '"+remoteAddr+"' - (negative pattern) ACCEPTING");
                return true;
            }

            // only debug
            if ((!matched) && (!negativePattern)) {
                LOGGER.fine("\t regexpattern '"+patternStr+"' trying to match with address  '"+remoteAddr+"' - NOT ACCEPTING");
            } else if ((matched) && (negativePattern)) {
                LOGGER.fine("\t regexpattern '"+patternStr+"' trying to match with address  '"+remoteAddr+"' -(negative pattern) NOT ACCEPTING");
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
