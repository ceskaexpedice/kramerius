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
package cz.incad.kramerius.fedora;

import java.io.IOException;

import org.w3c.dom.Document;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.service.ResourceBundleServiceModule;
import junit.framework.TestCase;

public class FedoraAccessTests extends TestCase {

    public void testProcessSubTree() throws IOException, ProcessSubtreeException {
        Injector injector = Guice.createInjector(new FedoraAccessModule());
        FedoraAccess instance = injector.getInstance(Key.get(FedoraAccess.class, Names.named("testFA")));
        assertNotNull(instance);
        Document relsExt = instance.getRelsExt("0eaa6730-9068-11dd-97de-000d606f5dc6");
        assertNotNull(relsExt);
        instance.processSubtree("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", new TreeNodeProcessor() {
            
            @Override
            public void process(String pid, int level) {
                System.out.println("Processed "+pid);
            }
            
            @Override
            public boolean breakProcessing(String pid, int level) {
                // TODO Auto-generated method stub
                return false;
            }
        });
    }
}
