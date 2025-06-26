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
package cz.incad.kramerius.rest.oai.metadata.utils;

import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.oai.OAIRecord;
import cz.incad.kramerius.rest.oai.metadata.decorators.DecoratorsChain;
import cz.incad.kramerius.security.licenses.impl.embedded.cz.CzechEmbeddedLicenses;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class EDMUtils {


    private EDMUtils() {}

    @NotNull
    public static Element createEdmDataElements(
            Configuration configuration,
            String dataProvider,
            String dataProviderBaseUrl,
            List<String> licenses,
            Instances instances,
            Document owningDocument,
            OAIRecord oaiRec,
            InputStream directStreamDC,
            String pid,
            /* pid prvni stranky */
            String baseUrl) throws ParserConfigurationException, SAXException, IOException {



        // direct dc stream
        Document dc = XMLUtils.parseDocument(directStreamDC, true);
        // decorate; add default language and type
        dc = new DecoratorsChain().decorate(dc);

        Element dcElement = dc.getDocumentElement();

        Element rdf = owningDocument.createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:RDF");
        rdf.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", oaiRec.getIdentifier());

        Element providedCHO = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:ProvidedCHO");
        providedCHO.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", oaiRec.getIdentifier());
        rdf.appendChild(providedCHO);

        List<Element> elements = XMLUtils.getElements(dcElement);
        elements.stream().forEach(dcElm -> {
            owningDocument.adoptNode(dcElm);
            providedCHO.appendChild(dcElm);
        });

        Element type = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:type");
        providedCHO.appendChild(type);
        type.setTextContent("TEXT");

        // image - source library
        Element webresource = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:WebResource");
        rdf.appendChild(webresource);
        OneInstance oneInstance = instances.find(dataProvider);
        OneInstance.InstanceType instType = oneInstance.getInstanceType();
        switch (instType) {
            case V7:
                webresource.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", String.format("%s/api/client/v7.0/items/%s/image/preview", dataProviderBaseUrl, pid));
                break;
            case V5:
                webresource.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", String.format("%s/api/v5.0/items/%s/preview", dataProviderBaseUrl, pid));
                break;
        }

        Element edmAggregation = owningDocument.createElementNS("http://www.openarchives.org/ore/terms/", "ore:Aggregation");
        String clientUrl = configuration.getString("client");
        if (clientUrl != null) {
            edmAggregation.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", clientUrl + (clientUrl.endsWith("/") ? "" : "/") + "uuid/" + pid);
        } else {
            edmAggregation.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "/uuid/" + pid);
        }

        Element edmAggregatedCHO = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:aggregatedCHO");
        edmAggregatedCHO.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource",  oaiRec.getIdentifier());
        edmAggregation.appendChild(edmAggregatedCHO);

        Element edmDataPrvovider = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:dataProvider");
        edmDataPrvovider.setTextContent(dataProvider);

        // find data provider by acronym
        String acronym = configuration.getString("acronym", "");

        edmAggregation.appendChild(edmDataPrvovider);

        Element shownAt = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:isShownAt");
        if (clientUrl != null) {
            shownAt.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", clientUrl + (clientUrl.endsWith("/") ? "" : "/") + "uuid/" + pid);
        } else {
            shownAt.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "/uuid/" + pid);
        }
        edmAggregation.appendChild(shownAt);

        // mapovani na licence
        Element edmRights = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:rights");

        if (licenses != null && !licenses.isEmpty()) {
            if (licenses.contains(CzechEmbeddedLicenses.PUBLIC_LICENSE.getName())) {
                edmRights.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", "http://creativecommons.org/publicdomain/mark/1.0/");
            } else {
                edmRights.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", "http://rightsstatements.org/vocab/InC/1.0/");
            }
        }
        edmAggregation.appendChild(edmRights);
        Element edmObject = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:object");
        switch (instType) {
            case V7:
                // tady by mela letet prvni stranka
                edmObject.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", String.format("%s/api/client/v7.0/items/%s/image/full", dataProviderBaseUrl, pid));
                break;
            case V5:
                edmObject.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", String.format("%s/api/v5.0/items/%s/streams/FULL_IMG", dataProviderBaseUrl, pid));
                break;
        }
        edmAggregation.appendChild(edmObject);

        // ceska digitalni kniovna
        Element edmProvider = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:provider");
        String edmProviderText = configuration.getString("oai.set.edm.provider", acronym);
        edmProvider.setTextContent(edmProviderText); //"Czech digital library/Česká digitální knihovna");
        edmAggregation.appendChild(edmProvider);
        rdf.appendChild(edmAggregation);
        return rdf;
    }
}
