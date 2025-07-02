/*
 * Copyright (C) Jan 10, 2024 Pavel Stastny
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

/**
 * Enum representing various error codes used in OAI-PMH responses.
 * Each error code is associated with an HTTP status code.
 * @see <a href="https://www.openarchives.org/OAI/openarchivesprotocol.html#ErrorConditions">OAI-PMH Errors</a>
 */
public enum ErrorCode {

    // GetRecord, ListIdentifiers, ListRecords
    /**
     * Represents bad argument error.
     */
    badArgument(400),
    // acceptable badResumptionToken
    /**
     * Represents bad resumption token error.
     * This error occurs when the resumption token is malformed or invalid.
     */
    badResumptionToken(400),
    // no verb
    /**
     * Represents bad verb error.
     * This error occurs when the requested verb is not recognized or supported.
     */
    badVerb(400),
    // GetRecord, ListIdentifiers, ListRecords
    /**
     * Represents cannot dissemiante format error.
     * This error occurs when the requested format is not available for dissemination.
     */
    cannotDisseminateFormat(400),
    // GetRecord, ListMetadataFormat
    /**
     * Represents id does not exist error.
     * This error occurs when the requested identifier does not exist in the repository.
     */
    idDoesNotExist(404),
    //ListIdentifiers ListRecords
    /**
     * Represents no records match error.
     * This error occurs when the request does not match any records in the repository.
     */
    noRecordsMatch(200),
    //ListMetadataFormats
    /**
     * Represents no metadata formats error.
     * This error occurs when there are no metadata formats available for the requested identifier.
     */
    noMetadataFormats(400),
    
    //ListSets ListIdentifiers ListRecords
    /**
     * Represents no set hierarchy error.
     * This error occurs when the repository does not support sets or the set hierarchy is not available.
     */
    noSetHierarchy(400);

    /**
     * GetRecord, ListIdentifiers, ListRecords
     * The request is well-formed but cannot be processed due to a server error.
     */
    public int getStatusCode() {
        return statusCode;
    }
    
    int statusCode;

    private ErrorCode(int statusCode) {
        this.statusCode = statusCode;
    }
}

