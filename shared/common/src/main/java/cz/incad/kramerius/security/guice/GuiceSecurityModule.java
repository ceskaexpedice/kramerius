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
package cz.incad.kramerius.security.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;

import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.RightCriteriumContextFactory;
import cz.incad.kramerius.security.RightCriteriumLoader;
import cz.incad.kramerius.security.RightCriteriumWrapperFactory;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.database.InitSecurityDatabase;
import cz.incad.kramerius.security.database.InitSecurityDatabaseMethodInterceptor;
import cz.incad.kramerius.security.impl.ClassRightCriteriumLoaderImpl;
import cz.incad.kramerius.security.impl.DatabaseRightsManager;
import cz.incad.kramerius.security.impl.DatabaseUserManager;
import cz.incad.kramerius.security.impl.RightCriteriumContextFactoryImpl;
import cz.incad.kramerius.security.impl.RightCriteriumWrapperFactoryImpl;
import cz.incad.kramerius.security.impl.ScriptCriteriumLoaderImpl;

/**
 * Base abstract module for security in K4
 * @author pavels
 */
public class GuiceSecurityModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RightsManager.class).to(DatabaseRightsManager.class);
        bind(UserManager.class).to(DatabaseUserManager.class);
        bind(RightCriteriumContextFactory.class).to(RightCriteriumContextFactoryImpl.class);
        
        // bind criterium loaders
        Multibinder<RightCriteriumLoader> criteriumLoders = Multibinder.newSetBinder(binder(), RightCriteriumLoader.class);
        criteriumLoders.addBinding().to(ClassRightCriteriumLoaderImpl.class).asEagerSingleton();
        //criteriumLoders.addBinding().to(ScriptCriteriumLoaderImpl.class).asEagerSingleton();
        
        // wrapper factory
        bind(RightCriteriumWrapperFactory.class).to(RightCriteriumWrapperFactoryImpl.class);
        
        // init databases annotation
        /*
        InitSecurityDatabaseMethodInterceptor initDb = new InitSecurityDatabaseMethodInterceptor();
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(InitSecurityDatabase.class), 
                  initDb);
        requestInjection(initDb);
        */
    }
    
}
