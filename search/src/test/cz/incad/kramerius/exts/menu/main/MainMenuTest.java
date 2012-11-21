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
package cz.incad.kramerius.exts.menu.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.name.Named;

import cz.incad.Kramerius.exts.menu.main.guice.MainMenuConfiguration;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.IndexerAdministration;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.NDKMetsImport;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.ParametrizedConvert;
import cz.incad.kramerius.AbstractGuiceTestCase;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.impl.LRProcessDefinitionManagerImpl;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.service.ResourceBundleService;



/**
 * @author pavels
 *
 */
public class MainMenuTest extends AbstractGuiceTestCase {

    @Test
    public void shouldPass() {
        Injector inj = injector();
        IndexerAdministration reindexDialogItem = inj.getInstance(IndexerAdministration.class);
        Assert.assertTrue(reindexDialogItem.isRenderable());

        ParametrizedConvert parametrizedConvert = inj.getInstance(ParametrizedConvert.class);
        Assert.assertTrue(parametrizedConvert.isRenderable());

        NDKMetsImport metsImport = inj.getInstance(NDKMetsImport.class);
        Assert.assertFalse(metsImport.isRenderable());
    }
    
    @Override
    protected Injector injector() {
        HttpServletRequest mockReq  = EasyMock.createMock(HttpServletRequest.class);
        HttpSession mockSess = EasyMock.createMock(HttpSession.class);
        
        LRProcessManager processMan = EasyMock.createMock(LRProcessManager.class);
        ResourceBundleService resbundleserv = EasyMock.createMock(ResourceBundleService.class);
        
        EasyMock.expect(mockSess.getAttribute("securityForRepository")).andReturn(Arrays.asList(SecuredActions.REINDEX.getFormalName(), SecuredActions.CONVERT.getFormalName())).anyTimes();
        EasyMock.expect(mockReq.getSession()).andReturn(mockSess).anyTimes();
        EasyMock.replay(mockReq, processMan, resbundleserv, mockSess);

        
        Injector injector = Guice.createInjector(
                new MainMenuConfiguration(),
                new _Module(mockReq, processMan, resbundleserv)
        );
        return injector;
    }
    
    public static class _Module extends AbstractModule {

        HttpServletRequest request;
        LRProcessManager processManager;
        ResourceBundleService resBundleServ;
        
        public _Module(HttpServletRequest request, LRProcessManager manager, ResourceBundleService resbundle) {
            super();
            this.request = request;
            this.processManager = manager;
            this.resBundleServ = resbundle;
        }

        @Override
        protected void configure() {
            bind(DefinitionManager.class).to(LRProcessDefinitionManagerImpl.class).in(Scopes.SINGLETON);
            bind(LRProcessManager.class).toInstance(processManager);
            bind(ResourceBundleService.class).toInstance(resBundleServ);
        }
        
        @Provides
        public HttpServletRequest getRequest() {
            return this.request;
        }
        
        @Provides
        public Locale getLocale() {
            return Locale.getDefault();
        }
        
        @Provides
        @Named("LIBS")
        public String getLibsFolder() {
            return "LIBS";
        }

    }
}
