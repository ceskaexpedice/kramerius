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
package cz.incad.kramerius.lp;

import java.io.IOException;

import junit.framework.TestCase;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.lp.guice.PDFModule;

public class TestTitle extends TestCase {

    
    public void testTitle() throws IOException {
        String uuid = "9ccee465-e645-11de-a504-001143e3f55c";
        Injector injector = Guice.createInjector(new PDFModule());

        SolrAccess solrAccess = injector.getInstance(SolrAccess.class);
        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess"))); 

        //String calculatedTitle = TitlesUtils.title(uuid, solrAccess, fa);
        //System.out.println(calculatedTitle);
        
        
//        org.w3c.dom.Document relsExt = fa.getRelsExt(uuid);
//        KrameriusModels model = fa.getKrameriusModel(relsExt);
//        final AbstractRenderedDocument renderedDocument = new RenderedDocument(model, parentUUID);
//        renderedDocument.setDocumentTitle(getTitle(this.fedoraAccess.getBiblioMods(parentUUID), model));

    }
}
