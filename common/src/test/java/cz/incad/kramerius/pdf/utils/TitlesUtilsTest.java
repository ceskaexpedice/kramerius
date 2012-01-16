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
package cz.incad.kramerius.pdf.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.w3c.dom.Document;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.AbstractGuiceTestCase;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.XMLUtils;

public class TitlesUtilsTest extends AbstractGuiceTestCase {

    @Test
    public void testTitle() throws IOException, TransformerException {
        Injector inj = injector();
        String lidoveNoviny = "uuid:bdc405b0-e5f9-11dc-bfb2-000d606f5dc6";
        //String strankaLidNov = "uuid:55219069-435f-11dd-b505-00145e5790ea";
        
        FedoraAccess fa = inj.getInstance(FedoraAccess.class);
        SolrAccess sa = inj.getInstance(SolrAccess.class);
        ObjectPidsPath[] paths = sa.getPath(lidoveNoviny);
        ObjectPidsPath path = paths[0];
        
        List<String> sPath = new ArrayList<String>(Arrays.asList(path.getPathFromLeafToRoot()));
        
        //System.out.println(title);
        Document dc = fa.getDC(lidoveNoviny);
        
        
        List<String> keys = new ArrayList<String>();
        keys.add("title");
        keys.add("publisher");
        keys.add("date");
        keys.add("type");
        keys.add("identifier");

        String titleFromDC = DCUtils.titleFromDC(dc);
        String[] publishersFromDC = DCUtils.publishersFromDC(dc);
        String date = DCUtils.dateFromDC(dc);

        System.out.println("Title from DC :" + titleFromDC);
        System.out.println("Publishers :"+Arrays.asList(publishersFromDC));
        System.out.println("Date :"+date);
        
       // XMLUtils.print(dc, System.out);
    }

    @Override
    protected Injector injector() {
        Injector injector = Guice.createInjector(new SimpleFedoraAccessModule());
        return injector;
    }

    
    
}
