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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.CriteriumType;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;

import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.database.SecurityDatabaseUtils;
import cz.incad.kramerius.security.utils.SortingRightsUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

public class DatabaseRightsManager implements RightsManager {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DatabaseRightsManager.class.getName());
    
    //TODO: Zmenit 
    public static final String REPOSITORY_UUID="1";
    
    @Inject
    @Named("kramerius4")
    Provider<Connection> provider;
    
    
    @Inject
    UserManager userManager;
    

    @Override
    public Right[] findRights(final String[] uuids, final String action, final User user) {
        Group[] grps = user.getGroups();
        int[] grpIds = new int[grps.length]; {
            for (int i = 0; i < grps.length; i++) {
                grpIds[i]=grps[i].getId();
            }
        }
        for (int i = 0; i < uuids.length; i++) {
            if (!uuids[i].startsWith("uuid:")) {
                uuids[i] = "uuid:"+uuids[i];
            }
        }
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("findRightFromWithGroups");
        template.setAttribute("uuids", uuids);
        template.setAttribute("groups", grpIds);
        template.setAttribute("user", user.getId());
        template.setAttribute("action", action);

        String command = template.toString();
        
        List<Right> rights = new JDBCQueryTemplate<Right>(this.provider.get()){
            @Override
            public boolean handleRow(ResultSet rs, List<Right> returnsList) throws SQLException {

                int rightId=rs.getInt("right_id");
    
                String uuidVal = rs.getString("uuid");
                String actionVal = rs.getString("action");
                
                int criteriumId = rs.getInt("crit_id");
                int userId = rs.getInt("user");
                int groupId = rs.getInt("group");
                
                int fixedPriority = rs.getInt("fixed_priority");
                
                AbstractUser dbUser = null;
                if (userId > 0) {
                    dbUser = userManager.findUser(userId);
                } else {
                    dbUser = userManager.findGroup(groupId);
                }
                
                String qname = rs.getString("qname");
                int typeId = rs.getInt("type");
                String vals = rs.getString("vals");
                
                if (qname!=null) {
                    // ma kriterium
                    int type = rs.getInt("type");
                    if (type >= 0) {
                        Object[] objs = vals != null ? vals.split(";") : new Object[0];
                        RightCriterium crit = CriteriumType.findByValue(type).createCriterium(criteriumId, qname, objs);
                        if (fixedPriority != 0) {
                            crit.setFixedPriority(fixedPriority);
                        }
                        RightImpl rightImpl = new RightImpl(crit, uuidVal, actionVal, dbUser);
                        returnsList.add(rightImpl);
                    } else {
                        returnsList.add(new RightImpl(null, uuidVal, actionVal, dbUser));
                    }
                } else {
                    // jenom pravo
                    returnsList.add(new RightImpl(null, uuidVal, actionVal, dbUser));
                }
                return true;
            }
        }.executeQuery(command);
        return ((rights != null) && (!rights.isEmpty())) ? (Right[]) rights.toArray(new Right[rights.size()]) : new Right[0];
    }

    public Provider<Connection> getProvider() {
        return provider;
    }

    public void setProvider(Provider<Connection> provider) {
        this.provider = provider;
    }

    @Override
    public EvaluatingResult resolve(RightCriteriumContext ctx, String uuid, String[] path, String action, User user) throws RightCriteriumException {
        List<String>uuids = new ArrayList<String>();
        uuids.add(uuid);
        for (String uuidOfPath : path) {
            if (!uuids.contains(uuidOfPath)) {
                uuids.add(uuidOfPath);
            }
        }
        uuids.add(SpecialObjects.REPOSITORY.getUuid());
        Right[] findRights = findRights((String[]) uuids.toArray(new String[uuids.size()]), action, user);
        findRights = SortingRightsUtils.sortRights(findRights, uuids);
        for (Right right : findRights) {
            ctx.setAssociatedUUID(right.getUUID());
            EvaluatingResult result = right.evaluate(ctx);
            ctx.setAssociatedUUID(null);
            if (result != EvaluatingResult.NOT_APPLICABLE) return result;
        }
        // nenasel zadne pravo nebo vsechny vracely NOT_APPLICABLE
        return EvaluatingResult.FALSE;
    }
}
