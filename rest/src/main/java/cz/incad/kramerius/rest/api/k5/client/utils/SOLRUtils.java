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
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.incad.kramerius.rest.api.k5.client.item.utils.ItemResourceUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.utils.XMLUtils;

//TODO: Delete
public class SOLRUtils {

    public static final Logger LOGGER = Logger.getLogger(SOLRUtils.class.getName());

    public static final Map<Class<?>, String> SOLR_TYPE_NAMES = new HashMap<>() {
        {
            put(String.class, "str");
            put(Boolean.class, "bool");
            put(Integer.class, "int");
        }
    };
    public static final Map<String, Class<?>> SOLR_NAME_TYPES = new HashMap<>() {
        {
            put("str", String.class);
            put("bool", Boolean.class);
            put("int", Integer.class);
        }
    };

    public static <T> T value(String val, Class<T> clz) {
        if (val == null) {
            return null;
        }
        val = val.trim();
        if (clz.equals(String.class)) {
            return clz.cast(val); 
        }else if (clz.equals(Boolean.class)) {
            return clz.cast(Boolean.valueOf(val)); 
        }else if (clz.equals(Integer.class)) {
            return clz.cast(Integer.valueOf(val)); 
        }else {
            throw new IllegalArgumentException("unsupported type " + clz + "");
        }
    }

    public static Element value(Document doc, String val) {
        synchronized (doc) {
            return value(doc, null, val);
        }
    }

    public static Element value(Document doc, String attname, String val) {
        synchronized (doc) {
            Element strElm = doc.createElement("str");
            if (attname != null) {
                strElm.setAttribute("name", attname);
            }
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
        synchronized (doc) {
            Element strElm = doc.createElement("int");
            if (attname != null) {
                strElm.setAttribute("name", attname);
            }
            strElm.setTextContent("" + val);
            return strElm;
        }
    }

    public static Element arr(Document doc, String attname, List<?> vals) {
        synchronized (doc) {
            Element arrElm = doc.createElement("arr");
            if (attname != null) {
                arrElm.setAttribute("name", attname);
            }
            for (Object obj : vals) {
                switch (obj) {
                    case String string -> arrElm.appendChild(value(doc, string));
                    case Integer i -> arrElm.appendChild(value(doc, i));
                    default -> throw new IllegalArgumentException("unsupported type "
                            + obj.getClass().getName() + "");
                }
            }
            return arrElm;
        }
    }

    public static <T> T value(final Element doc, final String attributeName,
            Class<T> clz) {
        if (doc == null) {
            throw new IllegalArgumentException("element must not be null");
        }
        synchronized (doc.getOwnerDocument()) {
            final String expectedTypeName = SOLR_TYPE_NAMES.get(clz);
            List<Element> elms = XMLUtils.getElements(doc, (Element element) -> (element.getNodeName().equals(expectedTypeName)
                    && element.hasAttribute("name") && element
                            .getAttribute("name").equals(attributeName)));
            Object obj = elms.isEmpty() ? null : elms.get(0).getTextContent();
            if (obj != null) {
                return value(obj.toString(), clz); 
            }else {
                return null;
            }
        }
    }

    public static <T> List<T> array(final Element doc,
            final String attributeName, Class<T> clz) {
        synchronized (doc.getOwnerDocument()) {
            List<T> ret = new ArrayList<>();
            List<Element> elms = XMLUtils.getElements(doc, (Element element) -> (element.getNodeName().equals("arr")
                    && element.hasAttribute("name") && element
                            .getAttribute("name").equals(attributeName)));
            for (Element e : elms) {
                ret.add(value(e.getTextContent(), clz));
            }
            return ret;
        }
    }

    //TODO: CDK Bugfix !! change basic array method !
    public static <T> List<T> narray(final Element doc,
            final String attributeName, Class<T> clz) {
        synchronized (doc.getOwnerDocument()) {
            List<T> ret = new ArrayList<>();
            List<Element> elms = XMLUtils.getElements(doc, (Element element) -> (element.getNodeName().equals("arr")
                    && element.hasAttribute("name") && element
                            .getAttribute("name").equals(attributeName)));

            if (elms.size() >= 1) {
                Element parentE = elms.get(0);
                NodeList chnds = parentE.getChildNodes();
                for (int i = 0, ll = chnds.getLength(); i < ll; i++) {
                    Node n = chnds.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        ret.add(value(n.getTextContent(), clz));
                    }
                }
            }
            return ret;
        }
    }

    /**
     * Finds correct rels ext position
     *
     * @param parentPid
     * @param docelm
     * @return
     */
    public static String relsExtIndex(String parentPid, Element docelm) {
        List<Integer> docindexes = narray(docelm, "rels_ext_index", Integer.class);

        if (docindexes.isEmpty()) {
            return "0";
        }
        List<String> parentPids = narray(docelm, "parent_pid", String.class);
        int index = 0;
        for (int i = 0, length = parentPids.size(); i < length; i++) {
            if (parentPids.get(i).endsWith(parentPid)) {
                index = i;
                break;
            }
        }
        if (docindexes.size() > index) {
            return "" + docindexes.get(index);
        } else {
            ItemResourceUtils.LOGGER.log(Level.WARNING, "bad solr document for parent_pid:{0}", parentPid);
            return "0";
        }
    }
}
