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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import cz.incad.kramerius.database.VersionService;
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
                
                // create one rule 
                /*
                new JDBCTransactionTemplate(connection, false).updateWithTransaction(
                        new JDBCCommand() {
                            
                            @Override
                            public Object executeJDBCCommand(Connection con) throws SQLException {
                                return insertParams(con);
                            }

                        },
                        new JDBCCommand() {
                            
                            @Override
                            public Object executeJDBCCommand(Connection con) throws SQLException {
                                return insertCriterium(con, (Integer)getPreviousResult());
                            }
                        },
                        new JDBCCommand() {
                            
                            @Override
                            public Object executeJDBCCommand(Connection con) throws SQLException {
                                String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("select_common_role").toString();

                                List<Integer> ids = new JDBCQueryTemplate<Integer>(con, false){

                                    @Override
                                    public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                                        returnsList.add(rs.getInt("group_id"));
                                        return false;
                                    }
                                }.executeQuery(sql);

                                if (ids.isEmpty()) throw new SQLException("cannot find common group entity !");
                                return insertRight(con, ids.get(0), (Integer)getPreviousResult());
                            }
                        }

                );*/ 

                
                
            } else { /* already created */ }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    public static int insertParams(Connection connection) throws SQLException {
        String sql = SecurityDatabaseUtils.stUdateRightGroup().getInstanceOf("insertParams_SecuredStreams").toString();
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
        template.executeUpdate(IOUtils.readAsString(is, Charset.forName("UTF-8"), true));
    }
    
    
    public static void alterSecurityTableActiveColumn(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "ALTER TABLE USER_ENTITY ADD COLUMN DEACTIVATED BOOLEAN"); 
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows {0}", r);
    }

    public static void updateSecurityTableActiveColumn(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "UPDATE USER_ENTITY set DEACTIVATED = FALSE"); 
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "UPDATE TABLE: updated rows {0}", r);
    }

}
