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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.collections.map.HashedMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.utils.XMLUtils;

import java.util.ArrayList;

public class SOLRUtils {

    public static final Logger LOGGER = Logger.getLogger(SOLRUtils.class.getName());
    
    public static Map<Class, String> SOLR_TYPE_NAMES = new HashMap<Class, String>();
    static {
        SOLR_TYPE_NAMES.put(String.class, "str");
        SOLR_TYPE_NAMES.put(Boolean.class, "bool");
        SOLR_TYPE_NAMES.put(Integer.class, "int");
    }

    public static <T> T value(String val, Class<T> clz) {
        if (val == null) return null;
        val = val.trim();
        if (clz.equals(String.class))
            return (T) val;
        else if (clz.equals(Boolean.class))
            return (T) new Boolean(val);
        else if (clz.equals(Integer.class))
            return (T) Integer.valueOf(val);
        else
            throw new IllegalArgumentException("unsupported type " + clz + "");
    }

    public static Element value(Document doc, String val) {
        synchronized (doc) {
            return value(doc, null, val);
        }
    }

    public static Element value(Document doc, String attname, String val) {
        synchronized (doc) {
            Element strElm = doc.createElement("str");
            if (attname != null)
                strElm.setAttribute("name", attname);
            strElm.setTextContent(val);
            return strElm;
        }
    }

    public static Element value(Document doc, Integer val) {
        synchronized (doc) {
            return value(doc, null, val);
        }
    }

    public static Element value(Document doc, String attname, Integer val) {
        synchronized(doc) {
            Element strElm = doc.createElement("int");
            if (attname != null)
                strElm.setAttribute("name", attname);
            strElm.setTextContent("" + val);
            return strElm;
        }
    }

    public static Element arr(Document doc, String attname, List vals) {
        synchronized(doc) {
            Element arrElm = doc.createElement("arr");
            if (attname != null)
                arrElm.setAttribute("name", attname);
            for (Object obj : vals) {
                if (obj instanceof String) {
                    arrElm.appendChild(value(doc, (String) obj));
                } else if (obj instanceof Integer) {
                    arrElm.appendChild(value(doc, (Integer) obj));
                } else
                    throw new IllegalArgumentException("unsupported type "
                            + obj.getClass().getName() + "");
            }
            return arrElm;
        }
    }

    public static <T> T value(final Element doc, final String attributeName,
            Class<T> clz) {
        if (doc == null) {
            throw new IllegalArgumentException("element must not be null");
        }
        synchronized(doc.getOwnerDocument()) {
            final String expectedTypeName = SOLR_TYPE_NAMES.get(clz);
            List<Element> elms = XMLUtils.getElements(doc,
                    new XMLUtils.ElementsFilter() {

                        @Override
                        public boolean acceptElement(Element element) {
                            return (element.getNodeName().equals(expectedTypeName)
                                    && element.hasAttribute("name") && element
                                    .getAttribute("name").equals(attributeName));
                        }
                    });
            Object obj = elms.isEmpty() ? null : elms.get(0).getTextContent();
            if (obj != null)
                return value(obj.toString(), clz);
            else
                return null;
        }
    }

    public static <T> List<T> array(final Element doc,
            final String attributeName, Class<T> clz) {
        synchronized(doc.getOwnerDocument()) {
            List<T> ret = new ArrayList<T>();
            List<Element> elms = XMLUtils.getElements(doc,
                    new XMLUtils.ElementsFilter() {

                        @Override
                        public boolean acceptElement(Element element) {
                            return (element.getNodeName().equals("arr")
                                    && element.hasAttribute("name") && element
                                    .getAttribute("name").equals(attributeName));
                        }
                    });
            for (Element e : elms) {
                ret.add(value(e.getTextContent(), clz));
            }
            return ret;
        }        
    }

    
    //TODO: CDK Bugfix !! change basic array method !
    public static <T> List<T> narray(final Element doc,
            final String attributeName, Class<T> clz) {
        synchronized(doc.getOwnerDocument()) {
            List<T> ret = new ArrayList<T>();
            List<Element> elms = XMLUtils.getElements(doc,
                    new XMLUtils.ElementsFilter() {
                        @Override
                        public boolean acceptElement(Element element) {
                            return (element.getNodeName().equals("arr")
                                    && element.hasAttribute("name") && element
                                    .getAttribute("name").equals(attributeName));
                        }
                    });
         
            if (elms.size() >= 1) {
                Element parentE = elms.get(0);
                NodeList chnds = parentE.getChildNodes();
                for (int i = 0,ll=chnds.getLength() ; i < ll; i++) {
                    Node n = chnds.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        ret.add(value(n.getTextContent(), clz));
                    }
                }
            }
            return ret;
        }
    }
}
