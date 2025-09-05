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
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerException;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.rest.oai.OAIRecord;
import cz.incad.kramerius.rest.oai.OAISet;
import cz.incad.kramerius.rest.oai.metadata.utils.OAICDKUtils;
import cz.incad.kramerius.security.User;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;


/**
 * Abstract strategy for exporting metadata in different formats and contexts.
 *
 * <p>
 * Implementations of this class define how metadata should be exported for both
 * local and remote (CDK side) scenarios, using different export mechanisms and
 * supporting specific metadata schemas and namespaces.
 */
public abstract class MetadataExportStrategy {

    private String metadataPrefix;
    private String schema;
    private String metadataNamespace;


    /**
     * Constructs a new metadata export strategy with the specified metadata prefix, schema, and namespace.
     *
     * @param metadataPrefix     The metadata prefix used to identify the format.
     * @param schema             The XML schema URL associated with the metadata.
     * @param metadataNamespace  The XML namespace used for the metadata.
     */
    public MetadataExportStrategy(String metadataPrefix, String schema, String metadataNamespace) {
        this.metadataPrefix = metadataPrefix;
        this.schema = schema;
        this.metadataNamespace = metadataNamespace;
    }

    /**
     * Performs the metadata export operation for the local kramerius instance.
     *
     * @param request           The HTTP request that may contain additional parameters.
     * @param owningDocument    The source document whose metadata is being exported.
     * @param akubraRepository  The repository access object used for reading resources.
     * @param oaiIdentifier     The unique OAI identifier of the record.
     * @param set               The OAI set the record belongs to.
     * @return A list of XML elements representing the exported metadata.
     */
    public abstract  List<Element>  perform(HttpServletRequest request, Document owningDocument, AkubraRepository akubraRepository, String oaiIdentifier, OAISet set);

    /**
     * Performs the metadata export operation on the CDK side.
     *
     * @param request                 The HTTP request that may contain additional parameters.
     * @param owningDocument          The source document whose metadata is being exported.
     * @param solrAccess              Solr access object for querying additional metadata.
     * @param userProvider            Provider for accessing the current user context.
     * @param apacheClientProvider    Provider for obtaining an HTTP client for remote requests.
     * @param instances               Instances configuration used for distributed processing.
     * @param oaiRec                  The OAI record being exported.
     * @param set                     The OAI set the record belongs to.
     * @param cacheSupport            Cache support for optimizing CDK-side exports.
     * @return A list of XML elements representing the exported metadata.
     */
    public abstract  List<Element>  performOnCDKSide(HttpServletRequest request, Document owningDocument, SolrAccess solrAccess, Provider<User> userProvider, Provider<CloseableHttpClient> apacheClientProvider, Instances instances, OAIRecord oaiRec, OAISet set, CDKRequestCacheSupport cacheSupport);

    /**
     * Checks if the metadata export strategy is available on the local Kramerius instance.
     *
     * @return true if the strategy can be used locally, false otherwise.
     */
    public abstract boolean isAvailableOnLocal();

    /**
     *  Checks if the metadata export strategy is available on the CDK side.
     *
     * @return true if the strategy can be used on the CDK side, false otherwise.
     */
    public abstract boolean isAvailableOnCDKSide();

    /**
     * Determines if a representative page is needed for the metadata export.
     *
     * <p>
     * This method should return true if the export strategy requires a representative page
     * to be included in the metadata, which may involve additional processing or querying.
     *
     * @return true if a representative page is needed, false otherwise.
     */
    public abstract boolean isRepresentativePageNeeded();

    /**
     * Gets the metadata namespace associated with this export strategy.
     *
     * @return The XML namespace URI used for the metadata.
     */
    public String getMetadataNamespace() {
        return metadataNamespace;
    }

    /**
     * Gets the schema URL associated with this export strategy.
     *
     * @return The XML schema URL used for the metadata.
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Gets the metadata prefix used to identify the format of the exported metadata.
     *
     * @return The metadata prefix string.
     */
    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    @NotNull
    protected static InputStream getRemoteDC(CDKRequestCacheSupport cacheSupport, String baseUrl, String pid, ProxyItemHandler redirectHandler) throws ProxyHandlerException, IOException {
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
        return directStreamDC;
    }

    @NotNull
    protected static InputStream getRemoteMods(CDKRequestCacheSupport cacheSupport, String baseUrl, String pid, ProxyItemHandler redirectHandler) throws ProxyHandlerException, IOException {
        InputStream directStreamMods = null;
        String cacheURl = baseUrl + "/mods";

        CDKRequestItem hit = OAICDKUtils.cacheSearchHitByPid(cacheURl, pid, cacheSupport);
        if (hit != null) {
            directStreamMods = new ByteArrayInputStream(hit.getData().toString().getBytes(Charset.forName("UTF-8")));
        } else {
            InputStream dc = redirectHandler.directStreamDC(null);
            String remoteData = IOUtils.toString(dc, "UTF-8");
            OAICDKUtils.saveToCache(remoteData, cacheURl, pid, cacheSupport);
            directStreamMods = new ByteArrayInputStream(remoteData.getBytes("UTF-8"));
        }
        return directStreamMods;
    }

}
