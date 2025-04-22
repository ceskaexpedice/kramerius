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
package cz.incad.kramerius.rest.apiNew.client.v70.redirection;

import cz.incad.kramerius.rest.apiNew.client.v70.redirection.impl.DeleteTriggerSupportImpl;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.impl.DeleteTriggerSupportImplTest;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ResourcesUtils {

    public static Document solrAccess(Class clz, String path, String pid) throws ParserConfigurationException, SAXException, IOException {
        String uuidPart = subPart(pid);
        InputStream sa = clz.getResourceAsStream(String.format( "%s/solr_%s.xml", path, uuidPart));
        Document saDoc = XMLUtils.parseDocument(sa, false);
        return saDoc;
    }

    public static InputStream introspectStream(Class clz, String path, String pid) throws IOException {
        String uuidPart = subPart(pid);
        String fileName = "introspect_" + uuidPart + ".json";
        InputStream is  = StringUtils.isAnyString(path)? clz.getResourceAsStream(path+"/"+fileName) : clz.getResourceAsStream(fileName);
        return is;
    }

    @NotNull
    public static JSONObject introspect(Class clz,String path, String pid) throws IOException {
        InputStream is = introspectStream(clz,path, pid);
        JSONObject obj = new JSONObject(IOUtils.toString(is,"UTF-8"));
        return obj;
    }

//    @NotNull
//    public static Map<String, JSONObject> introspect(Class clz,String path, String pid) throws IOException {
//        String uuidPart = subPart(pid);
//        String fileName = "introspect_" + uuidPart + ".json";
//        InputStream is  =  clz.getResourceAsStream(path+"/"+fileName);
//        JSONObject obj = new JSONObject(IOUtils.toString(is,"UTF-8"));
//        Map<String,JSONObject> intrposectResult = new HashMap<>();
//        obj.keySet().forEach(k-> {
//            intrposectResult.put(k.toString(), obj.getJSONObject(k.toString()));
//        });
//        return intrposectResult;
//    }

    @NotNull
    public static String subPart(String pid) {
        if (pid == null || !pid.startsWith("uuid:")) {
            throw new IllegalArgumentException("Invalid input format");
        }
        String uuidPart = pid.substring(5);
        return uuidPart;
    }
}
