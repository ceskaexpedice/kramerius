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
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Strategy for exporting metadata in the OAI Dublin Core format.
 * This class extends MetadataExportStrategy to provide specific implementation
 * for the OAI Dublin Core metadata schema.
 */
public class OaiDcExportStrategy extends MetadataExportStrategy{

    public static final Logger LOGGER = Logger.getLogger(OaiDcExportStrategy.class.getName());

    public OaiDcExportStrategy() {
        super("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc.xsd",  "http://www.openarchives.org/OAI/2.0/oai_dc/");
    }

    @Override
    public List<Element> perform(HttpServletRequest request, Document owningDocument, AkubraRepository akubraRepository, String oaiIdentifier, OAISet set) {
        String pid = OAITools.pidFromOAIIdentifier(oaiIdentifier);
        Document dc = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_DC).asDom(false);
        if (dc != null) {
            Element rootElement = dc.getDocumentElement();
            owningDocument.adoptNode(rootElement);
            return Arrays.asList(rootElement);
        } else return null;

    }

    @Override
    public List<Element> performOnCDKSide(HttpServletRequest request, Document owningDocument, SolrAccess solrAccess, Provider<User> userProvider, Provider<CloseableHttpClient> apacheClientProvider, Instances instances, OAIRecord oaiRec, OAISet set, CDKRequestCacheSupport cacheSupport) {
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
    public boolean isRepresentativePageNeeded() {
        return false;
    }

    @Override
    public boolean isAvailableOnLocal() {
        return true;
    }

    @Override
    public boolean isAvailableOnCDKSide() {
        return true;
    }
}
