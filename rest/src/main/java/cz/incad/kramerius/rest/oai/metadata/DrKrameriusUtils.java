/*
 * Copyright (C) Aug 26, 2024 Pavel Stastny
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
package cz.incad.kramerius.rest.oai.metadata;

import javax.xml.namespace.QName;

public class DrKrameriusUtils {
    
    private DrKrameriusUtils() {}

    public static final String DR_NS_URI = "http://registrdigitalizace.cz/schemas/drkramerius/v4";
    public static final String DR_NS_PREFIX = "dr";
    public static final QName RECORD_QNAME = new QName(DR_NS_URI, "record", DR_NS_PREFIX);
    public static final QName UUID_QNAME = new QName(DR_NS_URI, "uuid", DR_NS_PREFIX);
    public static final QName TYPE_QNAME = new QName(DR_NS_URI, "type", DR_NS_PREFIX);
    //Disabled 
    public static final QName POLICY_QNAME = new QName(DR_NS_URI, "policy", DR_NS_PREFIX);
    public static final QName RELATION_QNAME = new QName(DR_NS_URI, "relation", DR_NS_PREFIX);
    public static final QName DESCRIPTOR_QNAME = new QName(DR_NS_URI, "descriptor", DR_NS_PREFIX);
}
