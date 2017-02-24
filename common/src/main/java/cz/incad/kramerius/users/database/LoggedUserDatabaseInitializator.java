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
package cz.incad.kramerius.users.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import cz.incad.kramerius.database.VersionService;
import cz.incad.kramerius.security.database.InitSecurityDatabaseMethodInterceptor;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

public class LoggedUserDatabaseInitializator {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(LoggedUserDatabaseInitializator.class.getName());
    
    public static StringTemplateGroup stGroup;
    static {
        InputStream is = LoggedUserDatabaseInitializator.class.getResourceAsStream("res/database.stg");
        stGroup = new StringTemplateGroup(new InputStreamReader(is), DefaultTemplateLexer.class);
    }

    
    public static void initDatabase(final Connection connection, VersionService versionService) {
        try {
            if (versionService.getVersion() == null) {
                createLoggedUsersTablesIfNotExists(connection);
            } else {/* no version */}
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }



    public static void deleteAllSessionKeys(final Connection connection) throws SQLException, IOException {
        JDBCTransactionTemplate transaction = new JDBCTransactionTemplate(connection, false);
        
        // delete assications
        JDBCCommand deleteAssociation = new JDBCCommand() {
            
            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                StringTemplate sql = stGroup.getInstanceOf("deleteAllAssociationOfSessionKeys");
                PreparedStatement prepareStatement = connection.prepareStatement( sql.toString());
                int r = prepareStatement.executeUpdate();
                LOGGER.log(Level.FINEST, "DELETED TABLE ASSOCIATION OF SESSION_KEYS: deleted rows {0}", r);
                return null;
            }
        };
        
        // delete keys in session keys
        JDBCCommand deleteKeys = new JDBCCommand() {
            
            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                StringTemplate sql = stGroup.getInstanceOf("deleteAllSessionKeys");
                PreparedStatement prepareStatement = connection.prepareStatement( sql.toString());
                int r = prepareStatement.executeUpdate();
                LOGGER.log(Level.FINEST, "DELETED TABLE SESSION_KEYS: deleted rows {0}", r);
                return null;
            }
        };
        
        transaction.updateWithTransaction(deleteAssociation, deleteKeys);
    }
    

    public static void createLoggedUsersTablesIfNotExists(final Connection connection) throws SQLException, IOException {
            boolean loggedUserTable = DatabaseUtils.tableExists(connection, "ACTIVE_USERS");
            if (!loggedUserTable) {
                createLoggedUsersTables(connection);
            }
    }

    

    
    public static void createLoggedUsersTables(Connection connection) throws SQLException, IOException {
        InputStream is = LoggedUserDatabaseInitializator.class.getResourceAsStream("res/initloggedusers.sql");
        String sqlScript = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
        PreparedStatement prepareStatement = connection.prepareStatement(
        sqlScript);
        int r = prepareStatement.executeUpdate();
        LOGGER.log(Level.FINEST, "CREATE TABLE: updated rows {0}", r);
    }

}
