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

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.cdk.CDKUtils;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.rest.oai.MetadataExport;
import cz.incad.kramerius.rest.oai.OAIRecord;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import cz.inovatika.cdk.cache.impl.CDKRequestItemFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.ceskaexpedice.akubra.pid.LexerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OAICDKUtils {

    public static final Logger LOGGER = Logger.getLogger(OAICDKUtils.class.getName());

    private OAICDKUtils() {}

    public static String findMetadataProvider(Document solrDataByPid) {
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

    private static String defaultDocumentSource(Document solrDataByPid) throws IOException {
        String leader = CDKUtils.findCDKLeader(solrDataByPid.getDocumentElement());
        List<String> sources = CDKUtils.findSources(solrDataByPid.getDocumentElement());
        return leader != null ? leader : (!sources.isEmpty() ? sources.get(0) : null);
    }

    public static void metadataProvider(Document solrDataByPid, OAIRecord oaiRec, Element provider) {
        String foundMetadataProvider = findMetadataProvider(solrDataByPid);
        if (foundMetadataProvider != null) {
            provider.setTextContent(foundMetadataProvider);
        }
    }

    public static void saveToCache(String data, String url, String pid, CDKRequestCacheSupport cacheSupport) {
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

    public static CDKRequestItem cacheSearchHitByPid(String url, String pid, CDKRequestCacheSupport cacheSupport) {
        int days = KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.item", 30);
        LOGGER.log(Level.INFO, String.format("this.cacheSupport.find(\"%s\", \"%s\",\"%s\", \"%s\")", null, url, pid, null));
        List<CDKRequestItem> cdkRequestItems = cacheSupport.find(null, url, pid, null);
        if (!cdkRequestItems.isEmpty() && !cdkRequestItems.get(0).isExpired(days)) {
            LOGGER.log(Level.INFO, String.format("this.cacheSupport.found(\"%s\", \"%s\",\"%s\", \"%s\")", null, url, pid, null));
            return cdkRequestItems.get(0);
        }
        return null;
    }
}
