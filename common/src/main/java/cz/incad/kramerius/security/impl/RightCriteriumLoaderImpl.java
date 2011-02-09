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
package cz.incad.kramerius.security.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumLoader;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.impl.criteria.Abonents;
import cz.incad.kramerius.security.impl.criteria.DefaultIPAddressFilter;
import cz.incad.kramerius.security.impl.criteria.MovingWall;
import cz.incad.kramerius.security.impl.criteria.PolicyFlag;
import cz.incad.kramerius.security.impl.criteria.StrictIPAddresFilter;

public class RightCriteriumLoaderImpl implements RightCriteriumLoader {
 
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(RightCriteriumLoaderImpl.class.getName());
    
    // nahravani z manifestu nebo z db
    private static String[] CLASSES = {
        MovingWall.class.getName(), 
        StrictIPAddresFilter.class.getName(), 
        DefaultIPAddressFilter.class.getName(), 
        PolicyFlag.class.getName()  
    };
    
    @Override
    public List<RightCriterium> getCriteriums() {
        try {
            List<String> clzz = Arrays.asList(CLASSES);
            List<RightCriterium> crits = new ArrayList<RightCriterium>();
            for (int i = 0; i < clzz.size(); i++) {
                crits.add((RightCriterium) Class.forName(clzz.get(i)).newInstance());
            }
            return crits;
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return new ArrayList<RightCriterium>();
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return new ArrayList<RightCriterium>();
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return new ArrayList<RightCriterium>();
        }
    }

    @Override
    public List<RightCriterium> getCriteriums(SecuredActions... applActions) {
        try {
            List<String> clzz = Arrays.asList(CLASSES);
            List<RightCriterium> crits = new ArrayList<RightCriterium>();
            for (int i = 0; i < clzz.size(); i++) {
                RightCriterium crit = (RightCriterium) Class.forName(clzz.get(i)).newInstance();
                List<SecuredActions> actList = Arrays.asList(crit.getApplicableActions());
                for (SecuredActions act : applActions) {
                    if (actList.contains(act)) {
                        crits.add(crit);
                    }
                }
            }
            return crits;
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return new ArrayList<RightCriterium>();
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return new ArrayList<RightCriterium>();
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return new ArrayList<RightCriterium>();
        }
    }

    @Override
    public RightCriterium getCriterium(String criteriumQName) {
        try {
            RightCriterium crit = (RightCriterium) Class.forName(criteriumQName).newInstance();
            return crit;
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return null;
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return null;
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return null;
        }
    }
}
