package cz.incad.kramerius.rest.api.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cz.incad.kramerius.rest.api.utils.ExceptionJSONObjectUtils;

public class AbstractRestXMLException extends WebApplicationException{


    public AbstractRestXMLException(String message, int status) {
        super(Response.status(status).entity(ExceptionJSONObjectUtils.fromMessage(message, status).toString()).type(MediaType.APPLICATION_JSON).build());
    }
    
    public AbstractRestXMLException(String message,Exception ex, int status) {
        super(Response.status(status).entity(ExceptionJSONObjectUtils.fromMessage(message, status,ex).toString()).type(MediaType.APPLICATION_JSON).build());
    }

}
