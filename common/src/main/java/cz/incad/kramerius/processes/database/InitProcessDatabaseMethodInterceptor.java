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

import java.sql.Connection;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.security.database.InitSecurityDatabaseMethodInterceptor;
import cz.incad.kramerius.utils.DatabaseUtils;
import java.util.logging.Logger;

/**
 * Inicializace databaze procesu
 * @author pavels
 */
public class InitProcessDatabaseMethodInterceptor implements MethodInterceptor {

    static final Logger LOGGER = Logger.getLogger(InitProcessDatabaseMethodInterceptor.class.getName());
    
    @Inject
    @Named("kramerius4")
    Provider<Connection> provider;

    public InitProcessDatabaseMethodInterceptor() {
        super();
    }


    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Connection connection = this.provider.get();
        try {
            // moved to Init servlet
            /*
            if (!DatabaseUtils.tableExists(connection,"PROCESSES")) {
                createProcessTable(connection);
            }
            if (!DatabaseUtils.columnExists(connection, "PROCESSES", "STARTEDBY")) {
                alterProcessTableStartedByColumn(connection);
            }
            if (!DatabaseUtils.columnExists(connection, "PROCESSES", "TOKEN")) {
                alterProcessTableProcessToken(connection);
            }
            if (!DatabaseUtils.tableExists(connection,"USER_ENTITY")) {
                InitSecurityDatabaseMethodInterceptor.createSecurityTables(connection);
            }
            */
            return invocation.proceed();
        } finally {
            if (connection != null) { DatabaseUtils.tryClose(connection); }
        }
    }


}

