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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.SecuredActions;

//TODO: ZMENIT
public class CriteriumsLoader {
    
    public static List<String> criteriumClasses() {
        return Arrays.asList(MovingWall.class.getName(), 
                StrictIPAddresFilter.class.getName(), 
                DefaultIPAddressFilter.class.getName(), 
                PolicyFlag.class.getName(),
                CoverAndContentFilter.class.getName()
                );
    }
    
    public static List<RightCriterium> criteriums() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        List<String> clzz = criteriumClasses();
        List<RightCriterium> crits = new ArrayList<RightCriterium>();
        for (int i = 0; i < clzz.size(); i++) {
            crits.add((RightCriterium) Class.forName(clzz.get(i)).newInstance());
        }
        return crits;
    }

    public static List<RightCriterium> criteriums(SecuredActions ...actions ) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        List<String> clzz = criteriumClasses();
        List<RightCriterium> crits = new ArrayList<RightCriterium>();
        for (int i = 0; i < clzz.size(); i++) {
            RightCriterium crit = (RightCriterium) Class.forName(clzz.get(i)).newInstance();
            List<SecuredActions> actList = Arrays.asList(crit.getApplicableActions());
            for (SecuredActions act : actions) {
                if (actList.contains(act)) {
                    crits.add(crit);
                }
            }
        }
        return crits;
    }
    
    
//    public static RightCriterium criteriumsByName(String criteriumQName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
//        RightCriterium crit = (RightCriterium) Class.forName(criteriumQName).newInstance();
//    }
}
