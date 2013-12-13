/*
 * Copyright (C) 2013 Pavel Stastny
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
package cz.incad.kramerius.rest.api.k5.client.utils;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import cz.incad.kramerius.utils.XMLUtils;

import java.util.ArrayList;

public class SOLRUtils {
	
	public static <T> T  value(String val, Class<T> clz) {
		if (clz.equals(String.class)) return (T) val;
		else if (clz.equals(Boolean.class)) return (T) new Boolean(val);
		else if (clz.equals(Integer.class)) return (T) Integer.valueOf(val);
		else throw new IllegalArgumentException("unsupported type "+clz+"");
	}
	

	
    public static <T> T value(final Element doc, final String attributeName, Class<T> clz) {
        List<Element> elms = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {

            @Override
            public boolean acceptElement(Element element) {
                return (element.getNodeName().equals("str") && element.hasAttribute("name") && element.getAttribute("name").equals(attributeName));
            }
        });
        Object obj= elms.isEmpty() ? null : elms.get(0).getTextContent();
        if (obj != null) return value(obj.toString(), clz);
        else return null;
    }

    public static  <T>  List<T> array(final Element doc, final String attributeName,Class<T> clz) {
        List<T> ret = new ArrayList<T>();
        List<Element> elms = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {

            @Override
            public boolean acceptElement(Element element) {
                return (element.getNodeName().equals("arr") && element.hasAttribute("name") && element.getAttribute("name").equals(attributeName));
            }
        });
        for (Element e : elms) {
            ret.add(value(elms.get(0).getTextContent(),clz));
        }
        return ret;
    }

    
}
