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
package cz.incad.kramerius.pdf.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.pdf.FirstPagePDFService;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.SimplePDFService;
import cz.incad.kramerius.pdf.impl.FirstPageAsImagePDFServiceImpl;
import cz.incad.kramerius.pdf.impl.FirstPagePDFServiceImpl;
import cz.incad.kramerius.pdf.impl.GeneratePDFServiceImpl;
import cz.incad.kramerius.pdf.impl.SimplePDFServiceImpl;
import cz.incad.kramerius.security.SecuredFedoraAccessImpl;

public class PDFModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GeneratePDFService.class).to(GeneratePDFServiceImpl.class);
        
        bind(FirstPagePDFService.class).annotatedWith(Names.named(FirstPagePDFService.FirstPageType.TEXT.name())).to(FirstPagePDFServiceImpl.class).in(Scopes.SINGLETON);
        bind(FirstPagePDFService.class).annotatedWith(Names.named(FirstPagePDFService.FirstPageType.IMAGE.name())).to(FirstPageAsImagePDFServiceImpl.class).in(Scopes.SINGLETON);

        bind(SimplePDFService.class).to(SimplePDFServiceImpl.class);
    }

    
}
