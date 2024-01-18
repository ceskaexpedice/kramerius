/*
 * Copyright (C) Jan 10, 2024 Pavel Stastny
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
package cz.incad.kramerius.rest.oai;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.utils.XMLUtils;

public class OAITools {

    private static final String XSI_SCHEMA_LOCATION = "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd";
    private static final String OAI_ARCHIVES = "http://www.openarchives.org/OAI/2.0/";
    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";

    
    public static Document createOAIDocument() throws ParserConfigurationException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        // Definujte jmenné prostory a jejich přidání k elementům
        String xsiNamespace = XSI_NAMESPACE;
        String oaiNamespace = OAI_ARCHIVES;

        Element oaiPmhElement = document.createElementNS(oaiNamespace, "OAI-PMH");
        oaiPmhElement.setAttribute("xmlns:xsi", xsiNamespace);
        oaiPmhElement.setAttribute("xsi:schemaLocation", XSI_SCHEMA_LOCATION);

        Element responseDateElement = document.createElement("responseDate");

        OffsetDateTime now = OffsetDateTime.now();

        responseDateElement.setTextContent(now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        oaiPmhElement.appendChild(responseDateElement);

        document.appendChild(oaiPmhElement);
        return document;
    }
    
    public static Element requestElement(Document doc, OAIVerb verb, OAISet set, String baseUrl, MetadataExport metadata) {
        Element elm = doc.createElement( "request");
        if (verb != null) {
            elm.setAttribute( "verb", verb.name());
        }
        if (set != null) {
            elm.setAttribute( "set", set.getSetSpec());
        }
        if (metadata != null) {
            elm.setAttribute( "metadataPrefix", metadata.getMetadataPrefix());
        }
        elm.setTextContent(baseUrl+(baseUrl.endsWith("/")? "" : "/")+"api/harvest/v7.0/oai");
        return elm;
    }
    
    /** Medata from resumption token */
    public static String metadataFromResumptionToken(String resumptionToken) {
        String[] split = resumptionToken.split(":");
        if (split.length > 2) {
            return split[2];
        } else return null;
    }
    
    /** Set spec from resumption token */
    public static String specFromResumptionToken(String resumptionToken) {
        if (resumptionToken.contains(":")) {
            String[] split = resumptionToken.split(":");
            if (split.length > 1) {
                return split[1];
            } else return null;
    
        } else return null;
    }
    
    /** Solr cursorMark from resumption token */
    public static String solrCursorMarkFromResumptionToken(String resumptionToken) {
        if (resumptionToken.contains(":")) {
            String[] split = resumptionToken.split(":");
            if (split.length > 1) {
                return split[0];
            } else return null;
        } else return null;
    }
    
    /** create OAI identifier */
    public static String oaiIdentfier(String host, String pid) {
        String oaiIdentifier =  String.format("oai:%s:%s", host, pid);
        return oaiIdentifier;
    }
    
    
    /** host from oai identifier */
    public static String hostFromOAIIdentifier(String oaiIdentifier) {
        String[] split = oaiIdentifier.split(":");
        if (split.length > 1) {
            return split[1];
        } else return null;
    }
    
    /** pid from oai identifier */
    public static String pidFromOAIIdentifier(String oaiIdentifier) {
        int pidIndex = oaiIdentifier.indexOf("uuid:");
        if (pidIndex > -1) {
            return oaiIdentifier.substring(pidIndex);
        } else return null;
    }
    
}
