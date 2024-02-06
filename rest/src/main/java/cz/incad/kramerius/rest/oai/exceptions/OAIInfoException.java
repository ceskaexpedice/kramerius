/*
 * Copyright (C) Feb 3, 2024 Pavel Stastny
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
package cz.incad.kramerius.rest.oai.exceptions;

import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import cz.incad.kramerius.rest.oai.ErrorCode;
import cz.incad.kramerius.rest.oai.MetadataExport;
import cz.incad.kramerius.rest.oai.OAISet;
import cz.incad.kramerius.rest.oai.OAIVerb;

public class OAIInfoException extends WebApplicationException {
    public static final Logger LOGGER = Logger.getLogger(OAIException.class.getName());
    
    protected OAIInfoException(Response response) {
        super(response);
    }

//    public AbstractOAIException(int errorCode) {
//        super(errorCode);
//    }
//
//    public AbstractOAIException(Response.Status status) {
//        super(status);
//    }

    public OAIInfoException(String errMessage) {
        this(Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(buildJSON(errMessage))
                .build());
    }

    private static String buildJSON(String errMessage) {
        // TODO Auto-generated method stub
        JSONObject errorJSON = new JSONObject();
        errorJSON.put("error", errMessage);
        return errorJSON.toString();
    }

}
