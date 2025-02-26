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

import java.io.IOException;
import java.util.Arrays;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.tuple.Pair;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.core.repository.KnownDatastreams;
import org.ceskaexpedice.akubra.core.repository.KnownRelations;
import org.ceskaexpedice.akubra.core.repository.RepositoryNamespaces;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.akubra.utils.RelsExtUtils;
import org.ceskaexpedice.akubra.utils.pid.LexerException;
import org.ceskaexpedice.akubra.utils.pid.PIDParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import cz.incad.kramerius.rest.oai.metadata.DrKrameriusUtils;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;

import javax.inject.Provider;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.cdk.CDKUtils;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerException;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.IPAddressUtils;

public enum MetadataExport {


    oaiDc("oai_dc",
            "http://www.openarchives.org/OAI/2.0/oai_dc.xsd",
            "http://www.openarchives.org/OAI/2.0/oai_dc/") {

        //public abstract Element perform(HttpServletRequest request, ProxyItemHandler handler, Document owningDocument, String oaiIdentifier, OAISet set);

        @Override
        public Element perform(HttpServletRequest request, AkubraRepository akubraRepository, Document owningDocument, String oaiIdentifier, OAISet set) {
            String pid = OAITools.pidFromOAIIdentifier(oaiIdentifier);
            InputStream inputStream = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_DC);
            Document dc = DomUtils.streamToDocument(inputStream);
            if (dc != null) {
                Element rootElement = dc.getDocumentElement();
                owningDocument.adoptNode(rootElement);
                return rootElement;
            } else return null;
        }

        public Element performOnCDKSide(SolrAccess solrAccess, Provider<User> userProvider, Provider<Client> clientProvider, Instances instances, HttpServletRequest request, Document owningDocument, OAIRecord oaiRec, OAISet set) {
            try {
                String pid = OAITools.pidFromOAIIdentifier(oaiRec.getIdentifier());
                ProxyItemHandler redirectHandler = findRedirectHandler(solrAccess, userProvider, clientProvider, instances, request, pid, null);
                if (redirectHandler != null) {
                    InputStream directStreamDC = redirectHandler.directStreamDC();
                    if (directStreamDC != null) {
                        Document dc = DomUtils.streamToDocument(directStreamDC, true);
                        Element rootElement = dc.getDocumentElement();
                        owningDocument.adoptNode(rootElement);
                        return rootElement;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } catch (IOException | LexerException | ProxyHandlerException  e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }
        }
    },


    edm("edm", "http://www.europeana.eu/schemas/ese/", "") {
        @Override
        public Element perform(HttpServletRequest request, AkubraRepository akubraRepository, Document owningDocument,
                               String oaiIdentifier, OAISet set) {
            String baseUrl = ApplicationURL.applicationURL(request);
            //rdf:about="uuid:6b182ad3-b9e9-11e1-1726-001143e3f55c"
            String pid = OAITools.pidFromOAIIdentifier(oaiIdentifier);
            InputStream inputStream = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_DC);
            Document dc = DomUtils.streamToDocument(inputStream);
            Element dcElement = dc.getDocumentElement();

            Element metadata = owningDocument.createElement("metadata");
            metadata.setAttribute("xmlns:europeana", "http://www.europeana.eu/schemas/ese/");
            metadata.setAttribute("xmlns:ore", "http://www.openarchives.org/ore/terms/");
            metadata.setAttribute("xmlns:edm", "http://www.europeana.eu/schemas/edm/");
            metadata.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            metadata.setAttribute("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            metadata.setAttribute("xmlns:rdaGr2", "http://rdvocab.info/ElementsGr2/");
            metadata.setAttribute("xmlns:skos", "http://www.w3.org/2004/02/skos/core#");
            metadata.setAttribute("xmlns:oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
            metadata.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
            metadata.setAttribute("xmlns:dcterms", "http://purl.org/dc/terms/");

            Element rdf = owningDocument.createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:RDF");
            rdf.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", oaiIdentifier);
            metadata.appendChild(rdf);

            Element providedCHO = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:ProvidedCHO");
            rdf.appendChild(providedCHO);

            List<Element> elements = DomUtils.getElements(dcElement);
            elements.stream().forEach(dcElm -> {
                owningDocument.adoptNode(dcElm);
                providedCHO.appendChild(dcElm);
            });

            //rdf.appendChild(providedCHO);

            Element type = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:type");
            providedCHO.appendChild(type);
            type.setTextContent("TEXT");

            Element webresource = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:WebResource");
            webresource.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", String.format("%s/api/client/v7.0/items/%s/image", baseUrl, pid));
            metadata.appendChild(webresource);

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

            if (clientUrl != null) {
                edmRights.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", clientUrl + (clientUrl.endsWith("/") ? "" : "/") + "uuid/" + pid);
            } else {
                edmRights.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "/uuid/" + pid);
            }


            edmAggregation.appendChild(edmRights);


            Element edmObject = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:object");
            edmObject.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", String.format("%s/api/client/v7.0/items/%s/image", baseUrl, pid));
            edmAggregation.appendChild(edmObject);


            // ceska digitalni kniovna
            Element edmProvider = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:provider");
            String edmProviderText = KConfiguration.getInstance().getConfiguration().getString("oai.set.edm.provider", acronym);
            edmProvider.setTextContent(edmProviderText); //"Czech digital library/Česká digitální knihovna");
            edmAggregation.appendChild(edmProvider);

            metadata.appendChild(edmAggregation);

            return metadata;
        }

        @Override
        public Element performOnCDKSide(SolrAccess solrAccess, Provider<User> userProvider, Provider<Client> clientProvider, Instances instances, HttpServletRequest request, Document owningDocument, OAIRecord oaiRec, OAISet set) {
            try {

                String baseUrl = ApplicationURL.applicationURL(request);
                //rdf:about="uuid:6b182ad3-b9e9-11e1-1726-001143e3f55c"
                String pid = OAITools.pidFromOAIIdentifier(oaiRec.getIdentifier());

                ProxyItemHandler redirectHandler = findRedirectHandler(solrAccess, userProvider, clientProvider, instances, request, pid, null);
                if (redirectHandler != null) {
                    InputStream directStreamDC = redirectHandler.directStreamDC();
                    if (directStreamDC != null) {
                        Document dc = DomUtils.streamToDocument(directStreamDC, true);
                        Element dcElement = dc.getDocumentElement();

                        Element metadata = owningDocument.createElement("metadata");
                        metadata.setAttribute("xmlns:europeana", "http://www.europeana.eu/schemas/ese/");
                        metadata.setAttribute("xmlns:ore", "http://www.openarchives.org/ore/terms/");
                        metadata.setAttribute("xmlns:edm", "http://www.europeana.eu/schemas/edm/");
                        metadata.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                        metadata.setAttribute("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
                        metadata.setAttribute("xmlns:rdaGr2", "http://rdvocab.info/ElementsGr2/");
                        metadata.setAttribute("xmlns:skos", "http://www.w3.org/2004/02/skos/core#");
                        metadata.setAttribute("xmlns:oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
                        metadata.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
                        metadata.setAttribute("xmlns:dcterms", "http://purl.org/dc/terms/");

                        Element rdf = owningDocument.createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:RDF");
                        rdf.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", oaiRec.getIdentifier());
                        metadata.appendChild(rdf);

                        Element providedCHO = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:ProvidedCHO");
                        rdf.appendChild(providedCHO);

                        List<Element> elements = DomUtils.getElements(dcElement);
                        elements.stream().forEach(dcElm -> {
                            owningDocument.adoptNode(dcElm);
                            providedCHO.appendChild(dcElm);
                        });

                        //rdf.appendChild(providedCHO);

                        Element type = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:type");
                        providedCHO.appendChild(type);
                        type.setTextContent("TEXT");

                        Element webresource = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:WebResource");
                        webresource.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", String.format("%s/api/client/v7.0/items/%s/image", baseUrl, pid));
                        metadata.appendChild(webresource);

                        Element edmAggregation = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:Aggregation");
                        String clientUrl = KConfiguration.getInstance().getConfiguration().getString("client");
                        if (clientUrl != null) {
                            edmAggregation.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", clientUrl + (clientUrl.endsWith("/") ? "" : "/") + "uuid/" + pid);
                        } else {
                            edmAggregation.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about", baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "/uuid/" + pid);
                        }
                        Element edmDataPrvovider = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:dataProvider");

                        // Data provider
                        // find data provider by acronym
                        String acronym = KConfiguration.getInstance().getConfiguration().getString("acronym", "");
                        String defaultEDMDataProvider = KConfiguration.getInstance().getConfiguration().getString("oai.set.edm.dataProvider", acronym);
//                        if (edmDataProvider != null) {
//                            edmDataPrvovider.setTextContent(edmDataProvider);
//                        }
                        metadataProvider(instances, oaiRec, edmDataPrvovider, defaultEDMDataProvider);
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

                        if (clientUrl != null) {
                            edmRights.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", clientUrl + (clientUrl.endsWith("/") ? "" : "/") + "uuid/" + pid);
                        } else {
                            edmRights.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "/uuid/" + pid);
                        }


                        edmAggregation.appendChild(edmRights);


                        Element edmObject = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:object");
                        edmObject.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", String.format("%s/api/client/v7.0/items/%s/image", baseUrl, pid));
                        edmAggregation.appendChild(edmObject);


                        // ceska digitalni kniovna 
                        Element edmProvider = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:provider");
                        String edmProviderText = KConfiguration.getInstance().getConfiguration().getString("oai.set.edm.provider", acronym);
                        edmProvider.setTextContent(edmProviderText); //"Czech digital library/Česká digitální knihovna");
                        edmAggregation.appendChild(edmProvider);

                        metadata.appendChild(edmAggregation);

                        return metadata;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } catch (IOException | LexerException | ProxyHandlerException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }
        }


    },


    ese("ese",
            "http://www.europeana.eu/schemas/ese/ESE-V3.2.xsd",
            "http://www.europeana.eu/schemas/ese/") {
        @Override
        public Element perform(HttpServletRequest request, AkubraRepository akubraRepository, Document owningDocument, String oaiIdentifier, OAISet set) {
            String baseUrl = ApplicationURL.applicationURL(request);
            String pid = OAITools.pidFromOAIIdentifier(oaiIdentifier);

            InputStream inputStream = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_DC);
            Document dc = DomUtils.streamToDocument(inputStream);

            Element dcElement = dc.getDocumentElement();

            Element record = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "record");
            record.setAttribute("xmlns:dc", RepositoryNamespaces.DC_NAMESPACE_URI);

            record.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "http://www.europeana.eu/schemas/ese/ http://www.europeana.eu/schemas/ese/ESE-V3.2.xsd http://purl.org/dc/elements/1.1/ http://www.dublincore.org/schemas/xmls/qdc/dc.xsd http://purl.org/dc/terms/ http://www.dublincore.org/schemas/xmls/qdc/dcterms.xsd http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
            List<Element> dcElems = DomUtils.getElements(dcElement);
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


            return record;
        }


        @Override
        public Element performOnCDKSide(SolrAccess solrAccess, Provider<User> userProvider, Provider<Client> clientProvider, Instances instances, HttpServletRequest request, Document owningDocument, OAIRecord oaiRec, OAISet set) {
            try {
                String baseUrl = ApplicationURL.applicationURL(request);
                String pid = OAITools.pidFromOAIIdentifier(oaiRec.getIdentifier());
                ProxyItemHandler redirectHandler = findRedirectHandler(solrAccess, userProvider, clientProvider, instances, request, pid, null);
                if (redirectHandler != null) {
                    InputStream directStreamDC = redirectHandler.directStreamDC();
                    if (directStreamDC != null) {
                        Document dc = DomUtils.streamToDocument(directStreamDC, true);
                        Element dcElement = dc.getDocumentElement();

                        Element record = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "record");
                        record.setAttribute("xmlns:dc", RepositoryNamespaces.DC_NAMESPACE_URI);

                        record.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "http://www.europeana.eu/schemas/ese/ http://www.europeana.eu/schemas/ese/ESE-V3.2.xsd http://purl.org/dc/elements/1.1/ http://www.dublincore.org/schemas/xmls/qdc/dc.xsd http://purl.org/dc/terms/ http://www.dublincore.org/schemas/xmls/qdc/dcterms.xsd http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
                        List<Element> dcElems = DomUtils.getElements(dcElement);
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

                        metadataProvider(instances, oaiRec, provider, defaultDataProvider);

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


                        return record;
                    } else return null;

                } else {
                    return null;
                }
            } catch (IOException | LexerException | ProxyHandlerException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }
        }
    },

    drkramerius4("drkramerius4",
            "http://registrdigitalizace.cz/schemas/drkramerius/v4/drkram.xsd",
            "http://registrdigitalizace.cz/schemas/drkramerius/v4/") {
        @Override
        public Element performOnCDKSide(SolrAccess solrAccess, Provider<User> userProvider,
                                        Provider<Client> clientProvider, Instances instances, HttpServletRequest request,
                                        Document owningDocument, OAIRecord oaiRec, OAISet set) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Element perform(HttpServletRequest request, AkubraRepository akubraRepository, Document owningDocument,
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
                InputStream inputStream = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT);
                Document relsExt = DomUtils.streamToDocument(inputStream);
                String model = RelsExtUtils.getModel(relsExt.getDocumentElement());

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

                inputStream = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_MODS);
                Document biblio = DomUtils.streamToDocument(inputStream);
                Element biblioRoots = (Element) owningDocument.adoptNode(biblio.getDocumentElement());
                drDescriptor.appendChild(biblioRoots);

                List<Pair<String, String>> relations = RelsExtUtils.getRelations(relsExt.getDocumentElement());
                for (Pair<String, String> relation : relations) {
                    if (!excludeModels.contains(relation.getLeft())) {
                        Element drRelation = owningDocument.createElementNS(DrKrameriusUtils.DR_NS_URI, "dr:relation");
                        String relationPid = relation.getRight();
                        PIDParser pidParser = new PIDParser(relationPid);
                        pidParser.objectPid();
                        drRelation.setTextContent(pidParser.getObjectId());
                        record.appendChild(drRelation);
                    }
                }
                record.appendChild(drDescriptor);
                return record;

            } catch (LexerException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }

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

    //TODO: Remove
//    private static InputStream dcStream(SolrAccess solrAccess, Provider<User> userProvider,
//            Provider<Client> clientProvider, Instances instances, HttpServletRequest request, String pid)
//            throws LexerException, IOException, ProxyHandlerException {
//        ProxyItemHandler redirectHandler = findRedirectHandler(solrAccess, userProvider, clientProvider, instances, request, pid, null);
//        InputStream directStreamDC = redirectHandler.directStreamDC();
//        return directStreamDC;
//    }


    private String metadataPrefix;
    private String schema;
    private String metadataNamespace;
    
    

    /*
     *     @Inject
    @Named("forward-client")
    Provider<Client> clientProvider;

     */

    public static ProxyItemHandler findRedirectHandler(SolrAccess solrAccess, Provider<User> userProvider, Provider<Client> clientProvider, Instances instances, HttpServletRequest request, String pid, String source) throws LexerException, IOException {
        if (source == null) {
            source = defaultDocumentSource(solrAccess, pid);
        }
        LOGGER.info("Info - default source " + source);
        OneInstance found = instances.find(source);
        if (found != null) {
            String remoteAddress = IPAddressUtils.getRemoteAddress(request, KConfiguration.getInstance().getConfiguration());
            ProxyItemHandler proxyHandler = found.createProxyItemHandler(userProvider.get(), clientProvider.get(), solrAccess, source, pid, remoteAddress);
            return proxyHandler;
        } else {
            return null;
        }
    }

    private static String defaultDocumentSource(SolrAccess solrAccess, String pid) throws IOException {
        org.w3c.dom.Document solrDataByPid = solrAccess.getSolrDataByPid(pid);
        String leader = CDKUtils.findCDKLeader(solrDataByPid.getDocumentElement());
        List<String> sources = CDKUtils.findSources(solrDataByPid.getDocumentElement());
        return leader != null ? leader : (!sources.isEmpty() ? sources.get(0) : null);
    }


    private static void metadataProvider(Instances instances, OAIRecord oaiRec, Element provider, String defaultValue) {
        List<String> cdkCollections = oaiRec.getCdkCollections();
        if (cdkCollections.size() > 0) {
            String acronym = cdkCollections.get(0);
            OneInstance found = instances.find(acronym.trim());
            if (found != null) {
                provider.setTextContent(found.getRegistrInfo().get(OneInstance.NAME_ENG));
            } else {
                provider.setTextContent(acronym.trim());
            }

        } else {
            provider.setTextContent(defaultValue);
        }
    }

    /** CDK side */
    public abstract Element performOnCDKSide(SolrAccess solrAccess, Provider<User> userProvider, Provider<Client> clientProvider, Instances instances, HttpServletRequest request, Document owningDocument, OAIRecord oaiRec, OAISet set);

    /** Local kramerius */
    public abstract Element perform(HttpServletRequest request, AkubraRepository akubraRepository, Document owningDocument, String oaiIdentifier, OAISet set);

    //public abstract Element perform(HttpServletRequest request, ProxyItemHandler handler, Document owningDocument, String oaiIdentifier, OAISet set);

    /*
     * ProxyItemHandler handler,
     */
//    public static final Logger LOGGER = Logger.getLogger(MetadataExport.class.getName());

}
