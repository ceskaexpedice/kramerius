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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightCriteriumWrapper;
import cz.incad.kramerius.security.RightCriteriumWrapperFactory;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.database.InitSecurityDatabase;
import cz.incad.kramerius.security.database.SecurityDatabaseUtils;
import cz.incad.kramerius.security.utils.RightsDBUtils;
import cz.incad.kramerius.security.utils.SecurityDBUtils;
import cz.incad.kramerius.security.utils.SortingRightsUtils;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;
import cz.incad.kramerius.virtualcollections.CollectionException;
import cz.incad.kramerius.virtualcollections.CollectionsManager;

public class DatabaseRightsManager implements RightsManager {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DatabaseRightsManager.class.getName());

    
    @Inject
    @Named("kramerius4")
    Provider<Connection> provider;

    @Inject
    UserManager userManager;

    @Inject
    RightCriteriumWrapperFactory criteriumWrapperFactory;
    
    @Inject
    @Named("solr")
    CollectionsManager colGet;
    
    @Override
    @InitSecurityDatabase
    public Right[] findAllRights(String[] pids, String action) {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("findAllRightsFromWithGroups");
        template.setAttribute("pids", pids);
        template.setAttribute("action", action);
        
        
        String sql = template.toString();

        List<Right> rights = new JDBCQueryTemplate<Right>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Right> returnsList) throws SQLException {
                int userId = rs.getInt("user_id");
                int groupId = rs.getInt("group_id");
                AbstractUser dbUser = null;
                if (userId > 0) {
                    dbUser = userManager.findUser(userId);
                } else {
                    dbUser = userManager.findRole(groupId);
                }
                returnsList.add(RightsDBUtils.createRight(rs, dbUser, criteriumWrapperFactory));
                return true;
            }
        }.executeQuery(sql);
        return ((rights != null) && (!rights.isEmpty())) ? (Right[]) rights.toArray(new Right[rights.size()]) : new Right[0];
    }

    
   
    
    @Override
	public Right[] findRights(final String[] ids, final String[] pids, final String[] actions, final String[] rnames) {
    	Map<String, List<String>> map = new HashMap<String, List<String>>();
    	if (ids.length > 0) {
        	map.put("id", Arrays.asList(ids));
    	}
    	if (pids.length > 0) {
        	map.put("uuid", Arrays.asList(pids));
    	}
    	if (actions.length > 0) {
        	map.put("action", Arrays.asList(actions));
    	}
    	if (rnames.length > 0) {
        	map.put("gname", Arrays.asList(rnames));
    	}

    	StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("findAllRights");
    	template.setAttribute("params", map);
    	
    	String sql = template.toString();
    	List<Right> rights = new JDBCQueryTemplate<Right>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Right> returnsList) throws SQLException {
                int userId = rs.getInt("user_id");
                int groupId = rs.getInt("group_id");
                AbstractUser dbUser = null;
                if (userId > 0) {
                    dbUser = userManager.findUser(userId);
                } else {
                    dbUser = userManager.findRole(groupId);
                }
                returnsList.add(RightsDBUtils.createRight(rs, dbUser, criteriumWrapperFactory));
                return true;
            }
        }.executeQuery(sql);
        
        return ((rights != null) && (!rights.isEmpty())) ? (Right[]) rights.toArray(new Right[rights.size()]) : new Right[0];
	}




	@InitSecurityDatabase
    public Right[] findRightsForGroup(final String[] pids, final String action, final Role group) {
        for (int i = 0; i < pids.length; i++) {
            if (!pids[i].startsWith("uuid:")) {
                pids[i] = "uuid:" + pids[i];
            }
        }
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("findRightsForGroup");
        template.setAttribute("pids", pids);
        template.setAttribute("groups", new int[] {group.getId()});
        template.setAttribute("action", action);

        String sql = template.toString();

        List<Right> rights = new JDBCQueryTemplate<Right>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Right> returnsList) throws SQLException {
                int userId = rs.getInt("user_id");
                int groupId = rs.getInt("group_id");
                AbstractUser dbUser = null;
                if (userId > 0) {
                    dbUser = userManager.findUser(userId);
                } else {
                    dbUser = userManager.findRole(groupId);
                }
                returnsList.add(RightsDBUtils.createRight(rs, dbUser, criteriumWrapperFactory));
                return true;
            }
        }.executeQuery(sql);
        return ((rights != null) && (!rights.isEmpty())) ? (Right[]) rights.toArray(new Right[rights.size()]) : new Right[0];
    }
    
    @Override
    @InitSecurityDatabase
    public Right[] findRights(final String[] pids, final String action, final User user) {
        Role[] grps = user.getGroups();
        int[] grpIds = new int[grps.length];
        {
            for (int i = 0; i < grps.length; i++) {
                grpIds[i] = grps[i].getId();
            }
        }
        for (int i = 0; i < pids.length; i++) {
            if (!pids[i].startsWith("uuid:") && !pids[i].startsWith("vc:")) {
                pids[i] = "uuid:" + pids[i];
            }
        }
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("findRightFromWithGroups");
        template.setAttribute("pids", pids);
        template.setAttribute("groups", grpIds);
        template.setAttribute("user", user.getId());
        template.setAttribute("action", action);

        String sql = template.toString();

        List<Right> rights = new JDBCQueryTemplate<Right>(this.provider.get()) {
            
            @Override
            public boolean handleRow(ResultSet rs, List<Right> returnsList) throws SQLException {
                //userId - blby
                int userId = rs.getInt("user_id");
                int groupId = rs.getInt("group_id");

                AbstractUser dbUser = null;
                LOGGER.fine("finding user ");
                if (userId > 0) {
                    dbUser = SecurityDBUtils.createUser(rs);
                } else {
                    dbUser = SecurityDBUtils.createUser(rs);
                }
                
                returnsList.add(RightsDBUtils.createRight(rs, dbUser, criteriumWrapperFactory));
                return true;
            }
        }.executeQuery(sql);
        return ((rights != null) && (!rights.isEmpty())) ? (Right[]) rights.toArray(new Right[rights.size()]) : new Right[0];
    }

    public Provider<Connection> getProvider() {
        return provider;
    }

    public void setProvider(Provider<Connection> provider) {
        this.provider = provider;
    }

    
    

    @Override
    @InitSecurityDatabase
    public EvaluatingResult resolve(RightCriteriumContext ctx, String uuid, ObjectPidsPath path, String action, User user) throws RightCriteriumException {
        ObjectPidsPath processPath=path.injectRepository();
        if (!SpecialObjects.isSpecialObject(uuid)) {
            try {
                processPath = processPath.injectCollections(this.colGet);
            } catch (CollectionException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        //List<String> pids = Arrays.asList(path.injectRepository().getPathFromRootToLeaf());
        String[] pids = processPath.getPathFromLeafToRoot();
        
        Right[] findRights = findRights(pids, action, user);
        findRights = SortingRightsUtils.sortRights(findRights, processPath);
        for (Right right : findRights) {
            ctx.setAssociatedPid(right.getPid());
            EvaluatingResult result = right.evaluate(ctx);
            ctx.setAssociatedPid(null);
            if (result != EvaluatingResult.NOT_APPLICABLE)
                return result;
        }
        // nenasel zadne pravo nebo vsechny vracely NOT_APPLICABLE
        return EvaluatingResult.FALSE;
    }

    @InitSecurityDatabase
    public EvaluatingResult[] resolveAllPath(RightCriteriumContext ctx, String pid, ObjectPidsPath path, String action, User user) throws RightCriteriumException {
        Right[] findRights = findRights(path.getPathFromLeafToRoot(), action, user);
        findRights = SortingRightsUtils.sortRights(findRights, path);
        EvaluatingResult[] results = new EvaluatingResult[path.getLength()];
        for (int i = 0; i < results.length; i++) {
            String curPid = path.getNodeFromLeafToRoot(i);
            ObjectPidsPath restPath = path.cutTail(i);
            
            EvaluatingResult result = EvaluatingResult.FALSE;
            for (Right right : findRights) {
                
                boolean thisPid = right.getPid().equals(curPid);
                boolean inTheRestOfPath = restPath.contains(right.getPid());
                if (thisPid || inTheRestOfPath) {
                    ctx.setAssociatedPid(right.getPid());
                    EvaluatingResult iresult = right.evaluate(ctx);
                    ctx.setAssociatedPid(null);
                    if (iresult != EvaluatingResult.NOT_APPLICABLE) {
                        result = iresult;
                        break;
                    }
                }
            }
            
            results[i] = result;
        }
        return results;
    }

    @Override
    @InitSecurityDatabase
    public List<String> saturatePathAndCreatesPIDs(String uuid, String[] path) {
        ArrayList<String> spath = new ArrayList<String>(Arrays.asList(path));
        Collections.reverse(spath);
        List<String> uuids = new ArrayList<String>();
        uuids.add(uuid);
        for (String uuidOfPath : spath) {
            if (!uuids.contains(uuidOfPath)) {
                uuids.add(uuidOfPath);
            }
        }
        if ((!uuid.equals(SpecialObjects.REPOSITORY.getUuid())) && (!Arrays.asList(path).contains(SpecialObjects.REPOSITORY.getUuid()))) {
            uuids.add(SpecialObjects.REPOSITORY.getUuid());
        }
        for (int i = 0; i < uuids.size(); i++) {
            String cuuid = uuids.get(i);
            if (!cuuid.startsWith("uuid:")) {
                uuids.set(i, "uuid:" + cuuid);
            }
        }
        return uuids;
    }
    
    

    @Override
    public Right findRightById(int id) {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("findRightById");
        String sql = template.toString();
        List<Right> rights = new JDBCQueryTemplate<Right>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Right> returnsList) throws SQLException {
                int userId = rs.getInt("user_id");
                int groupId = rs.getInt("group_id");
                AbstractUser dbUser = null;
                if (userId > 0) {
                    dbUser = userManager.findUser(userId);
                } else {
                    dbUser = userManager.findRole(groupId);
                }
                returnsList.add(RightsDBUtils.createRight(rs, dbUser, criteriumWrapperFactory));
                return true;
            }
        }.executeQuery(sql, id);
        return ((rights != null) && (!rights.isEmpty())) ? rights.get(0) : null;
    }

    @Override
    @InitSecurityDatabase
    public RightCriteriumParams[] findAllParams() {

        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("findAllCriteriumParams");

        List<RightCriteriumParams> crits = new JDBCQueryTemplate<RightCriteriumParams>(this.provider.get()) {

            @Override
            public boolean handleRow(ResultSet rs, List<RightCriteriumParams> returnsList) throws SQLException {

                String shortDesc = rs.getString("short_desc");
                String longDesc = rs.getString("long_desc");
                int critParamId = rs.getInt("crit_param_id");
                String vals = rs.getString("vals");

                RightCriteriumParamsImpl params = new RightCriteriumParamsImpl(critParamId);
                params.setLongDescription(longDesc);
                params.setShortDescription(shortDesc);
                params.setObjects(RightsDBUtils.valsFromString(vals));

                returnsList.add(params);

                return true;
            }
        }.executeQuery(template.toString());

        return (RightCriteriumParams[]) crits.toArray(new RightCriteriumParams[crits.size()]);
    }

    @Override
    @InitSecurityDatabase
    public RightCriteriumParams findParamById(int paramId) {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("findCriteriumParamsById");
        List<RightCriteriumParams> crits = new JDBCQueryTemplate<RightCriteriumParams>(this.provider.get()) {

            @Override
            public boolean handleRow(ResultSet rs, List<RightCriteriumParams> returnsList) throws SQLException {
                RightCriteriumParams params = RightsDBUtils.createCriteriumParams(rs);
                returnsList.add(params);
                return true;
            }
        }.executeQuery(template.toString(), paramId);

        return !crits.isEmpty() ? crits.get(0) : null;
    }

    @Override
    public RightCriteriumWrapper findRightCriteriumById(int critId) {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("findCriteriumById");
        List<RightCriteriumWrapper> crits = new JDBCQueryTemplate<RightCriteriumWrapper>(this.provider.get()) {

            @Override
            public boolean handleRow(ResultSet rs, List<RightCriteriumWrapper> returnsList) throws SQLException {
                RightCriteriumWrapper wrapper = RightsDBUtils.createCriteriumWrapper(criteriumWrapperFactory, rs);
                returnsList.add(wrapper);
                return true;
            }
        }.executeQuery(template.toString(), critId);

        return !crits.isEmpty() ? crits.get(0) : null;
    }

    @Override
    @InitSecurityDatabase
    public int insertRight(final Right right) throws SQLException {
        final RightCriteriumWrapper criteriumWrapper = right.getCriteriumWrapper();
        final RightCriteriumParams params = criteriumWrapper != null ? criteriumWrapper.getCriteriumParams() : null;
        final Connection con = provider.get();
        return (Integer) new JDBCTransactionTemplate(con, true).updateWithTransaction(new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                if (params != null) {
                    if (params.getId() < 1) {
                        params.setId(insertRightCriteriumParamsImpl(con, params));
                    } else {
                        updateRightCriteriumParamsImpl(con, params);
                    }
                    return params.getId();
                } else {
                    return -1;
                }
            }
        }, new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                if (criteriumWrapper != null) {
                    if (criteriumWrapper.getId() < 1) {
                        criteriumWrapper.setId(insertRightCriteriumImpl(con, criteriumWrapper));
                    } else {
                        updateRightCriteriumImpl(con, criteriumWrapper);
                    }
                    return criteriumWrapper.getId();
                } else {
                    return -1;
                }
            }
        }, new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                return insertRightImpl(con, right);
            }
        });
    }

    

    @InitSecurityDatabase
    public void updateRight(final Right right) throws SQLException {
        final RightCriteriumWrapper criteriumWrapper = right.getCriteriumWrapper();
        final RightCriteriumParams params = criteriumWrapper != null ? criteriumWrapper.getCriteriumParams() : null;
        LOGGER.log(Level.FINE, "got connection from provider ");
        final Connection con = provider.get();
        new JDBCTransactionTemplate(con, true).updateWithTransaction(new JDBCCommand() {
            
            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                if (params != null) {
                    if (params.getId() < 1) {
                        params.setId(insertRightCriteriumParamsImpl(con, params));
                    } else {
                        updateRightCriteriumParamsImpl(con, params);
                    }
                    return params.getId();
                } else {
                    return -1;
                }
            }
        }, new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                if (criteriumWrapper != null) {
                    if (criteriumWrapper.getId() < 1) {
                        criteriumWrapper.setId(insertRightCriteriumImpl(con, criteriumWrapper));
                    } else {
                        updateRightCriteriumImpl(con, criteriumWrapper);
                    }
                    return criteriumWrapper.getId();
                } else {
                    return -1;
                }
            }
        }, new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                updateRightImpl(con, right);
                return -1;
            }
        });
    }

    @InitSecurityDatabase
    public void updateRightImpl(Connection con, Right right) throws SQLException {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("updateRight");
        template.setAttribute("right", right);
        template.setAttribute("association", right.getUser() instanceof Role ? "group_id" : "user_id");
        template.setAttribute("priority", right.getFixedPriority() == 0 ? "NULL" : "" + right.getFixedPriority());
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(con, false);
        String sql = template.toString();
        LOGGER.fine(sql);
        jdbcTemplate.executeUpdate(sql);
    }

    @InitSecurityDatabase
    public void updateRightCriterium(final RightCriteriumWrapper criterium) throws SQLException {
        final RightCriteriumParams params = criterium.getCriteriumParams();
        final Connection con = provider.get();
        new JDBCTransactionTemplate(con, true).updateWithTransaction(new JDBCCommand() {
            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                if (params != null) {
                    if (params.getId() < 1) {
                        params.setId(insertRightCriteriumParamsImpl(con, params));
                    } else {
                        updateRightCriteriumParamsImpl(con, params);
                    }
                    return params.getId();
                } else {
                    return -1;
                }
            }
        }, new JDBCCommand() {
            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                updateRightCriteriumImpl(con, criterium);
                return -1;
            }
        });
    }
    
    
    @Override
    @InitSecurityDatabase
    public void deleteRight(final Right right) throws SQLException {
        final Connection con = provider.get();
        new JDBCTransactionTemplate(con, true).updateWithTransaction(new JDBCCommand() {
            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                deleteRightImpl(con, right);
                return -1;
            }
        }, new JDBCCommand() {
            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                if (right.getCriteriumWrapper() != null) {
                    deleteRightCriteriumImpl(con, right.getCriteriumWrapper());
                }
                return -1;
            }
        });
    }
    
    

    @Override
    public void deleteRightCriteriumParams(final int id) throws SQLException {
        final Connection connection = this.provider.get();

        List<Integer> ids = new JDBCQueryTemplate<Integer>(connection,false) {
            @Override
            public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                int id = rs.getInt("crit_id");
                returnsList.add(id);
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery(SecurityDatabaseUtils.stGroup().getInstanceOf("findCriteriumsDependsOnParams").toString(), id);

        List<JDBCCommand> commands = new ArrayList<JDBCCommand>();
        for (final Integer criteriumId : ids) {

            commands.add(new JDBCCommand() {
                @Override
                public Object executeJDBCCommand(Connection con) throws SQLException {
                    DatabaseRightsManager.this.deleteRightCriterium(criteriumId);
                    return null;
                }
            });
        }
        
        commands.add(new JDBCCommand() {
            
            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("deleteRightCriteriumParams");
                JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(provider.get(), true);
                String sql = template.toString();
                LOGGER.fine(sql);
                jdbcTemplate.executeUpdate(sql, id);
                return null;
            }
        });
        
        new JDBCTransactionTemplate(connection, true).updateWithTransaction(commands);
        
    }

    

    @Override
    public void deleteRightCriterium(int id) throws SQLException {
        this.deleteRightCriteriumImpl(this.provider.get(), id);
    }


    @InitSecurityDatabase
    public void deleteRightImpl(Connection con, Right right) throws SQLException {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("deleteRight");
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(con, false);
        String sql = template.toString();
        LOGGER.fine(sql);
        jdbcTemplate.executeUpdate(sql, right.getId());
    }

    public void deleteRightCriteriumImpl(Connection con, RightCriteriumWrapper criterium) throws SQLException {
        deleteRightCriteriumImpl(con, criterium.getId());
    }


    public void deleteRightCriteriumImpl(Connection con, int id) throws SQLException {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("deleteRightCriterium");
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(con, false);
        String sql = template.toString();
        LOGGER.fine(sql);
        jdbcTemplate.executeUpdate(sql, id);
    }

    public void updateRightCriteriumImpl(Connection con, RightCriteriumWrapper criteriumWrapper) throws SQLException {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("updateRightCriterium");
        template.setAttribute("criteriumWrapper", criteriumWrapper);
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(con, false);
        String sql = template.toString();
        LOGGER.fine(sql);
        jdbcTemplate.executeUpdate(sql);
    }

    @InitSecurityDatabase
    public void updateRightCriteriumParamsImpl(Connection con, RightCriteriumParams params) throws SQLException {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("updateRightCriteriumParams");
        template.setAttribute("params", params);
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(con, false);
        String sql = template.toString();
        LOGGER.fine(sql);
        jdbcTemplate.executeUpdate(sql);
    }

    @InitSecurityDatabase
    public int insertRightImpl(Connection con, Right right) throws SQLException {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("insertRight");
        template.setAttribute("association", right.getUser() instanceof Role ? "group_id" : "user_id");
        template.setAttribute("right", right);
        template.setAttribute("priority", right.getFixedPriority() == 0 ? "NULL" : "" + right.getFixedPriority());
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(con, false);
        String sql = template.toString();
        LOGGER.fine(sql);
        return jdbcTemplate.executeUpdate(sql);
    }


    @Override
    @InitSecurityDatabase
    public int insertRightCriterium(final RightCriteriumWrapper criterium) throws SQLException {
        final RightCriteriumParams params = criterium.getCriteriumParams();
        final Connection con = provider.get();

        return (Integer) new JDBCTransactionTemplate(con, true).updateWithTransaction(new JDBCCommand() {
            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                if (params != null) {
                    if (params.getId() < 1) {
                        params.setId(insertRightCriteriumParamsImpl(con, params));
                    } else {
                        updateRightCriteriumParamsImpl(con, params);
                    }
                    return params.getId();
                } else {
                    return -1;
                }
            }
        }, new JDBCCommand() {
            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                return insertRightCriteriumImpl(con, criterium);
            }
        });

    }

    @InitSecurityDatabase
    public int insertRightCriteriumImpl(Connection con, RightCriteriumWrapper criterium) throws SQLException {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("insertRightCriterium");
        template.setAttribute("criteriumWrapper", criterium);
        template.setAttribute("type", criterium.getCriteriumType().getVal());
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(con, false);
        String sql = template.toString();
        LOGGER.fine(sql);
        return jdbcTemplate.executeUpdate(sql);
    }

    @Override
    @InitSecurityDatabase
    public int insertRightCriteriumParams(final RightCriteriumParams criteriumParams) throws SQLException {
        final Connection con = provider.get();
        return (Integer) new JDBCTransactionTemplate(con, true).updateWithTransaction(new JDBCCommand() {
            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                return insertRightCriteriumParamsImpl(con, criteriumParams);
            }
        });
    }

    @InitSecurityDatabase
    public int insertRightCriteriumParamsImpl(Connection con, RightCriteriumParams criteriumParams) throws SQLException {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("insertRightCriteriumParams");
        template.setAttribute("params", criteriumParams);
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(con, false);
        String sql = template.toString();
        LOGGER.fine(sql);
        return jdbcTemplate.executeUpdate(sql);
    }

    @Override
    @InitSecurityDatabase
    public void updateRightCriteriumParams(RightCriteriumParams criteriumParams) throws SQLException {
        final Connection con = provider.get();  
        updateRightCriteriumParamsImpl(con, criteriumParams);
    }


    @Override
    public int[] findUsedRoleIDs() {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("findUsedRoles");
        List<Integer> ids = new JDBCQueryTemplate<Integer>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                int val = rs.getInt("group_id");
                if (val > 0) {
                    returnsList.add(val);
                }
                return true;
            }
        }.executeQuery(template.toString());
     
        int[] retArray = new int[ids.size()];
        for (int i = 0; i < retArray.length; i++) {
            retArray[i] = ids.get(i);
        }
        return retArray;
    }


    @Override
    public List<Map<String,String>> findObjectUsingParams(int paramId) {

        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("select_object_using_param");
        List<Map<String,String>> vals = new JDBCQueryTemplate<Map<String,String>>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Map<String,String>> returnsList) throws SQLException {
                String pid = rs.getString("pid");
                String action = rs.getString("action");
                Map<String, String> map = new HashMap<String, String>(); {
                    map.put("pid", pid);
                    map.put("action", action);
                }
                returnsList.add(map);
                return true;
            }
        }.executeQuery(template.toString(), new Integer(paramId));
        return vals;
    }
    
}



