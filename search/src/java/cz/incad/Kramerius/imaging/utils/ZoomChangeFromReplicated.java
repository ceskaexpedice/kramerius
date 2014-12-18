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
package cz.incad.Kramerius.imaging.utils;

import java.util.List;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.utils.XMLUtils;

public class ZoomChangeFromReplicated {

    public static String zoomifyAddress(Document relsExt, String pid) {
        String replicatedFrom = replicatedFrom(relsExt);
        if (replicatedFrom != null) {
            int indexOf = replicatedFrom.indexOf("/handle/");
            String app = replicatedFrom.substring(0, indexOf);
            return app + "/zoomify/" + pid;
        } else
            return null;
    }

    private static String replicatedFrom(Document relsExt) {
        Element descElement = XMLUtils.findElement(
                relsExt.getDocumentElement(), "Description",
                FedoraNamespaces.RDF_NAMESPACE_URI);
        List<Element> delems = XMLUtils.getElements(descElement);
        for (Element del : delems) {
            if (del.getNamespaceURI() != null) {
                if (del.getNamespaceURI()
                        .equals(FedoraNamespaces.KRAMERIUS_URI)
                        && del.getLocalName().equals("replicatedFrom")) {
                    return del.getTextContent();
                }
            }
        }
        return null;
    }

    public static String deepZoomAddress(Document relsExt, String pid) {
        String replicatedFrom = replicatedFrom(relsExt);
        return deepZoomInternal(pid, replicatedFrom);
    }

    private static String deepZoomInternal(String pid, String replicatedFrom) {
        if (replicatedFrom != null) {
            int indexOf = replicatedFrom.indexOf("/handle/");
            String app = replicatedFrom.substring(0, indexOf);
            return app + "/deepZoom/" + pid;
        } else
            return null;
    }
}
