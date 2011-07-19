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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import cz.incad.kramerius.security.database.InitSecurityDatabaseMethodInterceptor;
import cz.incad.kramerius.utils.DatabaseUtils;

/**
 * Database initialization - processes table
 * @author pavels
 */
public class ProcessDatabaseInitializator {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ProcessDatabaseInitializator.class.getName());
    
    public static void initDatabase(Connection connection) {
        try {
            if (!DatabaseUtils.tableExists(connection,"PROCESSES")) {
                createProcessTable(connection);
            }
            if (!DatabaseUtils.columnExists(connection, "PROCESSES", "STARTEDBY")) {
                alterProcessTableStartedByColumn(connection);
            }
            if (!DatabaseUtils.columnExists(connection, "PROCESSES", "TOKEN")) {
                alterProcessTableProcessToken(connection);
            }
            /*
            if (!DatabaseUtils.tableExists(connection,"USER_ENTITY")) {
                InitSecurityDatabaseMethodInterceptor.createSecurityTables(connection);
            }
            */
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }

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
        try {
            int r = prepareStatement.executeUpdate();
            InitProcessDatabaseMethodInterceptor.LOGGER.log(Level.FINEST, "CREATE TABLE: updated rows {0}", r);
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }

    public static void alterProcessTableStartedByColumn(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "ALTER TABLE PROCESSES ADD COLUMN STARTEDBY INT");
        try {
            int r = prepareStatement.executeUpdate();
            InitProcessDatabaseMethodInterceptor.LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows {0}", r);
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }

    public static void alterProcessTableProcessToken(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "ALTER TABLE PROCESSES ADD COLUMN TOKEN VARCHAR(255)");
        try {
            int r = prepareStatement.executeUpdate();
            InitProcessDatabaseMethodInterceptor.LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows {0}", r);
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }
}
