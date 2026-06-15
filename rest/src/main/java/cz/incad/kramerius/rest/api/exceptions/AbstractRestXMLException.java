package cz.incad.kramerius.rest.api.exceptions;

import cz.incad.kramerius.rest.api.utils.ExceptionXMLObjectUtils;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class AbstractRestXMLException extends WebApplicationException {

    public AbstractRestXMLException(String message, int status) {
        super(Response.status(status).entity(ExceptionXMLObjectUtils.fromMessageString(message, status).toString()).type(MediaType.APPLICATION_XML).build());
    }

    /*
    public AbstractRestXMLException(String message,Exception ex, int status) {
        super(Response.status(status).entity(ExceptionXMLObjectUtils.fromMessageString(message, status,ex).toString()).type(MediaType.APPLICATION_XML).build());
    }*/

}
