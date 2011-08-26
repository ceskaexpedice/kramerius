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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BiblioModsPart extends BiblioModsObject{

    private List<BiblioModsDetail> details = new ArrayList<BiblioModsDetail>();
    private String date;
    
    
    public List<BiblioModsDetail> getDetails() {
        return details;
    }
    
    public String getDate() {
        return date;
    }

    
    @Override
    public void init(Element elm) {
        NodeList childNodes = elm.getChildNodes();
        for (int i = 0, ll = childNodes.getLength(); i < ll; i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                Element chelem = (Element) item;
                String localName = chelem.getLocalName();
                if (localName.equals("detail")) {
                    BiblioModsDetail detail = new BiblioModsDetail();
                    detail.init(chelem);
                    this.details.add(detail);
                }
                if (localName.equals("date")) {
                    this.date = chelem.getTextContent();
                }
            }
        }
    }

    public BiblioModsDetail findDetail(String type) {
        for (int i = 0, ll = this.details.size(); i < ll; i++) {
            BiblioModsDetail detail = this.details.get(i);
            if (detail.getType() != null && detail.getType().equals(type)) {
                return detail;
            }
        }
        return null;
    }
    
}
