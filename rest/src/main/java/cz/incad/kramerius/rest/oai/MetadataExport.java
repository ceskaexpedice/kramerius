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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.security.licenses.impl.embedded.cz.CzechEmbeddedLicenses;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import cz.inovatika.cdk.cache.impl.CDKRequestItemFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.ceskaexpedice.akubra.pid.LexerException;
import org.ceskaexpedice.akubra.pid.PIDParser;
import org.ceskaexpedice.akubra.relsext.KnownRelations;
import org.ceskaexpedice.akubra.relsext.RelsExtRelation;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import cz.incad.kramerius.rest.oai.metadata.DrKrameriusUtils;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;

import javax.inject.Provider;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.cdk.CDKUtils;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerException;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.XMLUtils;

public enum MetadataExport {


    oaiDc("oai_dc",
            "http://www.openarchives.org/OAI/2.0/oai_dc.xsd",
            "http://www.openarchives.org/OAI/2.0/oai_dc/") {

        //public abstract Element perform(HttpServletRequest request, ProxyItemHandler handler, Document owningDocument, String oaiIdentifier, OAISet set);

        @Override
        public List<Element> perform(HttpServletRequest request, AkubraRepository akubraRepository, Document owningDocument, String oaiIdentifier, OAISet set) {
            String pid = OAITools.pidFromOAIIdentifier(oaiIdentifier);
            Document dc = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_DC).asDom(false);
            if (dc != null) {
                Element rootElement = dc.getDocumentElement();
                owningDocument.adoptNode(rootElement);
                return Arrays.asList(rootElement);
            } else return null;
        }

        @Override
        public List<Element> performOnCDKSide(SolrAccess solrAccess, Provider<User> userProvider, Provider<CloseableHttpClient> apacheClientProvider, Instances instances, HttpServletRequest request, Document owningDocument, OAIRecord oaiRec, OAISet set, CDKRequestCacheSupport cacheSupport) {
            try {
                String pid = OAITools.pidFromOAIIdentifier(oaiRec.getIdentifier());
                org.w3c.dom.Document solrDataByPid = solrAccess.getSolrDataByPid(pid);
                ProxyItemHandler redirectHandler = findRedirectHandler(solrDataByPid, solrAccess, userProvider, apacheClientProvider, instances, request, pid, null);

                if (redirectHandler != null) {
                    String baseUrl = ApplicationURL.applicationURL(request);
                    InputStream directStreamDC = null;
                    String cacheURl = baseUrl + "/dc";
                    CDKRequestItem hit = cacheSearchHitByPid(cacheURl, pid, cacheSupport);
                    if (hit != null) {
                        directStreamDC = new ByteArrayInputStream(hit.getData().toString().getBytes(Charset.forName("UTF-8")));
                    } else {
                        InputStream dc = redirectHandler.directStreamDC(null);
                        String remoteData = IOUtils.toString(dc, "UTF-8");
                        saveToCache(remoteData, cacheURl, pid, cacheSupport);
                        directStreamDC = new ByteArrayInputStream(remoteData.getBytes("UTF-8"));
                    }

                    if (directStreamDC != null) {
                        Document dc = DomUtils.streamToDocument(directStreamDC, true);
                        Element rootElement = dc.getDocumentElement();
                        owningDocument.adoptNode(rootElement);
                        return Arrays.asList(rootElement);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }
        }

        @Override
        public boolean isAvailableOnLocal() {
            return true;
        }

        @Override
        public boolean isAvailableOnCDKSide() {
            return true;
        }
    },


    edm("edm", "http://www.europeana.eu/schemas/ese/", "") {
        @Override
        public List<Element> perform(HttpServletRequest request, AkubraRepository akubraRepository, Document owningDocument,
                                     String oaiIdentifier, OAISet set) {

            String baseUrl = ApplicationURL.applicationURL(request);
            String pid = OAITools.pidFromOAIIdentifier(oaiIdentifier);
            Document dc = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_DC).asDom(false);
            Element dcElement = dc.getDocumentElement();

            Element rdf = owningDocument.createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:RDF");
            rdf.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", oaiIdentifier);

            Element providedCHO = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:ProvidedCHO");
            rdf.appendChild(providedCHO);

            List<Element> elements = XMLUtils.getElements(dcElement);
            elements.stream().forEach(dcElm -> {
                owningDocument.adoptNode(dcElm);
                providedCHO.appendChild(dcElm);
            });


            Element type = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:type");
            providedCHO.appendChild(type);
            type.setTextContent("TEXT");

            Element webresource = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:WebResource");
            webresource.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", String.format("%s/api/client/v7.0/items/%s/image", baseUrl, pid));
            rdf.appendChild(webresource);


            Element edmAggregation = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:Aggregation");
            String clientUrl = KConfiguration.getInstance().getConfiguration().getString("client");
            if (clientUrl != null) {
                edmAggregation.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", clientUrl + (clientUrl.endsWith("/") ? "" : "/") + "uuid/" + pid);
            } else {
                edmAggregation.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "/uuid/" + pid);
            }
            Element edmDataPrvovider = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:dataProvider");

            // Data provider
            String acronym = KConfiguration.getInstance().getConfiguration().getString("acronym", "");
            String edmDataProvider = KConfiguration.getInstance().getConfiguration().getString("oai.set.edm.dataProvider", acronym);
            if (edmDataProvider != null) {
                edmDataPrvovider.setTextContent(edmDataProvider);
            }
            edmAggregation.appendChild(edmDataPrvovider);

            // dodat dle setu
            Element shownAt = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:isShownAt");
            if (clientUrl != null) {
                shownAt.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", clientUrl + (clientUrl.endsWith("/") ? "" : "/") + "uuid/" + pid);
            } else {
                shownAt.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "/uuid/" + pid);
            }
            edmAggregation.appendChild(shownAt);

            // mapovani na licence
            Element edmRights = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:rights");
                /*
                if (clientUrl != null) {
                    edmRights.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", clientUrl+(clientUrl.endsWith("/") ? "" : "/")+"uuid/"+pid);
                } else {
                    edmRights.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", baseUrl+(baseUrl.endsWith("/") ? "" : "/")+"/uuid/"+pid);
                }*/

            edmAggregation.appendChild(edmRights);


            Element edmObject = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:object");
            // find first page
            edmObject.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", String.format("%s/api/client/v7.0/items/%s/image", baseUrl, pid));
            edmAggregation.appendChild(edmObject);


            // ceska digitalni kniovna
            Element edmProvider = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:provider");
            String edmProviderText = KConfiguration.getInstance().getConfiguration().getString("oai.set.edm.provider", acronym);
            edmProvider.setTextContent(edmProviderText); //"Czech digital library/Česká digitální knihovna");
            edmAggregation.appendChild(edmProvider);
            rdf.appendChild(edmAggregation);

            return Arrays.asList(rdf);
        }

        @Override
        public List<Element> performOnCDKSide(SolrAccess solrAccess, Provider<User> userProvider, Provider<CloseableHttpClient> apacheClientProvider, Instances instances, HttpServletRequest request, Document owningDocument, OAIRecord oaiRec, OAISet set, CDKRequestCacheSupport cacheSupport) {
            try {

                String baseUrl = ApplicationURL.applicationURL(request);
                // base api url - it is different from standard base url
                String pid = OAITools.pidFromOAIIdentifier(oaiRec.getIdentifier());

                org.w3c.dom.Document solrDataByPid = solrAccess.getSolrDataByPid(pid);
                ProxyItemHandler redirectHandler = findRedirectHandler(solrDataByPid,solrAccess, userProvider, apacheClientProvider, instances, request, pid, null);
                if (redirectHandler != null) {

                    InputStream directStreamDC = null;
                    String cacheURl = baseUrl + "/dc";

                    CDKRequestItem hit = cacheSearchHitByPid(cacheURl, pid, cacheSupport);
                    if (hit != null) {
                        directStreamDC = new ByteArrayInputStream(hit.getData().toString().getBytes(Charset.forName("UTF-8")));
                    } else {
                        InputStream dc = redirectHandler.directStreamDC(null);
                        String remoteData = IOUtils.toString(dc, "UTF-8");
                        saveToCache(remoteData, cacheURl, pid, cacheSupport);
                        directStreamDC = new ByteArrayInputStream(remoteData.getBytes("UTF-8"));
                    }

                    if (directStreamDC != null) {
                        Document dc = XMLUtils.parseDocument(directStreamDC, true);
                        Element dcElement = dc.getDocumentElement();

                        Element rdf = owningDocument.createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:RDF");
                        rdf.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", oaiRec.getIdentifier());
                        //metadata.appendChild(rdf);

                        Element providedCHO = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:ProvidedCHO");
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
                        String dataProvider = MetadataExport.findMetadataProvider(solrDataByPid);
                        String dataProviderBaseUrl = KConfiguration.getInstance().getConfiguration().getString(String.format("cdk.collections.sources.%s.baseurl", dataProvider));
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


                        Element edmAggregation = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:Aggregation");
                        String clientUrl = KConfiguration.getInstance().getConfiguration().getString("client");
                        if (clientUrl != null) {
                            edmAggregation.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", clientUrl + (clientUrl.endsWith("/") ? "" : "/") + "uuid/" + pid);
                        } else {
                            edmAggregation.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "/uuid/" + pid);
                        }
                        Element edmDataPrvovider = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:dataProvider");
                        edmDataPrvovider.setTextContent(dataProvider);
                        // Data provider
                        // find data provider by acronym
                        String acronym = KConfiguration.getInstance().getConfiguration().getString("acronym", "");

                        edmAggregation.appendChild(edmDataPrvovider);

                        // klient api + obrazek z klient api + redirect - na prvni stranku z headu ??
                        Element shownAt = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:isShownAt");
                        if (clientUrl != null) {
                            shownAt.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", clientUrl + (clientUrl.endsWith("/") ? "" : "/") + "uuid/" + pid);
                        } else {
                            shownAt.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "/uuid/" + pid);
                        }
                        edmAggregation.appendChild(shownAt);

                        // mapovani na licence
                        Element edmRights = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:rights");
                        Element licensesElm = XMLUtils.findElement(solrDataByPid.getDocumentElement(), new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                if (element.hasAttribute("name")) {
                                    String name = element.getAttribute("name");
                                    if (name.equals("licenses")) return true;
                                }
                                return false;
                            }
                        });

                        if (licensesElm != null) {
                            List<Element> elms = XMLUtils.getElements(licensesElm);
                            List<String> licenses = elms.stream().map(Element::getTextContent).collect(Collectors.toList());
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
                                edmObject.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", String.format("%s/api/client/v7.0/items/%s/image/thumb", dataProviderBaseUrl, pid));
                                break;
                            case V5:
                                edmObject.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", String.format("%s/api/v5.0/items/%s/thumb", dataProviderBaseUrl, pid));
                                break;
                        }
                        edmAggregation.appendChild(edmObject);


                        // ceska digitalni kniovna 
                        Element edmProvider = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:provider");
                        String edmProviderText = KConfiguration.getInstance().getConfiguration().getString("oai.set.edm.provider", acronym);
                        edmProvider.setTextContent(edmProviderText); //"Czech digital library/Česká digitální knihovna");
                        edmAggregation.appendChild(edmProvider);
                        rdf.appendChild(edmAggregation);

                        //return Arrays.asList(rdf ,webresource, edmAggregation);
                        return Arrays.asList(rdf);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }
        }

        @Override
        public boolean isAvailableOnLocal() {
            return false;
        }

        @Override
        public boolean isAvailableOnCDKSide() {
            return true;
        }
    },


    ese("ese",
            "http://www.europeana.eu/schemas/ese/ESE-V3.2.xsd",
            "http://www.europeana.eu/schemas/ese/") {
        @Override
        public List<Element> perform(HttpServletRequest request, AkubraRepository akubraRepository, Document owningDocument, String oaiIdentifier, OAISet set) {
            String baseUrl = ApplicationURL.applicationURL(request);
            String pid = OAITools.pidFromOAIIdentifier(oaiIdentifier);


            Document dc = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_DC).asDom(false);

            Element dcElement = dc.getDocumentElement();

            Element record = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "record");
            record.setAttribute("xmlns:dc", RepositoryNamespaces.DC_NAMESPACE_URI);

            record.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "http://www.europeana.eu/schemas/ese/ http://www.europeana.eu/schemas/ese/ESE-V3.2.xsd http://purl.org/dc/elements/1.1/ http://www.dublincore.org/schemas/xmls/qdc/dc.xsd http://purl.org/dc/terms/ http://www.dublincore.org/schemas/xmls/qdc/dcterms.xsd http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
            List<Element> dcElems = XMLUtils.getElements(dcElement);
            dcElems.stream().forEach(dcElm -> {
                owningDocument.adoptNode(dcElm);
                record.appendChild(dcElm);
            });

            Element object = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "object");
            String thumb = String.format(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/%s/image/thumb", pid);
            object.setTextContent(thumb);
            record.appendChild(object);

            Element provider = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "provider");
            provider.setTextContent("Academy of Sciences Library"); //TODO: To configuration
            record.appendChild(provider);

            Element type = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "type");
            type.setTextContent("TEXT");
            record.appendChild(type);

            Element isShownAt = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "isShownAt");
            String clientUrl = KConfiguration.getInstance().getConfiguration().getString("client");
            if (clientUrl != null) {
                isShownAt.setTextContent(clientUrl + (clientUrl.endsWith("/") ? "" : "/") + "uuid/" + pid);
            } else {
                isShownAt.setTextContent(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "/uuid/" + pid);
            }
            record.appendChild(isShownAt);
            //store
            return Arrays.asList(record);

        }


        @Override
        public List<Element> performOnCDKSide(SolrAccess solrAccess, Provider<User> userProvider, Provider<CloseableHttpClient> apacheClientProvider, Instances instances, HttpServletRequest request, Document owningDocument, OAIRecord oaiRec, OAISet set, CDKRequestCacheSupport cacheSupport) {
            try {
                String baseUrl = ApplicationURL.applicationURL(request);
                String pid = OAITools.pidFromOAIIdentifier(oaiRec.getIdentifier());
                org.w3c.dom.Document solrDataByPid = solrAccess.getSolrDataByPid(pid);
                ProxyItemHandler redirectHandler = findRedirectHandler(solrDataByPid,solrAccess, userProvider, apacheClientProvider, instances, request, pid, null);

                if (redirectHandler != null) {
                    String cacheURl = baseUrl + "/dc";

                    CDKRequestItem hit = cacheSearchHitByPid(cacheURl, pid, cacheSupport);
                    InputStream directStreamDC = null;
                    if (hit != null) {
                        directStreamDC = new ByteArrayInputStream(hit.getData().toString().getBytes(Charset.forName("UTF-8")));
                    } else {
                        InputStream dc = redirectHandler.directStreamDC(null);
                        String remoteData = IOUtils.toString(dc, "UTF-8");
                        saveToCache(remoteData, cacheURl, pid, cacheSupport);
                        directStreamDC = new ByteArrayInputStream(remoteData.getBytes("UTF-8"));
                    }
                    if (directStreamDC != null) {
                        Document dc = DomUtils.streamToDocument(directStreamDC, true);
                        Element dcElement = dc.getDocumentElement();

                        Element record = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "record");
                        record.setAttribute("xmlns:dc", RepositoryNamespaces.DC_NAMESPACE_URI);

                        record.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "http://www.europeana.eu/schemas/ese/ http://www.europeana.eu/schemas/ese/ESE-V3.2.xsd http://purl.org/dc/elements/1.1/ http://www.dublincore.org/schemas/xmls/qdc/dc.xsd http://purl.org/dc/terms/ http://www.dublincore.org/schemas/xmls/qdc/dcterms.xsd http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
                        List<Element> dcElems = XMLUtils.getElements(dcElement);
                        dcElems.stream().forEach(dcElm -> {
                            owningDocument.adoptNode(dcElm);
                            record.appendChild(dcElm);
                        });

                        Element object = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "object");
                        String thumb = String.format(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/%s/image/thumb", pid);
                        object.setTextContent(thumb);
                        record.appendChild(object);

                        Element provider = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "provider");
                        String acronym = KConfiguration.getInstance().getConfiguration().getString("acronym", "");
                        String defaultDataProvider = KConfiguration.getInstance().getConfiguration().getString("oai.set.edm.dataProvider", acronym);


                        metadataProvider( solrDataByPid, oaiRec, provider);

                        record.appendChild(provider);

                        Element type = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "type");
                        type.setTextContent("TEXT");
                        record.appendChild(type);

                        Element isShownAt = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "isShownAt");
                        String clientUrl = KConfiguration.getInstance().getConfiguration().getString("client");
                        if (clientUrl != null) {
                            isShownAt.setTextContent(clientUrl + (clientUrl.endsWith("/") ? "" : "/") + "uuid/" + pid);
                        } else {
                            isShownAt.setTextContent(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "/uuid/" + pid);
                        }
                        record.appendChild(isShownAt);

                        return Arrays.asList(record);
                    } else return null;

                } else {
                    return null;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }
        }

        @Override
        public boolean isAvailableOnLocal() {
            return true;
        }

        @Override
        public boolean isAvailableOnCDKSide() {
            return false;
        }
    },

    drkramerius4("drkramerius4",
            "http://registrdigitalizace.cz/schemas/drkramerius/v4/drkram.xsd",
            "http://registrdigitalizace.cz/schemas/drkramerius/v4/") {
        @Override
        public List<Element> performOnCDKSide(SolrAccess solrAccess, Provider<User> userProvider,
                                              Provider<CloseableHttpClient> apacheClientProvider, Instances instances, HttpServletRequest request,
                                              Document owningDocument, OAIRecord oaiRec, OAISet set, CDKRequestCacheSupport cacheSupport) {
            return new ArrayList<>();
        }

        @Override
        public List<Element> perform(HttpServletRequest request, AkubraRepository akubraRepository, Document owningDocument,
                                     String oaiIdentifier, OAISet set) {
            try {

                //hasIntCompPart

                List<String> excludeModels = Lists.transform(KConfiguration.getInstance().getConfiguration().getList("oai.metadata.drkramerius4.excluderelations",
                        Arrays.asList(KnownRelations.HAS_INT_COMP_PART.toString(),
                                KnownRelations.HAS_PAGE.toString())
                ), Functions.toStringFunction());


                String baseUrl = ApplicationURL.applicationURL(request);
                String pid = OAITools.pidFromOAIIdentifier(oaiIdentifier);
                //Document dc = fa.getDC(pid);
                //Element dcElement = dc.getDocumentElement();

                List<String> topLevelModels = Lists.transform(KConfiguration.getInstance().getConfiguration().getList("fedora.topLevelModels"), Functions.toStringFunction());
                String model = akubraRepository.re().getModel(pid);
                Element record = owningDocument.createElementNS(DrKrameriusUtils.DR_NS_URI, "dr:record");
                if (topLevelModels.contains(model)) {
                    record.setAttribute("root", "true");
                }

                Element uuid = owningDocument.createElementNS(DrKrameriusUtils.DR_NS_URI, "dr:uuid");
                uuid.setTextContent(pid.substring("uuid:".length()));
                record.appendChild(uuid);

                Element type = owningDocument.createElementNS(DrKrameriusUtils.DR_NS_URI, "dr:type");
                type.setTextContent(model.toUpperCase());
                record.appendChild(type);

                Element drDescriptor = owningDocument.createElementNS(DrKrameriusUtils.DR_NS_URI, "dr:descriptor");

                Document biblio = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_MODS).asDom(true);
                Element biblioRoots = (Element) owningDocument.adoptNode(biblio.getDocumentElement());
                drDescriptor.appendChild(biblioRoots);

                List<RelsExtRelation> relations = akubraRepository.re().getRelations(pid, null);
                for (RelsExtRelation relation : relations) {
                    if (!excludeModels.contains(relation.getLocalName())) {
                        Element drRelation = owningDocument.createElementNS(DrKrameriusUtils.DR_NS_URI, "dr:relation");
                        String relationPid = relation.getResource();
                        PIDParser pidParser = new PIDParser(relationPid);
                        pidParser.objectPid();
                        drRelation.setTextContent(pidParser.getObjectId());
                        record.appendChild(drRelation);
                    }
                }


                record.appendChild(drDescriptor);
                return Arrays.asList(record);

            } catch (LexerException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }

        }

        @Override
        public boolean isAvailableOnLocal() {
            return true;
        }

        @Override
        public boolean isAvailableOnCDKSide() {
            return false;
        }
    };


    public static final Logger LOGGER = Logger.getLogger(MetadataExport.class.getName());


    private MetadataExport(String metadataPrefix, String schema, String metadataNamespace) {
        this.metadataPrefix = metadataPrefix;
        this.schema = schema;
        this.metadataNamespace = metadataNamespace;
    }

    public String getMetadataNamespace() {
        return metadataNamespace;
    }

    public String getSchema() {
        return schema;
    }

    public String getMetadataPrefix() {
        return metadataPrefix;
    }


    public static MetadataExport findByPrefix(String prefix) {
        MetadataExport[] values = MetadataExport.values();
        for (MetadataExport me : values) {
            if (me.getMetadataPrefix().equals(prefix)) return me;
        }
        return null;
    }

    private String metadataPrefix;
    private String schema;
    private String metadataNamespace;


    public static ProxyItemHandler findRedirectHandler(Document solrByPid, SolrAccess solrAccess, Provider<User> userProvider, Provider<CloseableHttpClient> apacheClient, Instances instances, HttpServletRequest request, String pid, String source) throws LexerException, IOException {
        if (source == null) {
            source = defaultDocumentSource(solrByPid);
        }
        LOGGER.info("Info - default source " + source);
        OneInstance found = instances.find(source);
        if (found != null) {
            String remoteAddress = IPAddressUtils.getRemoteAddress(request, KConfiguration.getInstance().getConfiguration());
            ProxyItemHandler proxyHandler = found.createProxyItemHandler(userProvider.get(), apacheClient.get(), null, solrAccess, source, pid, remoteAddress);
            return proxyHandler;
        } else {
            return null;
        }
    }

    private static String defaultDocumentSource(org.w3c.dom.Document solrDataByPid) throws IOException {
        String leader = CDKUtils.findCDKLeader(solrDataByPid.getDocumentElement());
        List<String> sources = CDKUtils.findSources(solrDataByPid.getDocumentElement());
        return leader != null ? leader : (!sources.isEmpty() ? sources.get(0) : null);
    }


    private static void metadataProvider( Document solrDataByPid, OAIRecord oaiRec, Element provider) {
        String foundMetadataProvider = findMetadataProvider(solrDataByPid);
        if (foundMetadataProvider != null) {
            provider.setTextContent(foundMetadataProvider);
        }
    }




    /** CDK side */
    public abstract List<Element> performOnCDKSide(
            SolrAccess solrAccess,
            Provider<User> userProvider,
            Provider<CloseableHttpClient> apacheClientProvider,
            Instances instances,
            HttpServletRequest request,
            Document owningDocument,
            OAIRecord oaiRec, OAISet set, CDKRequestCacheSupport cacheSupport);

    /** Local kramerius */
    public abstract List<Element> perform(HttpServletRequest request, AkubraRepository akubraRepository, Document owningDocument, String oaiIdentifier, OAISet set);

    public abstract boolean isAvailableOnCDKSide();

    public abstract boolean isAvailableOnLocal();


    protected void saveToCache(String data, String url, String pid, CDKRequestCacheSupport cacheSupport) {
        try {
            CDKRequestItem<String> cacheItem = (CDKRequestItem<String>) CDKRequestItemFactory.createCacheItem(
                    data,
                    "text/xml",
                    url,
                    pid,
                    null,
                    LocalDateTime.now(),
                    null
            );

            LOGGER.info(String.format("Storing cache item %s", cacheItem.toString()));
            cacheSupport.save(cacheItem);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }


    protected CDKRequestItem cacheSearchHitByPid(String url, String pid, CDKRequestCacheSupport cacheSupport) {
        int days = KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.item", 30);
        LOGGER.log(Level.INFO, String.format("this.cacheSupport.find(\"%s\", \"%s\",\"%s\", \"%s\")", null, url, pid, null));
        List<CDKRequestItem> cdkRequestItems = cacheSupport.find(null, url, pid, null);
        if (!cdkRequestItems.isEmpty() && !cdkRequestItems.get(0).isExpired(days)) {
            LOGGER.log(Level.INFO, String.format("this.cacheSupport.found(\"%s\", \"%s\",\"%s\", \"%s\")", null, url, pid, null));
            return cdkRequestItems.get(0);
        }
        return null;
    }

    private static String findMetadataProvider(Document solrDataByPid) {
        Element cdkCollection = XMLUtils.findElement(solrDataByPid.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                if (element.hasAttribute("name") && element.getAttribute("name").equals("cdk.collection")) {
                    return true;
                }
                return false;
            }
        });
        String foundMetadataProvider = null;
        if (cdkCollection != null) {
            List<Element> elms = XMLUtils.getElements(cdkCollection);
            if (elms.size() > 0) {
                foundMetadataProvider = elms.get(0).getTextContent();
            }
        }
        return foundMetadataProvider;
    }

}
