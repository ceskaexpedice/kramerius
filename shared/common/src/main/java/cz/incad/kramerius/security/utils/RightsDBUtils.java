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
import java.util.Optional;

import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.impl.RightCriteriumParamsImpl;
import cz.incad.kramerius.security.impl.RightImpl;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.RuntimeLicenseType;
import cz.incad.kramerius.security.licenses.impl.LicenseImpl;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLock.ExclusiveLockType;


public class RightsDBUtils {

    private RightsDBUtils() {}

    // vytvori pravo z resultsetu s
    public static Right createRight(ResultSet rs, Role auser, RightCriteriumWrapperFactory factory) throws SQLException {
        int rightId = rs.getInt("right_id");
        String uuidVal = rs.getString("uuid");
        String actionVal = rs.getString("action");
        int fixedPriority = rs.getInt("fixed_priority");

        RightCriteriumWrapper crit = RightsDBUtils.createCriteriumWrapper(factory, rs);
        Right right = null;
        if (crit != null) {
            right = new RightImpl(rightId, crit, uuidVal, actionVal, auser);
            right.setFixedPriority(fixedPriority);
        } else {
            right = new RightImpl(rightId, null, uuidVal, actionVal, auser);
            right.setFixedPriority(fixedPriority);
        }
        return right;
    }
    
    public static RightCriteriumWrapper createCriteriumWrapper(RightCriteriumWrapperFactory factory, ResultSet rs) throws SQLException {
        String qname = rs.getString("qname");
        int rstype = rs.getInt("type");
        int criteriumId = rs.getInt("crit_id");
        CriteriumType type = CriteriumType.findByValue(rstype);
        if (qname != null) {
            RightCriteriumWrapper rightCriteriumWrapper = factory.loadExistingWrapper(type, qname, criteriumId, createCriteriumParams(rs));
            if (rightCriteriumWrapper.isLicenseAwareCriterium()) {
                License licenseImpl = new LicenseImpl(rs.getInt("label_id"), rs.getString("label_name"),  rs.getString("label_description"),rs.getString("label_group"),rs.getInt("label_priority"));

                boolean lock = rs.getBoolean("LOCK");
                int maxreaders = rs.getInt("LOCK_MAXREADERS");
                int refreshinterval = rs.getInt("LOCK_REFRESHINTERVAL");
                int maxinterval = rs.getInt("LOCK_MAXINTERVAL");
                String lockTypeStr = rs.getString("LOCK_TYPE");

                boolean runtime = rs.getBoolean("RUNTIME");
                String runtimeType = rs.getString("RUNTIME_TYPE");

                if (lock) {
                    licenseImpl.initExclusiveLock(refreshinterval, maxinterval, maxreaders,ExclusiveLockType.findByType(lockTypeStr));
                }

                if (runtime) {
                    Optional<RuntimeLicenseType> opt = RuntimeLicenseType.fromString(runtimeType);
                    opt.ifPresent(rtype -> {
                        licenseImpl.initRuntime(rtype);
                    });
                }
                
                rightCriteriumWrapper.setLicense(licenseImpl);
            }
            return rightCriteriumWrapper;
        } else return null;
    }
    
    /*
    public static RightCriterium createCriterium(ResultSet rs) throws SQLException {
        String qname = rs.getString("qname");
        if ((qname != null) && (!qname.equals(""))) {
            
            int criteriumId = rs.getInt("crit_id");
            String shortDesc = rs.getString("short_desc");
            String longDesc = rs.getString("long_desc");
            int critParamId = rs.getInt("crit_param_id");
            String vals = rs.getString("vals");
            int type = rs.getInt("type");
            RightCriterium crit = null;
            
            if (critParamId > 0) {
                Object[] objs = valsFromString(vals);
                crit = CriteriumType.findByValue(type).createCriterium(criteriumId, critParamId, qname, shortDesc, longDesc, objs);
            } else {
                crit = CriteriumType.findByValue(type).createCriteriumWithoutParams(criteriumId,  qname);
            }
            return crit;
        } else return null;
    }*/

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
