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
package cz.incad.kramerius.security.database;

import static cz.incad.kramerius.database.cond.ConditionsInterpretHelper.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.lang3.tuple.Triple;

import cz.incad.kramerius.database.VersionService;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.impl.embedded.cz.CzechEmbeddedLicenses;
import cz.incad.kramerius.security.licenses.impl.embedded.sk.SlovakEmbeddedLicenses;
import cz.incad.kramerius.users.database.LoggedUserDbHelper;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

public class SecurityDbInitializer {

    static Logger LOGGER = Logger.getLogger(SecurityDbInitializer.class.getName());

    public static void initDatabase(Connection connection, VersionService versionService) {
        try {

            if (!DatabaseUtils.tableExists(connection, "USER_ENTITY")) {
                createSecurityTables(connection);
            }

            if (versionService.getVersion() == null) {

                // column DEACTIVATED has been created
                makeSureThatUserEntity_DEACTIVATED(connection);

                //TODO: Move method
                LoggedUserDbHelper.createLoggedUsersTablesIfNotExists(connection);

                // the profiles table has been created + insert public role
                makeSureThatProfilesTable(connection);

                // public role created
                makeSurePublicRoleInserted(connection);


                updateUserEntityTable(connection);
                updateShowItems(connection);

                // labels table
                makeSureThatLabelsTable(connection);

                makeSureThatRoleColumnExists(connection);

                createNewActions(connection);

                insertGenerateContent(connection);

            } else {
                String v = versionService.getVersion();

                if (versionCondition(v, ">=", "4.5.0") && versionCondition(v, "<=", "4.8.0")) {

                    makeSureThatUserEntity_DEACTIVATED(connection);

                    //TODO: Move method
                    LoggedUserDbHelper.createLoggedUsersTablesIfNotExists(connection);
                    // the profiles table has been created
                    makeSureThatProfilesTable(connection);
                    // create public role
                    makeSurePublicRoleInserted(connection);

                    updateUserEntityTable(connection);
                    updateShowItems(connection);

                    // labels table
                    makeSureThatLabelsTable(connection);
                    makeSureThatRoleColumnExists(connection);
                    createNewActions(connection);

                    insertGenerateContent(connection);

                } else if (versionCondition(v, "=", "5.3.0")) {


                    updateUserEntityTable(connection);
                    updateShowItems(connection);

                    // labels table
                    makeSureThatLabelsTable(connection);
                    makeSureThatRoleColumnExists(connection);
                    createNewActions(connection);

                    insertGenerateContent(connection);

                } else if (versionCondition(v, "=", "5.4.0")) {

                    updateUserEntityTable(connection);
                    updateShowItems(connection);

                    // labels table
                    makeSureThatLabelsTable(connection);
                    makeSureThatRoleColumnExists(connection);
                    createNewActions(connection);

                    insertGenerateContent(connection);

                } else if (versionCondition(v, "=", "5.5.0")) {

                    updateUserEntityTable(connection);
                    updateShowItems(connection);

                    // labels table
                    makeSureThatLabelsTable(connection);
                    makeSureThatRoleColumnExists(connection);
                    createNewActions(connection);

                    insertGenerateContent(connection);

                } else if (versionCondition(v, "=", "5.6.0")) {

                    updateUserEntityTable(connection);
                    updateShowItems(connection);

                    // labels table
                    makeSureThatLabelsTable(connection);
                    makeSureThatRoleColumnExists(connection);
                    createNewActions(connection);

                    insertGenerateContent(connection);

                } else if (versionCondition(v, "=", "5.7.0")) {

                    updateUserEntityTable(connection);
                    updateShowItems(connection);

                    // labels table
                    makeSureThatLabelsTable(connection);
                    makeSureThatRoleColumnExists(connection);
                    createNewActions(connection);

                    insertGenerateContent(connection);

                } else if (versionCondition(v, "=", "5.8.0")) {

                    updateUserEntityTable(connection);
                    updateShowItems(connection);

                    // labels table
                    makeSureThatLabelsTable(connection);
                    makeSureThatRoleColumnExists(connection);
                    createNewActions(connection);

                    insertGenerateContent(connection);

                } else if (versionCondition(v, "=", "5.9.0")) {

                    updateUserEntityTable(connection);
                    updateShowItems(connection);

                    // labels table
                    makeSureThatLabelsTable(connection);
                    makeSureThatRoleColumnExists(connection);
                    createNewActions(connection);

                    insertGenerateContent(connection);

                } else if ((versionCondition(v, ">", "5.9.0")) && (versionCondition(v, "<", "6.3.0"))) {

                    updateUserEntityTable(connection);
                    updateShowItems(connection);

                    // labels table
                    makeSureThatLabelsTable(connection);
                    makeSureThatRoleColumnExists(connection);
                    createNewActions(connection);

                    insertGenerateContent(connection);

                } else if (versionCondition(v, "=", "6.3.0")) {
                    updateUserEntityTable(connection);
                    updateShowItems(connection);

                    // labels table
                    makeSureThatLabelsTable(connection);
                    makeSureThatRoleColumnExists(connection);
                    createNewActions(connection);

                    insertGenerateContent(connection);

                } else if (versionCondition(v, "=", "6.6.0")) {
                    updateUserEntityTable(connection);
                    updateShowItems(connection);

                    // labels table
                    makeSureThatLabelsTable(connection);
                    makeSureThatRoleColumnExists(connection);
                    createNewActions(connection);

                    insertGenerateContent(connection);

                } else if (versionCondition(v, ">", "6.6.0") && versionCondition(v, "<=", "6.6.2")) {
                    updateShowItems(connection);
                    // labels table
                    makeSureThatLabelsTable(connection);
                    makeSureThatRoleColumnExists(connection);
                    createNewActions(connection);

                    insertGenerateContent(connection);

                } else if (versionCondition(v, ">", "6.6.2") && versionCondition(v, "<", "7.0.2")) {

                    // labels table
                    makeSureThatLabelsTable(connection);
                    makeSureThatRoleColumnExists(connection);
                    createNewActions(connection);

                    insertGenerateContent(connection);

                } else if (versionCondition(v, ">=", "7.0.2") && versionCondition(v, "<", "7.0.6")) {
                    insertGenerateContent(connection);
                }
                // insertAggregateRight
            }

            // checks role column in right entity
            makeSureThatRoleColumnExists(connection);
            // update role column from relations
            makeSureRolesInTable(connection);
            
            // exclusive lock
            makeSureThatLicensesTableContainsExclusiveLockColumns(connection);
            
            updateExistingActions(connection);

            // runtime license columns
            makeSureThatRuntimeColumnsExists(connection);
            dropDeprecatedLabels(connection);

            // authenticated users
            makeSureAuthenticatedUsers(connection);

            checkLabelExists(connection);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    
    private static void dropDeprecatedLabels(Connection connection) {
        CzechEmbeddedLicenses.DEPRECATED_LICENSES.forEach(lic->{

            List<String> labels = new JDBCQueryTemplate<String>(connection, false) {
                @Override
                public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                    returnsList.add(rs.getString("label_name"));
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery("select * from labels_entity where label_name = ?", lic.getName());
            if (!labels.isEmpty()) {
                try {
                    JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
                    template.setUseReturningKeys(false);

                    template.executeUpdate(
                            "DELETE FROM labels_entity WHERE label_name = ?",
                            lic.getName()
                        );
                    
//                    template.executeUpdate(
//                            "insert into labels_entity(label_id,label_group,label_name, label_description, label_priority) \n" +
//                                    "values(nextval('LABEL_ID_SEQUENCE'), 'embedded', ?, ?, (select coalesce(max(label_priority),0)+1 from labels_entity))",
//                            lic.getName(), lic.getDescription());

                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, String.format("Cannot create embedded label %s", lic.getName()));
                }

            }
        });
    }
    



    
    private static void checkLabelExists(Connection connection) {

        /** Czech global licenses **/
        CzechEmbeddedLicenses.LICENSES.forEach(lic-> {
            List<String> labels = new JDBCQueryTemplate<String>(connection, false) {
                @Override
                public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                    returnsList.add(rs.getString("label_name"));
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery("select * from labels_entity where label_name = ?", lic.getName());
            if (labels.isEmpty()) {
                try {
                    JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
                    template.setUseReturningKeys(false);
                    template.executeUpdate(
                            "insert into labels_entity(label_id,label_group,label_name, label_description, label_priority, runtime, runtime_type) \n" +
                                    "values(nextval('LABEL_ID_SEQUENCE'), 'embedded', ?, ?, (select coalesce(max(label_priority),0)+1 from labels_entity), ?, ?)",
                            lic.getName(), lic.getDescription(), lic.isRuntimeLicense(), lic.getRuntimeLicenseType() != null ? lic.getRuntimeLicenseType().name() : new JDBCUpdateTemplate.NullObject(String.class));
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, String.format("Cannot create embedded label %s", lic.getName()));
                }
            }
        });

        
        
        
        /** Slovak global licenses **/
        /*
        SlovakEmbeddedLicenses.LICENSES.forEach(lic-> {
            List<String> labels = new JDBCQueryTemplate<String>(connection, false) {
                @Override
                public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                    returnsList.add(rs.getString("label_name"));
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery("select * from labels_entity where label_name = ?", lic.getName());
            if (labels.isEmpty()) {
                try {
                    JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
                    template.setUseReturningKeys(false);
                    template.executeUpdate(
                            "insert into labels_entity(label_id,label_group,label_name, label_description, label_priority) \n" +
                                    "values(nextval('LABEL_ID_SEQUENCE'), 'embedded', ?, ?, (select coalesce(max(label_priority),0)+1 from labels_entity))",
                            lic.getName(), lic.getDescription());

                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, String.format("Cannot create embedded label %s", lic.getName()));

                }
            }
        });*/
        
        /** update group */
        List<License> all = new ArrayList<>(CzechEmbeddedLicenses.LICENSES);
        all.addAll(SlovakEmbeddedLicenses.LICENSES);
        updateGlobalLicense(all, connection);
    }
    
    
    private static void updateGlobalLicense(List<License> globalLicenses, Connection conn) {

        String condition = globalLicenses.stream().map( lic -> {
            return "label_name='"+lic.getName()+"'";
        }).collect(Collectors.joining(" OR "));
        
        
        String query = "select * from labels_entity where  ("+condition+") AND label_group not like 'embedded%'";
        List<Triple<String, Integer, Integer>> notGroupLicenses =new JDBCQueryTemplate<Triple<String, Integer, Integer>>(conn, false) {
            @Override
            public boolean handleRow(ResultSet rs, List<Triple<String, Integer, Integer>> returnsList) throws SQLException {
                String labelName = rs.getString("label_name");
                Integer labelPriority = rs.getInt("label_priority");
                Integer labelId = rs.getInt("label_id");
                returnsList.add(Triple.of( labelName, labelId, labelPriority));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery(query);
        
        
        List<JDBCCommand> commands = new ArrayList<>();
        for (int i = 0; i < notGroupLicenses.size(); i++) {
            int labelId = notGroupLicenses.get(i).getMiddle();
            UpdateGroupCommand command = new UpdateGroupCommand(labelId, LicensesManager.GLOBAL_GROUP_NAME_EMBEDDED);
            commands.add(command);
        }
        
        
        if (commands.size() > 0) {
            try {
                new JDBCTransactionTemplate(conn, false).updateWithTransaction(commands);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }
    }
    
    //Automatic sort is disabled
    private static void sortByHint(List<License> globalLicenses, Connection conn) {
        globalLicenses.sort((left, right) -> {
            int leftHint = left.getPriorityHint();
            int rightHint = right.getPriorityHint();
            return Integer.compare(leftHint, rightHint);
        });
        
        
        String condition = globalLicenses.stream().map( lic -> {
            return "label_name='"+lic.getName()+"'";
        }).collect(Collectors.joining(" OR "));
        

        List<Triple<String, Integer, Integer>> realLicenses =new JDBCQueryTemplate<Triple<String, Integer, Integer>>(conn, false) {
            @Override
            public boolean handleRow(ResultSet rs, List<Triple<String, Integer, Integer>> returnsList) throws SQLException {
                String labelName = rs.getString("label_name");
                Integer labelPriority = rs.getInt("label_priority");
                Integer labelId = rs.getInt("label_id");
                returnsList.add(Triple.of( labelName, labelId, labelPriority));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery(String.format("select * from labels_entity where  %s order by label_priority asc", condition));
        
        
        List<JDBCCommand> commands = new ArrayList<>();
        
        
        if (realLicenses.size() == globalLicenses.size()) {
            
            for (int i = 0; i < globalLicenses.size(); i++) {
                License globalLicense = globalLicenses.get(i);
                String realLicenseName = realLicenses.get(i).getLeft();
                if (!globalLicense.getName().equals(realLicenseName)) {
                    int priority = realLicenses.get(i).getRight();
                    
                    Optional<Triple<String, Integer, Integer>> foundTripple = realLicenses.stream().filter(t-> {
                        return t.getLeft().equals(globalLicense.getName());
                    }).findAny();

                    
                    if (foundTripple.isPresent()) {
                        commands.add(new UpdatePriorityCommand(foundTripple.get().getMiddle(), foundTripple.get().getRight()));
                    }
                }
            }
            
            if (commands.size() > 0) {
                try {
                    new JDBCTransactionTemplate(conn, false).updateWithTransaction(commands);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
            }
        } else {
            // throw exception
        }

        
//        // real priorities
//        Map<String, License> realLienseMappings = new HashMap<>();
//        realLicenses.forEach(lic-> {realLienseMappings.put(lic.getName(), lic); });
//        
//        List<JDBCCommand> commands = new ArrayList<>();
//        for (int i = 0; i < globalLicenses.size(); i++) {
//            License globalLicense = globalLicenses.get(i);
//            int realPriority = realLicenses.get(i).getPriority();
//            License realLicense = realLienseMappings.get(globalLicense.getName());
//            commands.add(new  UpdatePriorityCommand(realLicense.getUpdatedPriorityLabel(realPriority)));
//        }
//
//        try {
//            new JDBCTransactionTemplate(conn, false).updateWithTransaction(commands);
//        } catch (SQLException e) {
//            throw new LicensesManagerException(e.getMessage(), e);
//        }
    
    }

    
    private static class UpdateGroupCommand extends JDBCCommand {

        private int  licenseid;
        private String group;
        
        public UpdateGroupCommand(int licenseId, String grp) {
            this.licenseid = licenseId;
            this.group = grp;
        }

        @Override
        public Object executeJDBCCommand(Connection con) throws SQLException {
            PreparedStatement prepareStatement = con
                    .prepareStatement("update labels_entity set label_group = ? where label_id = ? ");

            prepareStatement.setString(1, group);
            prepareStatement.setInt(2, licenseid);

            return prepareStatement.executeUpdate();
        }
    }

    private static class UpdatePriorityCommand extends JDBCCommand {

        private int  licenseid;
        private int priority;
        
        public UpdatePriorityCommand(int licenseId, int priority) {
            this.licenseid = licenseId;
            this.priority = priority;
        }

        @Override
        public Object executeJDBCCommand(Connection con) throws SQLException {
            PreparedStatement prepareStatement = con
                    .prepareStatement("update labels_entity set label_priority = ? where label_id = ? ");

            if (licenseid == -1) {
                prepareStatement.setNull(1, Types.INTEGER);
            } else {
                prepareStatement.setInt(1, priority);
            }
            prepareStatement.setInt(2, licenseid);

            return prepareStatement.executeUpdate();
        }
    }


    private static int insertGenerateContent(Connection connection) throws SQLException {
        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertRight_generate_content").toString();
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        return template.executeUpdate(sql);
    }


//    /**
//     * @param connection
//     * @return
//     * @throws SQLException
//     */
//    private static int insertAggregateRight(Connection connection) throws SQLException {
//        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertRight_aggregate").toString();
//        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
//        return template.executeUpdate(sql);
//    }
//
//    /**
//     * @param connection
//     * @return
//     * @throws SQLException
//     */
//    private static int insertShowStatiticsRight(Connection connection) throws SQLException {
//        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertRight_showstatistics").toString();
//        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
//        return template.executeUpdate(sql);
//    }
//

//    private static int insertSortRight(Connection connection) throws SQLException {
//        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertRight_sort").toString();
//        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
//        return template.executeUpdate(sql);
//    }

//    private static int insertPrintRight(Connection connection) throws SQLException {
//        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertRight_show_print_menu").toString();
//        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
//        return template.executeUpdate(sql);
//    }

    private static int updateUserEntityTable(Connection connection) throws SQLException {
        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("updateUserEntities").toString();
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        template.setUseReturningKeys(false);
        return template.executeUpdate(sql);
    }

    private static int updateShowItems(Connection connection) throws SQLException {
        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("updateShowItems").toString();
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        template.setUseReturningKeys(false);
        return template.executeUpdate(sql);
    }
    



//    /**
//     * @param connection
//     * @return
//     * @throws SQLException
//     */
//    private static int insertReplikatorK3(Connection connection) throws SQLException {
//        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertRight_ReplikatorK3").toString();
//        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
//        return template.executeUpdate(sql);
//    }
//    private static int insertRightK4ReplicationExport(Connection connection) throws SQLException {
//        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertRight_K4ReplicationExport").toString();
//        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
//        return template.executeUpdate(sql);
//    }
//
//    private static int insertRightK4ReplicationImport(Connection connection) throws SQLException {
//        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertRight_K4ReplicationImport").toString();
//        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
//        return template.executeUpdate(sql);
//    }

//    private static int insertRightForCriteriaParamsManage(Connection connection) throws SQLException {
//        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertRight_DisplayAdminMenu").toString();
//        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
//        return template.executeUpdate(sql);
//    }
//    private static int insertNDKMetsImport(Connection connection) throws SQLException {
//        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertRight_NDKMetsImport").toString();
//        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
//        return template.executeUpdate(sql);
//    }

    private static void makeSureThatProfilesTable(Connection connection) throws SQLException, IOException {
        if (!DatabaseUtils.tableExists(connection, "PROFILES")) {
            // create tables for public users - 4.5.0 - version
            createPublicUsersAndProfilesTables(connection); // Zavislost na active users
        }
    }

    private static void makeSureThatLabelsTable(Connection connection) throws SQLException, IOException {
        if (!DatabaseUtils.tableExists(connection, "LABELS_ENTITY")) {
            createLabelsTable(connection);
        }
    }

    
    private static void makeSureThatLicensesTableContainsExclusiveLockColumns(Connection connection )  throws SQLException, IOException {
        if (!DatabaseUtils.columnExists(connection, "LABELS_ENTITY", "LOCK")) {
            alterTableAddExclusiveLock(connection);
            alterTableAddExclusiveLockMaxReaders(connection);
            alterTableAddExclusiveLockRefreshInterval(connection);
            alterTableAddExclusiveLockMaxInterval(connection);
            alterTableAddExclusiveLockType(connection);
        }
        
        if (!DatabaseUtils.columnExists(connection, "LABELS_ENTITY", "LOCK_TYPE")) {
            alterTableAddExclusiveLockType(connection);
        }
    }
    

    public static void alterTableAddExclusiveLockMaxReaders(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement("ALTER TABLE LABELS_ENTITY ADD COLUMN LOCK_MAXREADERS INTEGER");
        prepareStatement.executeUpdate();
        LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows");
    }

    public static void alterTableAddExclusiveLockRefreshInterval(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement("ALTER TABLE LABELS_ENTITY ADD COLUMN LOCK_REFRESHINTERVAL INTEGER");
        prepareStatement.executeUpdate();
        LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows");
    }

    public static void alterTableAddExclusiveLockMaxInterval(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement("ALTER TABLE LABELS_ENTITY ADD COLUMN LOCK_MAXINTERVAL INTEGER");
        prepareStatement.executeUpdate();
        LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows");
    }

    public static void alterTableAddExclusiveLockType(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement("ALTER TABLE LABELS_ENTITY ADD COLUMN LOCK_TYPE VARCHAR(255)");
        prepareStatement.executeUpdate();
        LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows");
    }
    
    private static void makeSurePublicRoleInserted(Connection conn) throws SQLException {
        List<Integer> result = new JDBCQueryTemplate<Integer>(conn, false) {

            @Override
            public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                returnsList.add(rs.getInt("group_id"));
                return true;
            }

        }.executeQuery("select * from group_entity where gname = ?", "public_users");

        if (result.isEmpty()) {
            // create public role
            insertPublicRole(conn);
        }
    }

    private static void makeSureThatUserEntity_DEACTIVATED(Connection connection) throws SQLException {
        // nesmi byt v transakci ... ?
        if (!DatabaseUtils.columnExists(connection, "USER_ENTITY", "DEACTIVATED")) {

            new JDBCTransactionTemplate(connection, false).updateWithTransaction(
                    new JDBCCommand() {

                        @Override
                        public Object executeJDBCCommand(Connection con) throws SQLException {
                            alterSecurityTableActiveColumn(con);
                            return null;
                        }
                    },
                    new JDBCCommand() {

                        @Override
                        public Object executeJDBCCommand(Connection con) throws SQLException {
                            updateSecurityTableActiveColumn(con);
                            return null;
                        }
                    }
            );
        }
    }

    private static int insertParams(Connection connection) throws SQLException {
        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertParams_SecuredStreams").toString();
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        return template.executeUpdate(sql);
    }

    private static int insertPublicRole(Connection connection) throws SQLException {
        StringTemplate stemplate = SecurityDatabaseUtils.stGroup().getInstanceOf("insertPublicRole");
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        return template.executeUpdate(stemplate.toString());
    }

//    private static int insertRightForDisplayAdminMenu(Connection connection) throws SQLException {
//        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertDisplayAdminMenuRight").toString();
//        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
//        return template.executeUpdate(sql);
//    }

//    public static int insertRightForVirtualCollection(Connection connection) throws SQLException {
//        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertRight_VirtualCollections").toString();
//        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
//        return template.executeUpdate(sql);
//    }


    private static int insertCriterium(Connection connection, int paramid) throws SQLException {
        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertCriterium_SecuredStreams").toString();
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        return template.executeUpdate(sql, paramid);
    }

    private static int insertRight(Connection connection, int groupId, int criteriumid) throws SQLException {
        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertRight_SecuredStreams").toString();
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        return template.executeUpdate(sql, criteriumid, groupId);
    }

    private static void createSecurityTables(Connection connection) throws SQLException, IOException {
        InputStream is = InitSecurityDatabaseMethodInterceptor.class.getResourceAsStream("res/initsecdb.sql");
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        template.setUseReturningKeys(false);
        template.executeUpdate(IOUtils.readAsString(is, Charset.forName("UTF-8"), true));
    }

    public static void makeSureAuthenticatedUsers(Connection connection) throws SQLException, IOException {
        List<String> roleNames = new JDBCQueryTemplate<String>(connection, false) {
            public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                return rs.getString("gname") != null;
            }
        }.executeQuery("select gname from group_entity where gname='authenticated_users'");

        if (roleNames.isEmpty()) {
            insertAuthenticatedUserRole( connection);
        }
    }

    private static void insertAuthenticatedUserRole( Connection connection) throws SQLException, IOException {
        InputStream is = InitSecurityDatabaseMethodInterceptor.class.getResourceAsStream("res/initauthenticatedusersdb.sql");
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        template.setUseReturningKeys(false);
        template.executeUpdate(IOUtils.readAsString(is, Charset.forName("UTF-8"), true));
    }

    /** Premena na nove akce */
    // vytvareni 
    private static void createNewActions(Connection connection) throws SQLException, IOException {
        InputStream is = InitSecurityDatabaseMethodInterceptor.class.getResourceAsStream("res/initsecdb_newactions.sql");
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        template.setUseReturningKeys(false);
        template.executeUpdate(IOUtils.readAsString(is, Charset.forName("UTF-8"), true));
    }
    

    private static void updateExistingActions(Connection connection) throws SQLException, IOException {
        InputStream is = InitSecurityDatabaseMethodInterceptor.class.getResourceAsStream("res/rename_oldactions.sql");
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        template.setUseReturningKeys(false);
        template.executeUpdate(IOUtils.readAsString(is, Charset.forName("UTF-8"), true));
    }

    public static void createPublicUsersAndProfilesTables(Connection connection) throws SQLException, IOException {
        InputStream is = SecurityDbInitializer.class.getResourceAsStream("res/initpublicusers.sql");
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        template.setUseReturningKeys(false);
        String sqlScript = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
        template.executeUpdate(sqlScript);
    }

    public static void createLabelsTable(Connection connection) throws SQLException, IOException {
        InputStream is = InitSecurityDatabaseMethodInterceptor.class.getResourceAsStream("res/initlabels.sql");
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        template.setUseReturningKeys(false);
        template.executeUpdate(IOUtils.readAsString(is, Charset.forName("UTF-8"), true));
    }

    public static void alterSecurityTableActiveColumn(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement("ALTER TABLE USER_ENTITY ADD COLUMN DEACTIVATED BOOLEAN");
        prepareStatement.executeUpdate();
        LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows");
    }

    public static void updateSecurityTableActiveColumn(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement("UPDATE USER_ENTITY set DEACTIVATED = FALSE");
        int r = prepareStatement.executeUpdate();
        LOGGER.log(Level.FINEST, "UPDATE TABLE: updated rows {0}", r);
    }



    public static void alterTableAddRoleColumn(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement("ALTER TABLE right_entity ADD COLUMN ROLE TEXT");
        prepareStatement.executeUpdate();
        LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows");
    }

    public static void updateRolesFromRelations(Connection con) throws SQLException {
        String update = "UPDATE right_entity " +
                "SET \"role\"=subquery.gname " +
                "FROM (SELECT group_id, gname " +
                "      FROM  group_entity) AS subquery " +
                "WHERE (right_entity.group_id=subquery.group_id) and (right_entity.role is null) and (right_entity.group_id is not null)";
        PreparedStatement prepareStatement = con.prepareStatement(update);
        int i = prepareStatement.executeUpdate();
        LOGGER.log(Level.FINEST, "UPDATE TABLE: updated rows "+i);

    }

    private static void makeSureThatRoleColumnExists(Connection connection) throws SQLException {
        if (!DatabaseUtils.columnExists(connection, "right_entity", "role")) {
            new JDBCTransactionTemplate(connection, false).updateWithTransaction(
                    new JDBCCommand() {

                        @Override
                        public Object executeJDBCCommand(Connection con) throws SQLException {
                            alterTableAddRoleColumn(con);
                            return null;
                        }
                    }
            );
        }
    }


    private static void makeSureThatRuntimeColumnsExists(Connection connection) throws SQLException {
        List<JDBCCommand> commands = new ArrayList<>();

        if (!DatabaseUtils.columnExists(connection, "labels_entity", "RUNTIME")) {
            commands.add(new JDBCCommand() {
                @Override
                public Object executeJDBCCommand(Connection con) throws SQLException {
                    PreparedStatement prepareStatement = con.prepareStatement("ALTER TABLE LABELS_ENTITY ADD COLUMN RUNTIME BOOLEAN");
                    prepareStatement.executeUpdate();
                    LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows");

                    return null;
                }
            });
        }
        if (!DatabaseUtils.columnExists(connection, "labels_entity", "RUNTIME_TYPE")) {
            commands.add(new JDBCCommand() {
                @Override
                public Object executeJDBCCommand(Connection con) throws SQLException {

                    PreparedStatement prepareStatement = con.prepareStatement("ALTER TABLE LABELS_ENTITY ADD COLUMN RUNTIME_TYPE TEXT");
                    prepareStatement.executeUpdate();
                    LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows");

                    return null;
                }
            });
        }

        new JDBCTransactionTemplate(connection, false).updateWithTransaction(commands);
    }

    public static void alterTableAddExclusiveLock(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement("ALTER TABLE LABELS_ENTITY ADD COLUMN LOCK BOOLEAN");
        prepareStatement.executeUpdate();
        LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows");
    }


    public static void makeSureRolesInTable(Connection connection) throws SQLException {
        new JDBCTransactionTemplate(connection, false).updateWithTransaction(
                new JDBCCommand() {

                    @Override
                    public Object executeJDBCCommand(Connection con) throws SQLException {
                        updateRolesFromRelations(con);
                        return null;
                    }
                }
        );
    }
}
