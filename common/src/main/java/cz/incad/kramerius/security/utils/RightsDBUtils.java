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
package cz.incad.kramerius.security.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.CriteriumType;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.impl.RightCriteriumParamsImpl;
import cz.incad.kramerius.security.impl.RightImpl;

public class RightsDBUtils {

    public static Right createRight(ResultSet rs, AbstractUser auser) throws SQLException {
        int rightId = rs.getInt("right_id");
        String uuidVal = rs.getString("uuid");
        String actionVal = rs.getString("action");
        RightCriterium crit = RightsDBUtils.createCriterium(rs);
        if (crit != null) {
            return new RightImpl(rightId, crit, uuidVal, actionVal, auser);
        } else {
            return new RightImpl(rightId, null, uuidVal, actionVal, auser);
        }
        
    }
    
    public static RightCriterium createCriterium(ResultSet rs) throws SQLException {
        String qname = rs.getString("qname");
        if ((qname != null) && (!qname.equals(""))) {
            int criteriumId = rs.getInt("crit_id");
            String shortDesc = rs.getString("short_desc");
            String longDesc = rs.getString("long_desc");
            int critParamId = rs.getInt("crit_param_id");
            int fixedPriority = rs.getInt("fixed_priority");
            String vals = rs.getString("vals");
            int type = rs.getInt("type");
            RightCriterium crit = null;
            if (critParamId > 0) {
                Object[] objs = valsFromString(vals);
                crit = CriteriumType.findByValue(type).createCriterium(criteriumId, critParamId, qname, shortDesc, longDesc, objs);
                if (fixedPriority != 0) {
                    crit.setFixedPriority(fixedPriority);
                }
            } else {
                crit = CriteriumType.findByValue(type).createCriteriumWithoutParams(criteriumId,  qname);
            }
            return crit;
        } else return null;
    }

    public static Object[] valsFromString(String vals) {
        Object[] objs = vals != null ? vals.split(";") : new Object[0];
        return objs;
    }

    public static RightCriteriumParams createCriteriumParams(ResultSet rs) throws SQLException {
        String shortDesc = rs.getString("short_desc");
        String longDesc = rs.getString("long_desc");
        int critParamId = rs.getInt("crit_param_id");
        String vals = rs.getString("vals");
        
        RightCriteriumParamsImpl params = new RightCriteriumParamsImpl(critParamId);
        params.setLongDescription(longDesc);
        params.setShortDescription(shortDesc);
        params.setObjects(valsFromString(vals));
        return params;
    }

}
