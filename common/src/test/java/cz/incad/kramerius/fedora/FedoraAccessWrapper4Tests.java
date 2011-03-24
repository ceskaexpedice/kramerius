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
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.fedora.api.FedoraAPIA;
import org.fedora.api.FedoraAPIM;
import org.fedora.api.ObjectFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class FedoraAccessWrapper4Tests extends FedoraAccessImpl {


    
    @Inject
    public FedoraAccessWrapper4Tests(KConfiguration configuration) {
        super(configuration);
    }

    public Document getRelsExt(String uuid) throws IOException {
        try {
            Map<String, String> map = new HashMap<String, String>(){{
               put("0eaa6730-9068-11dd-97de-000d606f5dc6","res/0eaa6730-9068-11dd-97de-000d606f5dc6.xml");
               put("4308eb80-b03b-11dd-a0f6-000d606f5dc6","res/4308eb80-b03b-11dd-a0f6-000d606f5dc6.xml");
            }};
            
            String path = "res/"+uuid+".xml";
            System.out.println("path =="+path);
            return XMLUtils.parseDocument(this.getClass().getResourceAsStream(path), true);
            //return fedoraAccess.getRelsExt(uuid);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    
}
