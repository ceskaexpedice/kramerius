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

import java.io.FileNotFoundException;

import javax.script.ScriptException;

import org.easymock.EasyMock;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.security.RightCriteriumContextFactory;
import cz.incad.kramerius.security.RightCriteriumLoader;
import cz.incad.kramerius.security.RightCriteriumWrapperFactory;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.ClassRightCriteriumLoaderImpl;
import cz.incad.kramerius.security.impl.DatabaseRightsManager;
import cz.incad.kramerius.security.impl.DatabaseUserManager;
import cz.incad.kramerius.security.impl.RightCriteriumContextFactoryImpl;
import cz.incad.kramerius.security.impl.RightCriteriumWrapperFactoryImpl;
import cz.incad.kramerius.security.impl.ScriptCriteriumLoaderImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class MockGuiceSecurityModule extends AbstractModule {

    @Override
    protected void configure() {
        // needs database connection
        bind(RightsManager.class).toInstance(EasyMock.createMock(RightsManager.class));
        bind(UserManager.class).toInstance(EasyMock.createMock(UserManager.class));
        
        bind(RightCriteriumContextFactory.class).to(RightCriteriumContextFactoryImpl.class);
        
        Multibinder<RightCriteriumLoader> criteriumLoders = Multibinder.newSetBinder(binder(), RightCriteriumLoader.class);
        criteriumLoders.addBinding().to(ClassRightCriteriumLoaderImpl.class).asEagerSingleton();
//        try {
//            criteriumLoders.addBinding().toInstance( new ScriptCriteriumLoaderImpl(KConfiguration.getInstance()));
//        } catch (FileNotFoundException | NoSuchMethodException | ScriptException e) {
//            e.printStackTrace();
//        }

        bind(RightCriteriumWrapperFactory.class).to(RightCriteriumWrapperFactoryImpl.class);
        
    }

    
}
