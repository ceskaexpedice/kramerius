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

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.oai.OAIRecord;
import cz.incad.kramerius.rest.oai.OAISet;
import cz.incad.kramerius.rest.oai.utils.OAITools;
import cz.incad.kramerius.rest.oai.metadata.utils.DrKrameriusUtils;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.pid.LexerException;
import org.ceskaexpedice.akubra.pid.PIDParser;
import org.ceskaexpedice.akubra.relsext.KnownRelations;
import org.ceskaexpedice.akubra.relsext.RelsExtRelation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Strategy for exporting metadata in the DrKramerius4 format.
 * This class extends MetadataExportStrategy to provide specific implementation
 * for the DrKramerius4 metadata schema.
 */
public class DrKramerius4ExportStrategy extends MetadataExportStrategy {

    public static final Logger LOGGER = Logger.getLogger(DrKramerius4ExportStrategy.class.getName());

    public DrKramerius4ExportStrategy() {
        super("drkramerius4", "http://registrdigitalizace.cz/schemas/drkramerius/v4/drkram.xsd", "http://registrdigitalizace.cz/schemas/drkramerius/v4/");
    }

    @Override
    public List<Element> perform(HttpServletRequest request, Document owningDocument, AkubraRepository akubraRepository, String oaiIdentifier, OAISet set) {
        try {
            List<String> excludeModels = Lists.transform(KConfiguration.getInstance().getConfiguration().getList("oai.metadata.drkramerius4.excluderelations",
                    Arrays.asList(KnownRelations.HAS_INT_COMP_PART.toString(),
                            KnownRelations.HAS_PAGE.toString())
            ), Functions.toStringFunction());

            String baseUrl = ApplicationURL.applicationURL(request);
            String pid = OAITools.pidFromOAIIdentifier(oaiIdentifier);

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
    public List<Element> performOnCDKSide(HttpServletRequest request, Document owningDocument, SolrAccess solrAccess, Provider<User> userProvider, Provider<CloseableHttpClient> apacheClientProvider, Instances instances, OAIRecord oaiRec, OAISet set, CDKRequestCacheSupport cacheSupport) {
        return List.of();
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
