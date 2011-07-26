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
package cz.incad.Kramerius;

import java.sql.Connection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.processes.database.MostDesirableDatabaseInitializator;
import cz.incad.kramerius.processes.database.ProcessDatabaseInitializator;
import cz.incad.kramerius.security.database.SecurityDatabaseInitializator;
import cz.incad.kramerius.users.database.LoggedUserDatabaseInitializator;
import cz.incad.kramerius.utils.DatabaseUtils;

/**
 * Starting point for K4 application
 */
public class StartupServlet extends GuiceServlet {

    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider = null;

    @Override
    public void init() throws ServletException {
        super.init();
        
        Connection connection = this.connectionProvider.get();
        try {
            // process tables
            ProcessDatabaseInitializator.initDatabase(connection);
            // mostdesirable table
            MostDesirableDatabaseInitializator.initDatabase(connection);
            // all security tables
            SecurityDatabaseInitializator.initDatabase(connection);
            // Logged users table
            //LoggedUserDatabaseInitializator.initDatabase(connection);
        } finally {
            if (connection != null) { DatabaseUtils.tryClose(connection); }
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }
    
    
    
}
