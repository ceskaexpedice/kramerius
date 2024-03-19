/*
 * Copyright (C) Mar 18, 2024 Pavel Stastny
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
package cz.incad.kramerius.auth.thirdparty.keycloack.dnnt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import cz.incad.kramerius.auth.thirdparty.impl.AbstractThirdPartyUser;

public class StandardDNNTUsersSupport {
    
    public static final String ENTITLEMENT_VALUE = "urn:mace:dir:entitlement:common-lib-terms";
    public static final List<String> EDUSCOPE_AFFILIATION = Arrays.asList("member@", "library-walk-in@");
    
    
    public static final String DNNT_USERS_ROLE = "dnnt_users";
    
    //eduPersonScopedAffiliation
    //eduPersonEntitlement

    //ii.eduPersonScopedAffiliation,
    // member@ nebo library-walk-in@
    
    //eduPersonEntitlement
    //urn:mace:dir:entitlement:common-lib-terms
    private StandardDNNTUsersSupport() {}
    
 
    public static void makeSureDNNTUsersRole(AbstractThirdPartyUser user) {
        String eduPersonScopeAffilitaion = null;
        String eduPersonEntitlement = null;
        
        Set<String> properties = user.getPropertyKeys();
        for (String key : properties) {
            if (key.toLowerCase().equals("eduPersonScopedAffiliation".toLowerCase())) {
                eduPersonScopeAffilitaion = user.getProperty( key);
            }
            
            if (key.toLowerCase().equals("eduPersonEntitlement".toLowerCase())) {
                eduPersonEntitlement = user.getProperty( key);
            }
        }
        
        if (eduPersonEntitlement != null && eduPersonScopeAffilitaion != null) {
            if (eduPersonEntitlement.contains(ENTITLEMENT_VALUE)) {
                for (String eduAff : EDUSCOPE_AFFILIATION) {
                    if (eduPersonScopeAffilitaion.contains(eduAff)) {
                        List<String> roles = user.getRoles();
                        if (!roles.contains(DNNT_USERS_ROLE)) {
                            ArrayList<String> nroles = new ArrayList<>(roles);
                            nroles.add(DNNT_USERS_ROLE);
                            user.setRoles(nroles);
                        }
                    }
                }
            }
        }
    }
}
