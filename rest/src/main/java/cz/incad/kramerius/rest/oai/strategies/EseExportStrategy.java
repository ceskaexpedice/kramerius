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
package cz.incad.kramerius.rest.oai.strategies;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.rest.oai.OAIRecord;
import cz.incad.kramerius.rest.oai.OAISet;
import cz.incad.kramerius.rest.oai.utils.OAITools;
import cz.incad.kramerius.rest.oai.metadata.utils.OAICDKUtils;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Strategy for exporting metadata in the ESE (Europeana Semantic Elements) format.
 * This class extends MetadataExportStrategy to provide specific implementation
 * for the ESE metadata schema.
 */
public class EseExportStrategy extends MetadataExportStrategy{
    public static final Logger LOGGER = Logger.getLogger(EseExportStrategy.class.getName());


    public EseExportStrategy() {
        super("ese", "http://www.europeana.eu/schemas/ese/ESE-V3.2.xsd", "http://www.europeana.eu/schemas/ese/");
    }

    @Override
    public List<Element> perform(HttpServletRequest request, Document owningDocument, AkubraRepository akubraRepository, String oaiIdentifier, OAISet set) {
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
    public List<Element> performOnCDKSide(HttpServletRequest request, Document owningDocument, SolrAccess solrAccess, Provider<User> userProvider, Provider<CloseableHttpClient> apacheClientProvider, Instances instances, OAIRecord oaiRec, OAISet set, CDKRequestCacheSupport cacheSupport) {
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
    public boolean isRepresentativePageNeeded() {
        return false;
    }

    @Override
    public boolean isAvailableOnLocal() {
        return true;
    }

    @Override
    public boolean isAvailableOnCDKSide() {
        return false;
    }

}
