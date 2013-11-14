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

import java.util.List;

import org.w3c.dom.Element;

import cz.incad.kramerius.utils.XMLUtils;


public class SOLRUtils {

	public static String string(final Element doc, final String attributeName) {
		List<Element> elms = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
			
			@Override
			public boolean acceptElement(Element element) {
				return (element.getNodeName().equals("str") && element.hasAttribute("name") && element.getAttribute("name").equals(attributeName));
			}
		});
		return elms.isEmpty() ? null : elms.get(0).getTextContent();
	}
	
}
