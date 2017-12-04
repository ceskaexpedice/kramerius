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
package cz.incad.kramerius.utils.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Represents JDBC command used in {@link JDBCTransactionTemplate}
 * @author pavels
 */
public abstract class JDBCCommand {
    
    private Object obj;
    
    /**
     * Sets result from previous JDBCommand
     * @param obj result from previous jdbc command
     */
    public void setPreviousResult(Object obj) {
        this.obj = obj;
    }
    
    /**
     * Returns result from previous JDBCommand
     * @return result from previous JDBCommand
     */
    public Object getPreviousResult() {
        return this.obj;
    }
    
    /**
     * Executed JDBC command
     * @param con JDBC connection
     * @return results of jdbc command execution
     * @throws SQLException Something happened during jdbc command execution
     */
    public abstract Object executeJDBCCommand(Connection con) throws SQLException;
}
