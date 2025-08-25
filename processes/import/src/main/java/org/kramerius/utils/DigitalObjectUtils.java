/*
 * Copyright (C) 2025  Inovatika
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
package org.kramerius.utils;

import org.ceskaexpedice.fedoramodel.DatastreamType;
import org.ceskaexpedice.fedoramodel.DatastreamVersionType;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.ceskaexpedice.fedoramodel.XmlContentType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.List;

public class DigitalObjectUtils {

    private DigitalObjectUtils() {
    }

    public static String dcTitle(DigitalObject dobj) {
        String title = "";
        for (DatastreamType ds : dobj.getDatastream()) {
            if ("DC".equals(ds.getID())) {//obtain title from DC stream
                List<DatastreamVersionType> versions = ds.getDatastreamVersion();
                if (versions != null) {
                    DatastreamVersionType v = versions.get(versions.size() - 1);
                    XmlContentType dcxml = v.getXmlContent();
                    List<Element> elements = dcxml.getAny();
                    for (Element el : elements) {
                        NodeList titles = el.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "title");
                        if (titles.getLength() > 0) {
                            title = titles.item(0).getTextContent();
                        }
                    }
                }
            }
        }
        return title;
    }
}
