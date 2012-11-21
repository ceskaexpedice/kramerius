/*
 * Copyright (C) 2012 Pavel Stastny
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
/**
 * 
 */
package cz.incad.kramerius;

import java.sql.Connection;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;

/**
 * @author pavels
 *
 */
public abstract class AbstractGuiceTestCase {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(AbstractGuiceTestCase.class.getName());
    
    

    public Connection connection() {
        Injector inj = injector();
        Provider<Connection> kramerius4Provider = inj.getProvider(Key.get(Connection.class, Names.named("kramerius4")));
        Connection connection = kramerius4Provider.get();
        return connection;
    }

    protected abstract Injector injector();

}
