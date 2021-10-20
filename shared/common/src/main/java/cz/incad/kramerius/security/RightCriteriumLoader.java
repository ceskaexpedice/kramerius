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
package cz.incad.kramerius.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.incad.kramerius.security.impl.criteria.Abonents;
import cz.incad.kramerius.security.impl.criteria.DefaultIPAddressFilter;
import cz.incad.kramerius.security.impl.criteria.MovingWall;
import cz.incad.kramerius.security.impl.criteria.PolicyFlag;
import cz.incad.kramerius.security.impl.criteria.StrictIPAddresFilter;

// ma za ukol nahrat skutecna kriteria... jsou ruzne implementace
public interface RightCriteriumLoader {
    
    public CriteriumType getCriteriumType();
    
    public boolean isDefined(String qname);
    
    public List<RightCriterium> getCriteriums();
    
    public List<RightCriterium> getCriteriums(SecuredActions ...applActions);

    public RightCriterium createCriterium(String criteriumQName);

    

}
