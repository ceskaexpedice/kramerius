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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

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
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;

import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.database.SecurityDatabaseUtils;
import cz.incad.kramerius.security.utils.RightsDBUtils;
import cz.incad.kramerius.security.utils.SortingRightsUtils;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

public class DatabaseRightsManager implements RightsManager {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DatabaseRightsManager.class.getName());

    
    @Inject
    @Named("kramerius4")
    Provider<Connection> provider;

    @Inject
    UserManager userManager;

    @Override
    public Right[] findRights(final String[] pids, final String action, final User user) {
        Group[] grps = user.getGroups();
        int[] grpIds = new int[grps.length];
        {
            for (int i = 0; i < grps.length; i++) {
                grpIds[i] = grps[i].getId();
            }
        }
        for (int i = 0; i < pids.length; i++) {
            if (!pids[i].startsWith("uuid:")) {
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
                int userId = rs.getInt("user");
                int groupId = rs.getInt("group");
                AbstractUser dbUser = null;
                if (userId > 0) {
                    dbUser = userManager.findUser(userId);
                } else {
                    dbUser = userManager.findGroup(groupId);
                }
                returnsList.add(RightsDBUtils.createRight(rs, dbUser));
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
    public EvaluatingResult resolve(RightCriteriumContext ctx, String uuid, String[] path, String action, User user) throws RightCriteriumException {
        List<String> pids = saturatePathAndCreatesPIDs(uuid, path);
        Right[] findRights = findRights((String[]) pids.toArray(new String[pids.size()]), action, user);
        findRights = SortingRightsUtils.sortRights(findRights, pids);
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

    @Override
    public List<String> saturatePathAndCreatesPIDs(String uuid, String[] path) {
        List<String> uuids = new ArrayList<String>();
        uuids.add(uuid);
        for (String uuidOfPath : path) {
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
                int userId = rs.getInt("user");
                int groupId = rs.getInt("group");
                AbstractUser dbUser = null;
                if (userId > 0) {
                    dbUser = userManager.findUser(userId);
                } else {
                    dbUser = userManager.findGroup(groupId);
                }
                returnsList.add(RightsDBUtils.createRight(rs, dbUser));
                return true;
            }
        }.executeQuery(sql, id);
        return ((rights != null) && (!rights.isEmpty())) ? rights.get(0) : null;
    }

    @Override
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
    public RightCriterium findRightCriteriumById(int critId) {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("findCriteriumById");
        List<RightCriterium> crits = new JDBCQueryTemplate<RightCriterium>(this.provider.get()) {

            @Override
            public boolean handleRow(ResultSet rs, List<RightCriterium> returnsList) throws SQLException {
                RightCriterium crit = RightsDBUtils.createCriterium(rs);
                returnsList.add(crit);
                return true;
            }
        }.executeQuery(template.toString(), critId);

        return !crits.isEmpty() ? crits.get(0) : null;
    }

    @Override
    public int insertRight(final Right right) throws SQLException {
        final RightCriterium criterium = right.getCriterium();
        final RightCriteriumParams params = criterium != null ? criterium.getCriteriumParams() : null;
        final Connection con = provider.get();
        return (Integer) new JDBCTransactionTemplate(con, true).updateWithTransaction(new JDBCCommand() {

            @Override
            public Object executeJDBCCommand() throws SQLException {
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
            public Object executeJDBCCommand() throws SQLException {
                if (criterium != null) {
                    if (criterium.getId() < 1) {
                        criterium.setId(insertRightCriteriumImpl(con, criterium));
                    } else {
                        updateRightCriteriumImpl(con, criterium);
                    }
                    return criterium.getId();
                } else {
                    return -1;
                }
            }
        }, new JDBCCommand() {

            @Override
            public Object executeJDBCCommand() throws SQLException {
                return insertRightImpl(con, right);
            }
        });
    }

    

    public void updateRight(final Right right) throws SQLException {
        final RightCriterium criterium = right.getCriterium();
        final RightCriteriumParams params = criterium != null ? criterium.getCriteriumParams() : null;
        final Connection con = provider.get();
        new JDBCTransactionTemplate(con, true).updateWithTransaction(new JDBCCommand() {

            @Override
            public Object executeJDBCCommand() throws SQLException {
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
            public Object executeJDBCCommand() throws SQLException {
                if (criterium != null) {
                    if (criterium.getId() < 1) {
                        criterium.setId(insertRightCriteriumImpl(con, criterium));
                    } else {
                        updateRightCriteriumImpl(con, criterium);
                    }
                    return criterium.getId();
                } else {
                    return -1;
                }
            }
        }, new JDBCCommand() {

            @Override
            public Object executeJDBCCommand() throws SQLException {
                updateRightImpl(con, right);
                return -1;
            }
        });
    }

    public void updateRightImpl(Connection con, Right right) throws SQLException {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("updateRight");
        template.setAttribute("right", right);
        template.setAttribute("association", right.getUser() instanceof Group ? "group" : "user");
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(con, false);
        String sql = template.toString();
        LOGGER.info(sql);
        jdbcTemplate.executeUpdate(sql);
    }

    public void updateRightCriterium(final RightCriterium criterium) throws SQLException {
        final RightCriteriumParams params = criterium.getCriteriumParams();
        final Connection con = provider.get();
        new JDBCTransactionTemplate(con, true).updateWithTransaction(new JDBCCommand() {
            @Override
            public Object executeJDBCCommand() throws SQLException {
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
            public Object executeJDBCCommand() throws SQLException {
                updateRightCriteriumImpl(con, criterium);
                return -1;
            }
        });
    }
    
    
    @Override
    public void deleteRight(final Right right) throws SQLException {
        final Connection con = provider.get();
        new JDBCTransactionTemplate(con, true).updateWithTransaction(new JDBCCommand() {
            @Override
            public Object executeJDBCCommand() throws SQLException {
                deleteRightImpl(con, right);
                return -1;
            }
        }, new JDBCCommand() {
            @Override
            public Object executeJDBCCommand() throws SQLException {
                deleteRightCriteriumImpl(con, right.getCriterium());
                return -1;
            }
        });
    }

    public void deleteRightImpl(Connection con, Right right) throws SQLException {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("deleteRight");
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(con, false);
        String sql = template.toString();
        LOGGER.info(sql);
        jdbcTemplate.executeUpdate(sql, right.getId());
    }

    public void deleteRightCriteriumImpl(Connection con, RightCriterium criterium) throws SQLException {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("deleteRightCriterium");
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(con, false);
        String sql = template.toString();
        LOGGER.info(sql);
        jdbcTemplate.executeUpdate(sql, criterium.getId());
    }

    public void updateRightCriteriumImpl(Connection con, RightCriterium criterium) throws SQLException {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("updateRightCriterium");
        template.setAttribute("criterium", criterium);
        template.setAttribute("priority", criterium.getFixedPriority() == 0 ? "NULL" : "" + criterium.getFixedPriority());
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(con, false);
        String sql = template.toString();
        LOGGER.info(sql);
        jdbcTemplate.executeUpdate(sql);
    }

    public void updateRightCriteriumParamsImpl(Connection con, RightCriteriumParams params) throws SQLException {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("updateRightCriteriumParams");
        template.setAttribute("params", params);
        Connection connection = this.provider.get();
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(connection, false);
        String sql = template.toString();
        LOGGER.info(sql);
        jdbcTemplate.executeUpdate(sql);
    }

    public int insertRightImpl(Connection con, Right right) throws SQLException {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("insertRight");
        template.setAttribute("association", right.getUser() instanceof Group ? "group" : "user");
        template.setAttribute("right", right);
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(con, false);
        String sql = template.toString();
        LOGGER.info(sql);
        return jdbcTemplate.executeUpdate(sql);
    }


    @Override
    public int insertRightCriterium(final RightCriterium criterium) throws SQLException {
        final RightCriteriumParams params = criterium.getCriteriumParams();
        final Connection con = provider.get();

        return (Integer) new JDBCTransactionTemplate(con, true).updateWithTransaction(new JDBCCommand() {
            @Override
            public Object executeJDBCCommand() throws SQLException {
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
            public Object executeJDBCCommand() throws SQLException {
                return insertRightCriteriumImpl(con, criterium);
            }
        });

    }

    public int insertRightCriteriumImpl(Connection con, RightCriterium criterium) throws SQLException {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("insertRightCriterium");
        template.setAttribute("criterium", criterium);
        template.setAttribute("priority", criterium.getFixedPriority() == 0 ? "NULL" : "" + criterium.getFixedPriority());
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(con, false);
        String sql = template.toString();
        LOGGER.info(sql);
        return jdbcTemplate.executeUpdate(sql);
    }

    @Override
    public int insertRightCriteriumParams(final RightCriteriumParams criteriumParams) throws SQLException {
        final Connection con = provider.get();
        return (Integer) new JDBCTransactionTemplate(con, true).updateWithTransaction(new JDBCCommand() {
            @Override
            public Object executeJDBCCommand() throws SQLException {
                return insertRightCriteriumParamsImpl(con, criteriumParams);
            }
        });
    }

    public int insertRightCriteriumParamsImpl(Connection con, RightCriteriumParams criteriumParams) throws SQLException {
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("insertRightCriteriumParams");
        template.setAttribute("params", criteriumParams);
        Connection connection = this.provider.get();
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(connection, false);
        String sql = template.toString();
        LOGGER.info(sql);
        return jdbcTemplate.executeUpdate(sql);
    }

    @Override
    public void updateRightCriteriumParams(RightCriteriumParams criteriumParams) throws SQLException {
        final Connection con = provider.get();  
        updateRightCriteriumParamsImpl(con, criteriumParams);
    }

    
}

