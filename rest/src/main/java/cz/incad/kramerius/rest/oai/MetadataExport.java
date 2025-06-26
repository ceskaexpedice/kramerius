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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.rest.oai.metadata.utils.EDMUtils;
import cz.incad.kramerius.rest.oai.metadata.utils.OAICDKUtils;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import org.apache.commons.io.IOUtils;
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

import cz.incad.kramerius.rest.oai.metadata.utils.DrKrameriusUtils;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;

import javax.inject.Provider;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.XMLUtils;

public enum MetadataExport {


    oaiDc("oai_dc",
            "http://www.openarchives.org/OAI/2.0/oai_dc.xsd",
            "http://www.openarchives.org/OAI/2.0/oai_dc/") {

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
                ProxyItemHandler redirectHandler = OAICDKUtils.findRedirectHandler(solrDataByPid, solrAccess, userProvider, apacheClientProvider, instances, request, pid, null);

                if (redirectHandler != null) {
                    String baseUrl = ApplicationURL.applicationURL(request);
                    InputStream directStreamDC = null;
                    String cacheURl = baseUrl + "/dc";
                    CDKRequestItem hit = OAICDKUtils.cacheSearchHitByPid(cacheURl, pid, cacheSupport);
                    if (hit != null) {
                        directStreamDC = new ByteArrayInputStream(hit.getData().toString().getBytes(Charset.forName("UTF-8")));
                    } else {
                        InputStream dc = redirectHandler.directStreamDC(null);
                        String remoteData = IOUtils.toString(dc, "UTF-8");
                        OAICDKUtils.saveToCache(remoteData, cacheURl, pid, cacheSupport);
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

            throw new UnsupportedOperationException("unsupported on local instance");
        }

        @Override
        public List<Element> performOnCDKSide(SolrAccess solrAccess, Provider<User> userProvider, Provider<CloseableHttpClient> apacheClientProvider, Instances instances, HttpServletRequest request, Document owningDocument, OAIRecord oaiRec, OAISet set, CDKRequestCacheSupport cacheSupport) {
            try {



                String baseUrl = ApplicationURL.applicationURL(request);
                // base api url - it is different from standard base url
                String pid = OAITools.pidFromOAIIdentifier(oaiRec.getIdentifier());

                org.w3c.dom.Document solrDataByPid = solrAccess.getSolrDataByPid(pid);
                ProxyItemHandler redirectHandler = OAICDKUtils.findRedirectHandler(solrDataByPid,solrAccess, userProvider, apacheClientProvider, instances, request, pid, null);
                if (redirectHandler != null) {

                    InputStream directStreamDC = null;
                    String cacheURl = baseUrl + "/dc";

                    CDKRequestItem hit = OAICDKUtils.cacheSearchHitByPid(cacheURl, pid, cacheSupport);
                    if (hit != null) {
                        directStreamDC = new ByteArrayInputStream(hit.getData().toString().getBytes(Charset.forName("UTF-8")));
                    } else {
                        InputStream dc = redirectHandler.directStreamDC(null);
                        String remoteData = IOUtils.toString(dc, "UTF-8");
                        OAICDKUtils.saveToCache(remoteData, cacheURl, pid, cacheSupport);
                        directStreamDC = new ByteArrayInputStream(remoteData.getBytes("UTF-8"));
                    }

                    if (directStreamDC != null) {
                        List<String> licenses = new ArrayList<>();
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
                            licenses = elms.stream().map(Element::getTextContent).collect(Collectors.toList());
                        }

                        String dataProvider = OAICDKUtils.findMetadataProvider(solrDataByPid);
                        String dataProviderBaseUrl = KConfiguration.getInstance().getConfiguration().getString(String.format("cdk.collections.sources.%s.baseurl", dataProvider));

                        Element rdf = EDMUtils.createEdmDataElements(KConfiguration.getInstance().getConfiguration(), dataProvider, dataProviderBaseUrl, licenses, instances, owningDocument, oaiRec, directStreamDC, pid, baseUrl);
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
                ProxyItemHandler redirectHandler = OAICDKUtils.findRedirectHandler(solrDataByPid,solrAccess, userProvider, apacheClientProvider, instances, request, pid, null);

                if (redirectHandler != null) {
                    String cacheURl = baseUrl + "/dc";

                    CDKRequestItem hit = OAICDKUtils.cacheSearchHitByPid(cacheURl, pid, cacheSupport);
                    InputStream directStreamDC = null;
                    if (hit != null) {
                        directStreamDC = new ByteArrayInputStream(hit.getData().toString().getBytes(Charset.forName("UTF-8")));
                    } else {
                        InputStream dc = redirectHandler.directStreamDC(null);
                        String remoteData = IOUtils.toString(dc, "UTF-8");
                        OAICDKUtils.saveToCache(remoteData, cacheURl, pid, cacheSupport);
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


                        OAICDKUtils.metadataProvider( solrDataByPid, oaiRec, provider);

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


}
