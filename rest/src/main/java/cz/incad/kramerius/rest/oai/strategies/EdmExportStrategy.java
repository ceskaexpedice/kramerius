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
import cz.incad.kramerius.rest.oai.metadata.utils.EDMUtils;
import cz.incad.kramerius.rest.oai.metadata.utils.OAICDKUtils;
import cz.incad.kramerius.rest.oai.utils.OAITools;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Strategy for exporting metadata in the EDM format.
 * This class extends MetadataExportStrategy to provide specific implementation
 * for the EDM metadata schema.
 */
public class EdmExportStrategy extends MetadataExportStrategy{

    public static final Logger LOGGER = Logger.getLogger(EdmExportStrategy.class.getName());


    public EdmExportStrategy() {
        super("edm", "http://www.europeana.eu/schemas/ese/", "");
    }

    @Override
    public List<Element> perform(HttpServletRequest request, Document owningDocument, AkubraRepository akubraRepository, String oaiIdentifier, OAISet set) {
        throw new UnsupportedOperationException("unsupported on local instance");
    }

    @Override
    public List<Element> performOnCDKSide(HttpServletRequest request, Document owningDocument, SolrAccess solrAccess, Provider<User> userProvider, Provider<CloseableHttpClient> apacheClientProvider, Instances instances, OAIRecord oaiRec, OAISet set, CDKRequestCacheSupport cacheSupport) {
        try {
            String baseUrl = ApplicationURL.applicationURL(request);
            // base api url - it is different from standard base url
            String pid = OAITools.pidFromOAIIdentifier(oaiRec.getIdentifier());

            org.w3c.dom.Document solrDataByPid = solrAccess.getSolrDataByPid(pid);
            ProxyItemHandler redirectHandler = OAICDKUtils.findRedirectHandler(solrDataByPid,solrAccess, userProvider, apacheClientProvider, instances, request, pid, null);
            if (redirectHandler != null) {

                InputStream directStreamDC = getRemoteDC(cacheSupport, baseUrl, pid, redirectHandler);
                InputStream directStreamMods = getRemoteMods(cacheSupport, baseUrl, pid, redirectHandler);

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

                    Element rdf = EDMUtils.createEdmDataElements(KConfiguration.getInstance().getConfiguration(), dataProvider, dataProviderBaseUrl, licenses, instances, owningDocument, oaiRec, directStreamDC, directStreamMods, pid, baseUrl);
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
    public boolean isRepresentativePageNeeded() {
        return true;
    }

    @Override
    public boolean isAvailableOnLocal() {
        return false;
    }

    @Override
    public boolean isAvailableOnCDKSide() {
        return true;
    }

}
