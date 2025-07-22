/*
 * Copyright (C) Jan 15, 2024 Pavel Stastny
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

import java.util.List;

/**
 * Represents OAI result pages containing metadata and records.
 */
public class OAIResults {
    
    private String metadataPrerix;
    private int completeListSize;
    private String resumptionToken;
    private List<OAIRecord> records;

    /**
     * Constructor for OAIResults.
     * @param completeListSize the total number of records available in the OAI repository
     * @param resumptionToken the token for resuming the result set, if applicable
     * @param metadataPrefix the metadata format prefix used for the records
     * @param records the list of OAI records in this result page
     */
    public OAIResults(int completeListSize, String resumptionToken, String metadataPrefix, List<OAIRecord> records) {
        super();
        this.completeListSize = completeListSize;
        this.resumptionToken = resumptionToken;
        this.records = records;
        this.metadataPrerix = metadataPrefix;
    }

    /**
     * Gets the total number of records available in the OAI repository.
     * @return the complete list size
     */
    public int getCompleteListSize() {
        return completeListSize;
    }

    /**
     * Gets the resumption token for this result set.
     * @return the resumption token, or null if there are no more records to fetch
     */
    public String getResumptionToken() {
        return resumptionToken;
    }
    
    /**
     * Gets the metadata prefix used for the records in this result set.
     * @return the metadata prefix
     */
    public String getMetadataPrefix() {
        return metadataPrerix;
    }

    /**
     * Gets the list of OAI records in this result page.
     * @return the list of OAI records
     */
    public List<OAIRecord> getRecords() {
        return records;
    }
}
