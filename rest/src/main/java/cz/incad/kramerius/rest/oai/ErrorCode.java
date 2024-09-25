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

public enum ErrorCode {

    // acceptable for all verbs
    badArgument(400),
    // acceptable badResumptionToken
    badResumptionToken(400),
    // no verb
    badVerb(400),
    // GetRecord, ListIdentifiers, ListRecords
    cannotDisseminateFormat(400),
    // GetRecord, ListMetadataFormat
    idDoesNotExist(404),
    //ListIdentifiers ListRecords
    noRecordsMatch(200),
    //ListMetadataFormats
    noMetadataFormats(400),
    
    //ListSets ListIdentifiers ListRecords
    noSetHierarchy(400);

    public int getStatusCode() {
        return statusCode;
    }
    
    int statusCode;

    private ErrorCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    
}

