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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.FedoraNamespaces;

public class BiblioModsTitleInfo extends BiblioModsObject {

    private String type;
    private String title;
    private String subtitle;
    private String partName;
    
    public BiblioModsTitleInfo() {
        super();
    }
    

    public String getTitle() {
        return title;
    }
    
    public String getType() {
        return type;
    }
    
    public String getSubtitle() {
        return subtitle;
    }
    
    public String getPartName() {
        return partName;
    }


    @Override
    public void init(Element elm) {
        if (elm.hasAttribute("type")) {
            this.type = elm.getAttribute("type");
        }

        NodeList childNodes = elm.getChildNodes();
        for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
            Node n = childNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element chelem = (Element) n;
                
                if (n.getLocalName().equals("title")) {
                    this.title = n.getTextContent();
                }
                if (n.getLocalName().equals("subTitle")) {
                    this.subtitle = n.getTextContent();
                }
                
                if (n.getLocalName().equals("partName")) {
                    this.partName = n.getTextContent();
                }
                
            }
            
        }
    }
}
