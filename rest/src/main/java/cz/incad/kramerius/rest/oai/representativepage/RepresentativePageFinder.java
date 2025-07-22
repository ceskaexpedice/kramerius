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
import cz.incad.kramerius.rest.oai.strategies.MetadataExportStrategy;

/**
 * Interface for finding the representative page of a given OAI record.
 * <p>
 * Implementations of this interface should define the logic to identify
 * the most suitable or representative page associated with a specific record.
 * This may involve querying a Solr index and applying a particular metadata export strategy.
 */
public interface RepresentativePageFinder {

    /**
     * Finds and assigns the representative page for the specified OAI record.
     *
     * @param oaiRecord      The OAI record for which the representative page is to be found.
     * @param sa             SolrAccess instance used to query Solr for relevant information.
     * @param exportStrategy The metadata export strategy to be applied during the process.
     */
    void findRepresentativePage(OAIRecord oaiRecord, SolrAccess sa, MetadataExportStrategy exportStrategy);
}
