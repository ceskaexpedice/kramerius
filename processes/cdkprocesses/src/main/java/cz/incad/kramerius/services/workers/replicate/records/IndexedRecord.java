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
package cz.incad.kramerius.services.workers.replicate.records;

import java.util.Map;

/**
 * Represents a single already indexed document with related metadata.
 */
public class IndexedRecord extends ReplicateRecord {

    // The CDK Leader field from the document
    private final String cdkLeader;
    // The compositeId field used for SolrCloud sharding and distribution
    private final String composeId;
    // The actual indexed document represented as a map of field names to values
    private Map<String, Object> document;

    /**
     * Constructs an IndexedRecord from a map representing the indexed document.
     * Extracts specific fields like "pid", "cdk.leader", and "compositeId" if present.
     *
     * @param document A map containing the indexed document's fields and values.
     */
    public IndexedRecord(Map<String,Object> document) {
        super(document.containsKey("pid") ? (String) document.get("pid") : null);
        this.document = document;
        this.cdkLeader =  document.containsKey("cdk.leader") ? (String) this.document.get("cdk.leader") : null;
        this.composeId = document.containsKey("compositeId") ? (String) this.document.get("compositeId") : null;;
    }


    /**
     * Returns the value of the "cdk.leader" field from the document.
     *
     * @return The CDK Leader as a string, or null if not present.
     */
    public String getCdkLeader() {
        return cdkLeader;
    }

    /**
     * Returns the composite ID used for SolrCloud indexing and document distribution.
     *
     * @return The composite ID as a string, or null if not present.
     */
    public String getComposeId() {
        return composeId;
    }

    /**
     * Returns the full indexed document as a map of fields and values.
     *
     * @return A map representing the indexed document.
     */
    public Map<String, Object> getDocument() {
        return document;
    }
}
