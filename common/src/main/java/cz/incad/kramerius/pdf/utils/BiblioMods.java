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



public class BiblioMods extends BiblioModsObject {
    
    private List<BiblioModsTitleInfo> titles = new ArrayList<BiblioModsTitleInfo>();
    private List<BiblioModsPart> parts = new ArrayList<BiblioModsPart>();
    private List<BiblioModsIdentifier> identifiers = new ArrayList<BiblioModsIdentifier>();
    
    private String modelName;
    
    public BiblioMods(String modelName) {
        super();
        this.modelName = modelName;
    }
    
    public String getModelName() {
        return modelName;
    }
    
    public BiblioModsTitleInfo findTitle() {
        BiblioModsTitleInfo mainTitle = null;
        BiblioModsTitleInfo alternative = null;
        for (int i = 0; i < this.titles.size(); i++) {
            BiblioModsTitleInfo info = this.titles.get(i);
            if (info.getType() == null) {
                mainTitle = info;
            } else if (info.getType().equals("alternative")) {
                alternative = info;
            }
        }
        return mainTitle != null ? mainTitle : alternative;
    }

    public BiblioModsPart findDetail(String type) {
        for (int i = 0, ll = this.parts.size(); i < ll; i++) {
            BiblioModsPart part =  this.parts.get(i);
            BiblioModsDetail detail =  part.findDetail(type);
            if (detail != null) return part;
        }
        return null;
    }
    
    @Override
    public void init(Element elm) {
        NodeList childNodes = elm.getChildNodes();
        // TODO Auto-generated method stub
        for (int i = 0, ll = childNodes.getLength(); i < ll; i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                String nm = item.getLocalName();
                if (nm.equals("identifier")) {
                    BiblioModsIdentifier ident = new BiblioModsIdentifier();
                    ident.init((Element) item);
                    this.identifiers.add(ident);
                }
                
                if (nm.equals("titleInfo")) {
                    BiblioModsTitleInfo titleInfo = new BiblioModsTitleInfo();
                    titleInfo.init((Element) item);
                    this.titles.add(titleInfo);
                }
               
                if (nm.equals("part")) {
                    BiblioModsPart part = new BiblioModsPart();
                    part.init((Element) item);
                    this.parts.add(part);
                }
            }
        }
    }

    
    public List<BiblioModsPart> getParts() {
        return parts;
    }
    
    public List<BiblioModsTitleInfo> getTitles() {
        return titles;
    }

    
    //"issn" "isbn"
    public BiblioModsIdentifier findIdent(String type) {
        for (int i = 0, ll = this.identifiers.size(); i < ll; i++) {
            BiblioModsIdentifier ident =  this.identifiers.get(i);
            if (ident.getType() != null && ident.getType().equals(type)) {
                return ident;
            }
        }
        return null;
    }
}
