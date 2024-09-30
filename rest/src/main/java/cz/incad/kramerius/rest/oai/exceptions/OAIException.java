/*
 * Copyright (C) Jan 12, 2024 Pavel Stastny
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
package cz.incad.kramerius.rest.oai.exceptions;

import javax.ws.rs.WebApplicationException;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.rest.oai.ErrorCode;
import cz.incad.kramerius.rest.oai.MetadataExport;
import cz.incad.kramerius.rest.oai.OAISet;
import cz.incad.kramerius.rest.oai.OAIVerb;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;

import static cz.incad.kramerius.rest.oai.OAITools.*;

import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OAIException extends WebApplicationException {

    public static final Logger LOGGER = Logger.getLogger(OAIException.class.getName());
    
    protected OAIException(Response response) {
        super(response);
    }


    public OAIException(ErrorCode oaiErrorCode,OAIVerb verb, OAISet set,String baseUrl,MetadataExport metadata) {
        this(Response.status(oaiErrorCode.getStatusCode())
                .type(MediaType.APPLICATION_XML)
                .entity(buildXml(oaiErrorCode, verb, set, baseUrl, metadata, null))
                .build());
    }

    public OAIException(ErrorCode oaiErrorCode,OAIVerb verb, OAISet set,String baseUrl,MetadataExport metadata, String message) {
        this(Response.status(oaiErrorCode.getStatusCode())
                .type(MediaType.APPLICATION_XML)
                .entity(buildXml(oaiErrorCode, verb, set, baseUrl, metadata, message))
                .build());
    }

    public OAIException(int errorCode, ErrorCode oaiErrorCode,OAIVerb verb, OAISet set,String baseUrl,MetadataExport metadata) {
        this(Response.status(errorCode)
                .type(MediaType.APPLICATION_XML)
                .entity(buildXml(oaiErrorCode, verb, set, baseUrl, metadata, null))
                .build());
    }

    public OAIException(int errorCode, ErrorCode oaiErrorCode,OAIVerb verb, OAISet set,String baseUrl,MetadataExport metadata,String message) {
        this(Response.status(errorCode)
                .type(MediaType.APPLICATION_XML)
                .entity(buildXml(oaiErrorCode, verb, set, baseUrl, metadata, message))
                .build());
    }

    private static String buildXml(ErrorCode oaiErrorCode, OAIVerb verb, OAISet set,String baseUrl,MetadataExport metadata, String message) {
        try {
            Document oai = createOAIDocument();
            Element oaiRoot = oai.getDocumentElement();
            oaiRoot.appendChild(requestElement(oai, verb, set, baseUrl, metadata));
            
            Element error = oai.createElement("error");
            error.setAttribute("code", oaiErrorCode.name());
            oaiRoot.appendChild(error);
            if (StringUtils.isAnyString(message)) { error.setTextContent(message); }
            
            StringWriter writer = new StringWriter();
            XMLUtils.print(oai, writer);
            return writer.toString();
            
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (TransformerException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        return "";
    }

//    public AbstractOAIException(int errorCode, String messageTemplate, Object... messageArgs) {
//        this(errorCode, String.format(messageTemplate, messageArgs));
//    }
//
//    public AbstractOAIException(Response.Status status, String message) {
//        this(status.getStatusCode(), message);
//    }
//
//    public AbstractOAIException(Response.Status status, String messageTemplate, Object... messageArgs) {
//        this(status, String.format(messageTemplate, messageArgs));
//    }
//
//    private static String buildErrorJson(String errorMessage) {
//        JSONObject json = new JSONObject();
//        try {
//            //json.put("error", errorMessage);
//            json.put("message", errorMessage);
//        } catch (JSONException e) {
//            //noting
//        }
//        return json.toString();
//    }

}
