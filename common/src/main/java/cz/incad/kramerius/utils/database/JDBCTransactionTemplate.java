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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class JDBCTransactionTemplate {
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(JDBCTransactionTemplate.class.getName());
    
    private Connection connection;
    private boolean closeConnectionFlag = true;

    private Callbacks callbacks;
    
    
    public JDBCTransactionTemplate(Connection con, boolean closeConnection) {
        this.connection = con;
        this.closeConnectionFlag = closeConnection;
    }
    

    
    public Callbacks getCallbacks() {
        return callbacks;
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }


    public Object updateWithTransaction(final List<JDBCCommand> commands) throws SQLException {
        return this.updateWithTransaction((JDBCCommand[]) commands.toArray(new JDBCCommand[commands.size()]));
    }    
    
    public Object updateWithTransaction(Callbacks callbacks, final JDBCCommand... commands) throws SQLException {
        this.setCallbacks(callbacks);
        return this.updateWithTransaction(commands);
    }
    
    public Object updateWithTransaction(final JDBCCommand... commands) throws SQLException {
        boolean previous = this.connection.getAutoCommit();
        try {
            this.connection.setAutoCommit(false);
            
            Object obj = null;
            for (int i = 0,ll=commands.length; i < ll; i++) {
                JDBCCommand command = commands[i];
                command.setPreviousResult(obj);
                obj = command.executeJDBCCommand(this.connection);
            }
            this.connection.commit();
            if (callbacks != null) {
                callbacks.commited();
            }
            return obj;
        } catch (SQLException ex) {
            this.connection.rollback();
            if (callbacks != null) {
                callbacks.rollbacked();
            }
            throw ex;
        } finally {
            this.connection.setAutoCommit(previous);
            if (closeConnectionFlag ) {
                try {
                    LOGGER.info("Closing connection !");
                    this.connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }

    }
    

    public static interface Callbacks {
        
        void commited();
        void rollbacked();
    }
}
