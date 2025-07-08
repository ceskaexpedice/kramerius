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
package cz.incad.kramerius.rest.oai.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cz.incad.kramerius.rest.oai.OAISet;
import cz.incad.kramerius.rest.oai.OAIVerb;
import cz.incad.kramerius.rest.oai.strategies.MetadataExportStrategy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.utils.StringUtils;

/**
 * Utility class for OAI-PMH related operations.
 * Provides methods to create OAI documents, handle resumption tokens,
 * and manipulate OAI identifiers.
 */
public class OAITools {

    private static final String XSI_SCHEMA_LOCATION = "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd";
    private static final String OAI_ARCHIVES = "http://www.openarchives.org/OAI/2.0/";
    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";

    private OAITools() {}
    /**
     * Creates a new OAI-PMH document with the current response date.
     *
     * @return A Document object representing the OAI-PMH response.
     * @throws ParserConfigurationException If a DocumentBuilder cannot be created.
     */
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

    /**
     * Creates a request element for the OAI-PMH request.
     *
     * @param doc The document to which the request element will be added.
     * @param verb The OAI verb (e.g., GetRecord, Identify).
     * @param set The OAI set (optional).
     * @param baseUrl The base URL of the OAI-PMH service.
     * @param metadata The metadata export strategy used.
     * @return An Element representing the request.
     */
    public static Element requestElement(Document doc, OAIVerb verb, OAISet set, String baseUrl, MetadataExportStrategy metadata) {
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

    /**
     * Extracts the metadata prefix from a resumption token.
     * @param resumptionToken
     * @return
     */
    public static String metadataFromResumptionToken(String resumptionToken) {
        String[] split = resumptionToken.split(":");
        if (split.length > 2) {
            return split[2];
        } else return null;
    }



    /**
     * Set spec from resumption token
     */
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

    public static String fromFromResumptionToken(String resumptionToken) {
        if (resumptionToken.contains(":")) {
            String[] split = resumptionToken.split(":");
            if (split.length > 3) {
                String fromUntil=split[3];
                if (fromUntil.contains("from_")) {
                    int maxIndex =  fromUntil.indexOf("_until") > 0 ? fromUntil.indexOf("_until") :  fromUntil.length();
                    return fromUntil.substring(fromUntil.indexOf("from_")+"from_".length(), maxIndex);
                }
                return null;
            } else return null;
        } else return null;
        
    }

    public static String untilFromResumptionToken(String resumptionToken) {
        if (resumptionToken.contains(":")) {
            String[] split = resumptionToken.split(":");
            if (split.length > 3) {
                String fromUntil=split[3];
                if (fromUntil.contains("until_")) {
                    return fromUntil.substring(fromUntil.indexOf("until_")+"until_".length(), fromUntil.length());
                }
                return null;
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

    
    public static ZonedDateTime parseISO8601Date(String dateStr) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
        try {
            // YYYY-MM-DD)
            LocalDate date = LocalDate.parse(dateStr, dateFormatter);
            return date.atStartOfDay(ZonedDateTime.now().getZone());
        } catch (DateTimeParseException e) {
            // YYYY-MM-DDThh:mm:ssZ
            try {
                LocalDateTime dateTime = LocalDateTime.parse(dateStr, dateTimeFormatter);
                return dateTime.atZone(ZonedDateTime.now().getZone());
            } catch (DateTimeParseException ex) {
                // ZonedDateTime (full UTC)
                return ZonedDateTime.parse(dateStr, dateTimeFormatter);
            }
        }
    }
    
    public static String formatForSolr(ZonedDateTime dateTime) {
        DateTimeFormatter solrDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return dateTime.withZoneSameInstant(dateTime.getZone()).format(solrDateFormat);
    }
    
    public static String generateResumptionToken(String metadataPrefix, int rowsInPage, int configuredRowsInPage, String solrNextCursor, String setSpec, String from, String until) {
        if (configuredRowsInPage == rowsInPage && solrNextCursor != null) {
            StringBuilder builder = new StringBuilder().append(solrNextCursor).append(":").append(setSpec).append(":").append(metadataPrefix);
            if (StringUtils.isAnyString(from) || StringUtils.isAnyString(until)) {
                builder.append(":");
                if (StringUtils.isAnyString(from)) {
                    builder.append("from_").append(from);
                    
                    if (StringUtils.isAnyString(until)) {
                        builder.append("_");
                    }
                } 
                if (StringUtils.isAnyString(until)) {
                    builder.append("until_").append(until);
                }
            }
            return builder.toString();
        } else return null;
    }
}
