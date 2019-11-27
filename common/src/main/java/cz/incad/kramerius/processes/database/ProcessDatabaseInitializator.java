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
package cz.incad.kramerius.processes.database;

import static cz.incad.kramerius.database.cond.ConditionsInterpretHelper.versionCondition;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import cz.incad.kramerius.database.VersionService;
import cz.incad.kramerius.security.database.InitSecurityDatabaseMethodInterceptor;
import cz.incad.kramerius.users.database.LoggedUserDatabaseInitializator;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.IOUtils;

/**
 * Database initialization - processes table
 * @author pavels
 */
public class ProcessDatabaseInitializator {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ProcessDatabaseInitializator.class.getName());
    
    public static void initDatabase(Connection connection, VersionService versionService) {
        try {
            String v = versionService.getVersion();
            if (v == null) {
                nullVersionInitialization(connection);
            } else if (v.equals("4.5.0") ||  v.equals("4.6.0") ||  v.equals("4.7.0") || v.equals("4.8.0") ||  v.equals("4.9.0")) {
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","PARAMS_MAPPING")) {
                    alterProcessTableParamsMappingToken(connection);
                }
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","BATCH_STATUS")) {
                    alterProcessTableBatchState(connection);
                    updateProcessTableBatchStates(connection);
                }
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","FINISHED")) {
                    alterProcessTableFinished(connection);
                }
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","TOKEN_ACTIVE")) {
                    alterProcessTableTokenActive(connection);
                }
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","AUTH_TOKEN")) {
                    alterProcessTableAuthToken(connection);
                    alterProcessTableProcess2TokenAuthToken(connection);
                }
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","IP_ADDR")) {
                    alterProcessTableIPADDR(connection);
                }
            } else if ((v.equals("5.0.0")) || (v.equals("5.1.0")))  {
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","BATCH_STATUS")) {
                    alterProcessTableBatchState(connection);
                    updateProcessTableBatchStates(connection);
                }
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","FINISHED")) {
                    alterProcessTableFinished(connection);
                }
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","TOKEN_ACTIVE")) {
                    alterProcessTableTokenActive(connection);
                }
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","AUTH_TOKEN")) {
                    alterProcessTableAuthToken(connection);
                    alterProcessTableProcess2TokenAuthToken(connection);
                }
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","IP_ADDR")) {
                    alterProcessTableIPADDR(connection);
                }
            } else if (v.equals("5.1.0"))  {
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","FINISHED")) {
                    alterProcessTableFinished(connection);
                }
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","TOKEN_ACTIVE")) {
                    alterProcessTableTokenActive(connection);
                }
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","AUTH_TOKEN")) {
                    alterProcessTableAuthToken(connection);
                    alterProcessTableProcess2TokenAuthToken(connection);
                }
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","IP_ADDR")) {
                    alterProcessTableIPADDR(connection);
                }
            } else if (v.equals("5.2.0"))  {
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","TOKEN_ACTIVE")) {
                    alterProcessTableTokenActive(connection);
                }
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","AUTH_TOKEN")) {
                    alterProcessTableAuthToken(connection);
                    alterProcessTableProcess2TokenAuthToken(connection);
                }
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","IP_ADDR")) {
                    alterProcessTableIPADDR(connection);
                }
            } else if (v.equals("5.3.0"))  {
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","AUTH_TOKEN")) {
                    alterProcessTableAuthToken(connection);
                    alterProcessTableProcess2TokenAuthToken(connection);
                }
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","IP_ADDR")) {
                    alterProcessTableIPADDR(connection);
                }
            } else if (versionCondition(v, ">", "5.3.0"))  {
                if (!DatabaseUtils.columnExists(connection, "PROCESSES","IP_ADDR")) {
                    alterProcessTableIPADDR(connection);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    private static void alterProcessTableIPADDR(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "ALTER TABLE PROCESSES ADD COLUMN IP_ADDR VARCHAR(255);");
            try {
                int r = prepareStatement.executeUpdate();
                LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows {0}", r);
            } finally {
                DatabaseUtils.tryClose(prepareStatement);
            }
    }

    /** No version defined in db */
    public static void nullVersionInitialization(Connection connection) throws SQLException, IOException {
        if (!DatabaseUtils.tableExists(connection,"PROCESSES")) {
            createProcessTable(connection);
        }
        
        if (!DatabaseUtils.columnExists(connection, "PROCESSES", "STARTEDBY")) {
            alterProcessTableStartedByColumn(connection);
        }
        
        if (!DatabaseUtils.columnExists(connection, "PROCESSES", "TOKEN")) {
            alterProcessTableProcessToken(connection);
        }
        
        if (!DatabaseUtils.columnExists(connection, "PROCESSES", "PROCESS_ID")) {
            changeDatabaseBecauseShibb(connection);
        }
        
        
        if (!DatabaseUtils.tableExists(connection, "PROCESS_2_TOKEN")) {
            createToken2SessionkeysMapping(connection); // zavislost na session_keys
        }

        if (!DatabaseUtils.columnExists(connection, "PROCESSES","PARAMS_MAPPING")) {
            alterProcessTableParamsMappingToken(connection);
        }
        if (!DatabaseUtils.columnExists(connection, "PROCESSES","BATCH_STATUS")) {
            alterProcessTableBatchState(connection);
            updateProcessTableBatchStates(connection);
        }
        if (!DatabaseUtils.columnExists(connection, "PROCESSES","FINISHED")) {
            alterProcessTableFinished(connection);
        }
        if (!DatabaseUtils.columnExists(connection, "PROCESSES","TOKEN_ACTIVE")) {
            alterProcessTableTokenActive(connection);
        }
        if (!DatabaseUtils.columnExists(connection, "PROCESSES","AUTH_TOKEN")) {
            alterProcessTableAuthToken(connection);
            alterProcessTableProcess2TokenAuthToken(connection);
        }
        if (!DatabaseUtils.columnExists(connection, "PROCESSES","IP_ADDR")) {
            alterProcessTableIPADDR(connection);
        }
    }

    public static void createToken2SessionkeysMapping(Connection connection) throws SQLException, IOException {
        InputStream is = ProcessDatabaseInitializator.class.getResourceAsStream("res/initprocesstoken.sql");
        String sqlScript = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
        PreparedStatement prepareStatement = connection.prepareStatement(sqlScript);
        int r = prepareStatement.executeUpdate();
        LOGGER.log(Level.FINEST, "CREATE TABLE: updated rows {0}", r);
    }

    
    public static void createProcessTable(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "CREATE TABLE PROCESSES(DEFID VARCHAR(255), " +
            		"UUID VARCHAR(255) PRIMARY KEY," +
            		"PID int,STARTED timestamp, " +
            		"PLANNED timestamp, " +
            		"STATUS int, " +
            		"NAME VARCHAR(1024), " +
            		"PARAMS VARCHAR(4096), "+
            		"STARTEDBY INT)"); 
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "CREATE TABLE: updated rows {0}", r);
    }

    public static void alterProcessTableStartedByColumn(Connection con) throws SQLException {
        
        PreparedStatement prepareStatement = con.prepareStatement(
                "ALTER TABLE PROCESSES ADD COLUMN STARTEDBY INT");
            try {
                int r = prepareStatement.executeUpdate();
                LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows {0}", r);
            } finally {
                DatabaseUtils.tryClose(prepareStatement);
            }
    }

    public static void alterProcessTableProcessToken(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "ALTER TABLE PROCESSES ADD COLUMN TOKEN VARCHAR(255)");
        try {
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows {0}", r);
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }
    
    public static void alterProcessTableParamsMappingToken(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
            "ALTER TABLE PROCESSES ADD COLUMN PARAMS_MAPPING VARCHAR(4096);");
        try {
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows {0}", r);
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }    


    public static void alterProcessTableTokenActive(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
            "ALTER TABLE PROCESSES ADD COLUMN TOKEN_ACTIVE BOOLEAN;");
        try {
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows {0}", r);
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }    

    public static void alterProcessTableProcess2TokenAuthToken(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
            "ALTER TABLE PROCESS_2_TOKEN RENAME COLUMN TOKEN TO AUTH_TOKEN;");
        try {
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows {0}", r);
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }    
    
    public static void alterProcessTableAuthToken(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
            "ALTER TABLE PROCESSES ADD COLUMN AUTH_TOKEN VARCHAR(255);");
        try {
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows {0}", r);
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }    


    public static void alterProcessTableBatchState(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
            "ALTER TABLE PROCESSES ADD COLUMN BATCH_STATUS INTEGER;");
        try {
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows {0}", r);
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }    

    public static void alterProcessTableFinished(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
            "ALTER TABLE PROCESSES ADD COLUMN FINISHED TIMESTAMP;");
        try {
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows {0}", r);
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }    

    public static void  updateProcessTableBatchStates(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
            "update processes set batch_status = case "+
                "   when status = 6 then 1"+
                "   when status = 7 then 2"+
                "   when status = 8 then 3"+
                "   else 0"+
                "   end");
        try {
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "UPDATE TABLE: updated rows {0}", r);
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }

    
    public static void changeDatabaseBecauseShibb(Connection con) throws SQLException {
        boolean autocommit = con.getAutoCommit();
        con.setAutoCommit(false);
        PreparedStatement alterProcessIdPS=null,createUniqueIndexPS=null,createSequencePS=null,updateProcessPS=null,createViewPS=null,
        loginnamePS=null,firstnamePS=null,surnamePS=null,
        usernamekeyPS=null, updateNamesPS=null;
        
        
        
        try {
            // UPDATE PROCESS ID
            alterProcessIdPS = con.prepareStatement("ALTER TABLE PROCESSES ADD COLUMN PROCESS_ID INTEGER");
            int r = alterProcessIdPS.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE PROCESSES: updated rows {0}", r);
            // UNIQUE INDEX ON  PROCESS ID
            createUniqueIndexPS = con.prepareStatement("CREATE UNIQUE INDEX PROCESSES_IDX ON PROCESSES (PROCESS_ID)");
            r = createUniqueIndexPS.executeUpdate();
            LOGGER.log(Level.FINEST, "CREATE UNIQUE ON PROCESS ID: updated rows {0}", r);
            // SEQUENCE INCREMENTS PROCESS ID
            createSequencePS = con.prepareStatement("CREATE SEQUENCE PROCESS_ID_SEQUENCE INCREMENT BY 1 START WITH 1 MINVALUE 0;");
            r = createSequencePS.executeUpdate();
            LOGGER.log(Level.FINEST, "CREATE SEQUENCE: updated rows {0}", r);
            // UPDATE ALL PROCESS WHICH HAS NO PROCESS ID
            updateProcessPS = con.prepareStatement("UPDATE PROCESSES SET PROCESS_ID=nextval('PROCESS_ID_SEQUENCE') WHERE PROCESS_ID IS NULL");
            r = updateProcessPS.executeUpdate();
            LOGGER.log(Level.FINEST, "UPDATE PROCESSES: updated rows {0}", r);
            // CREATE VIEW WHICH POINTS MAIN PROCESSSES
            createViewPS = con.prepareStatement("CREATE VIEW PROCESS_GROUPED_VIEW as " +
            		"select min(process_id) as process_id, count(*) as pcount from processes group by token");
            r = createViewPS.executeUpdate();
            LOGGER.log(Level.FINEST, "CREATE VIEW: updated rows {0}", r);

            //loginnamePS=null,firstname=null,surname=null,

            // user names
            loginnamePS = con.prepareStatement("ALTER TABLE PROCESSES  " +
                    " ADD COLUMN loginname VARCHAR(1024)");
            r = loginnamePS.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE loginname: updated rows {0}", r);

            firstnamePS = con.prepareStatement("ALTER TABLE PROCESSES  " +
            " ADD COLUMN firstname VARCHAR(1024)");
            r = firstnamePS.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE firstname: updated rows {0}", r);

            surnamePS = con.prepareStatement("ALTER TABLE PROCESSES  " +
            " ADD COLUMN surname VARCHAR(1024)");
            r = surnamePS.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE surname: updated rows {0}", r);

            
            // ADD USER_KEY COLUMN
            usernamekeyPS = con.prepareStatement("ALTER TABLE PROCESSES  " +
            		" ADD COLUMN USER_KEY VARCHAR(255)");
            r = usernamekeyPS.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE NAMES: updated rows {0}", r);

            

            updateNamesPS = con.prepareStatement(                    
                    " update processes p set " +
                    "   surname=(select surname from user_entity where user_id= p.startedby), " +
                    "   loginname=(select loginname from user_entity where user_id= p.startedby), " +
                    "   firstname=(select \"name\" from user_entity where user_id= p.startedby)"
            );
            r = updateNamesPS.executeUpdate();
            LOGGER.log(Level.FINEST, "UPDATE TABLE: updated rows {0}", r);
            
            
            con.commit();

        } catch(SQLException se) {
            con.rollback();
            throw se;
        } finally {
            con.setAutoCommit(autocommit);
            
            DatabaseUtils.tryClose(alterProcessIdPS);
            DatabaseUtils.tryClose(createUniqueIndexPS);
            DatabaseUtils.tryClose(createSequencePS);
            DatabaseUtils.tryClose(updateProcessPS);
            DatabaseUtils.tryClose(createViewPS);
            DatabaseUtils.tryClose(usernamekeyPS);
            DatabaseUtils.tryClose(updateNamesPS);

            DatabaseUtils.tryClose(loginnamePS);
            DatabaseUtils.tryClose(firstnamePS);
            DatabaseUtils.tryClose(surnamePS);
            
        }
    }
}
