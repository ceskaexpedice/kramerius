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
package cz.incad.kramerius.rest.oai.representativepage;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.oai.OAIRecord;
import cz.incad.kramerius.rest.oai.record.OAIRecordSupplement;
import cz.incad.kramerius.rest.oai.record.SupplementType;
import cz.incad.kramerius.rest.oai.strategies.MetadataExportStrategy;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Optional;

public class FirstPageFinder implements RepresentativePageFinder {


    @Override
    public void findRepresentativePage(OAIRecord oaiRecord, SolrAccess sa, MetadataExportStrategy exportStrategy) {
        if (exportStrategy != null &&  exportStrategy.isRepresentativePageNeeded()) {
            Optional<OAIRecordSupplement> reprePage = oaiRecord.getSupplements().stream().filter(s -> {
                return s.supplementType() == SupplementType.REPRESENTATIVE_PAGE_PID;
            }).findAny();

            if (reprePage.isEmpty()) {
                String parentIdentifier = oaiRecord.getSolrIdentifier();
                JSONObject childDoc = null;
                do {
                    try {
                        childDoc = goDown(sa, parentIdentifier);
                        if (childDoc != null) {
                            if (childDoc.has("ds.img_full.mime")) {
                                String reprepid = childDoc.optString("pid");
                                String repreMimetype = childDoc.optString("ds.img_full.mime");
                                oaiRecord.addSupplement(new OAIRecordSupplement(reprepid, SupplementType.REPRESENTATIVE_PAGE_PID));
                                oaiRecord.addSupplement(new OAIRecordSupplement(repreMimetype, SupplementType.REPRESENTATIVE_PAGE_MIME_TYPE));
                                break;
                            } else {
                                parentIdentifier = childDoc.getString("pid");
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } while (childDoc != null);
            }
        }
    }

    private static JSONObject goDown(SolrAccess sa, String pid) throws IOException {
        String ownPidPathq= URLEncoder.encode(pid.replaceAll(":","\\\\:"), "UTF-8");
        String q = String.format("q=own_parent.pid:%s&fl=pid+ds.img_full.mime+rels_ext_index.sort&sort=rels_ext_index.sort+asc&rows=1", ownPidPathq);
        String solrResponseJSON = sa.requestWithSelectReturningString(q, "json", null);
        JSONObject rJSON = new JSONObject(solrResponseJSON);
        JSONObject response = rJSON.getJSONObject("response");
        long numFound = response.optLong("numFound");
        if (numFound >0 ) {
            return response.getJSONArray("docs").getJSONObject(0);
        } else return null;
    }
}
