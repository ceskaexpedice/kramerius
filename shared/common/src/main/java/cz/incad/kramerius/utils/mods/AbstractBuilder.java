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
package cz.incad.kramerius.utils.mods;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

/**
 * Implementace tridy je zodpovedna na vytahnuti konkretnich casti ze streamu BIBLIO_MODS.   
 * @author pavels
 */
public abstract class AbstractBuilder {
    
    /** Hlavni titulek casti */
    public static final String DEFAULT_TITLE="mods:defaultTitle";
    
    private XPathFactory factory = XPathFactory.newInstance();

    public abstract void build(Document document, Map<String, List<String>> map, String model) throws XPathExpressionException;
    
    protected XPathFactory getFactory() {
        return factory;
    }
    
    /** Pridava do mapy hodnotu */
    public void add(String key, String val, Map<String, List<String>> map) {
        List<String> list = map.get(key);
        if (list == null) {
            list = new ArrayList<String>();
            map.put(key, list);
        }
        list.add(val);
    }
}
