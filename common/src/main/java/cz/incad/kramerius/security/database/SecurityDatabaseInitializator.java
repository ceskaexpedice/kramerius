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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import org.antlr.stringtemplate.StringTemplate;

import cz.incad.kramerius.database.VersionService;
import cz.incad.kramerius.users.database.LoggedUserDatabaseInitializator;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

public class SecurityDatabaseInitializator {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(SecurityDatabaseInitializator.class.getName());

    public static void initDatabase(Connection connection, VersionService versionService) {
        try {
            if (versionService.getVersion()  == null) {
                
                if (!DatabaseUtils.tableExists(connection, "USER_ENTITY")) {
                    createSecurityTables(connection);
                }

                userEntity_DEACTIVATED(connection);
                
                //TODO: Move method
                LoggedUserDatabaseInitializator.createLoggedUsersTablesIfNotExists(connection);
                
                // create tables for public users - 4.5.0 - version
                createPublicUsersAndProfilesTables(connection); // Zavislost na active users
                // create public role
                insertPublicRole(connection);
                // create public role
                insertRightForDisplayAdminMenu(connection);

                // insert right for virtual collection manage
                insertRightForVirtualCollection(connection);

                
            } else { 
                
                if (versionService.getVersion().equals("4.5.0")) {

                    userEntity_DEACTIVATED(connection);

                    //TODO: Move method
                    LoggedUserDatabaseInitializator.createLoggedUsersTablesIfNotExists(connection);

                    // create tables for public users
                    createPublicUsersAndProfilesTables(connection);
                    // create public role
                    insertPublicRole(connection);
                    // create public role
                    insertRightForDisplayAdminMenu(connection);

                    insertRightForVirtualCollection(connection);

                }
                
                if (versionService.getVersion().equals("4.6.0")) {

                    userEntity_DEACTIVATED(connection);

                    // create public role
                    insertPublicRole(connection);
                    // create public role
                    insertRightForDisplayAdminMenu(connection);

                    // insert right for virtual collection manage
                    insertRightForVirtualCollection(connection);

                }

                if (versionService.getVersion().equals("4.7.0")) {

                    userEntity_DEACTIVATED(connection);

                    // create public role
                    insertRightForDisplayAdminMenu(connection);

                    // insert right for virtual collection manage
                    insertRightForVirtualCollection(connection);
                }
                
                if (versionService.getVersion().equals("4.8.0")) {

                    userEntity_DEACTIVATED(connection);

                    
                    // insert right for virtual collection manage
                    insertRightForVirtualCollection(connection);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    public static void userEntity_DEACTIVATED(Connection connection) throws SQLException {
        if (!DatabaseUtils.columnExists(connection, "USER_ENTITY","DEACTIVATED")) {
            
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

    public static int insertParams(Connection connection) throws SQLException {
        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertParams_SecuredStreams").toString();
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection,false);
        return template.executeUpdate(sql);
    }

    public static int insertPublicRole(Connection connection) throws SQLException {
        StringTemplate stemplate = SecurityDatabaseUtils.stGroup().getInstanceOf("insertPublicRole");
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection,false);
        return template.executeUpdate(stemplate.toString());
    }
    
    public static int insertRightForDisplayAdminMenu(Connection connection) throws SQLException {
        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertDisplayAdminMenuRight").toString();
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection,false);
        return template.executeUpdate(sql);
    }

    public static int insertRightForVirtualCollection(Connection connection) throws SQLException {
        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertRight_VirtualCollections").toString();
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection,false);
        return template.executeUpdate(sql);
    }

    
    public static int insertCriterium(Connection connection, int paramid) throws SQLException {
        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertCriterium_SecuredStreams").toString();
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection,false);
        return template.executeUpdate(sql, paramid);
    }

    public static int insertRight(Connection connection, int groupId,int criteriumid) throws SQLException {
        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertRight_SecuredStreams").toString();
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection,false);
        return template.executeUpdate(sql, criteriumid, groupId);
    }

    public static void createSecurityTables(Connection connection) throws SQLException, IOException {
        InputStream is = InitSecurityDatabaseMethodInterceptor.class.getResourceAsStream("res/initsecdb.sql");
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        template.setUseReturningKeys(false);
        template.executeUpdate(IOUtils.readAsString(is, Charset.forName("UTF-8"), true));
    }
    
    public static void createPublicUsersAndProfilesTables(Connection connection) throws SQLException, IOException {
        InputStream is = SecurityDatabaseInitializator.class.getResourceAsStream("res/initpublicusers.sql");
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        template.setUseReturningKeys(false);
        String sqlScript = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
        template.executeUpdate(sqlScript);
    }
    
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
//        InputStream is = SecurityDatabaseInitializator.class.getResourceAsStream("res/initpublicusers.sql");
//        String sqlScript = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
//        Class<?> clz = Class.forName("org.postgresql.Driver");
//        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost/kramerius4","fedoraAdmin","fedoraAdmin");
//
//        StringReader reader = new StringReader(sqlScript);
//        BufferedReader bufReader = new BufferedReader(reader);
//        String line = null;
//        while((line = bufReader.readLine())!=null) {
//            System.out.println(line +" = "+line.length());
//            
//        }
//
//        createPublicUsersAndProfilesTables(conn);
//        String str = "ALTER TABLE PROFILES ADD CONSTRAINT PROFILES_ACTIVE_USER_ID_FK FOREIGN KEY (active_users_id) REFERENCES ACTIVE_USERS (ACTIVE_USERS_ID);";
//        System.out.println(str.substring(0,135));
    }


    
    public static void alterSecurityTableActiveColumn(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "ALTER TABLE USER_ENTITY ADD COLUMN DEACTIVATED BOOLEAN"); 
            prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows");
    }

    public static void updateSecurityTableActiveColumn(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "UPDATE USER_ENTITY set DEACTIVATED = FALSE"); 
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "UPDATE TABLE: updated rows {0}", r);
    }

}
