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
import java.sql.SQLException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

public class InitSecurityDatabaseMethodInterceptor implements MethodInterceptor {

    @Inject
    @Named("kramerius4")
    Provider<Connection> provider;

    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Connection connection = this.provider.get();
        try {
            // MOVED to startup servlet

            /*
            if (!DatabaseUtils.tableExists(connection,"USER_ENTITY")) {
                createSecurityTables(connection);
            }
            */
            return invocation.proceed();
        } finally {
            if (connection != null) { DatabaseUtils.tryClose(connection); }
        }
    }


    
}
