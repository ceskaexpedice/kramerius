package cz.incad.kramerius.rest.api.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import cz.incad.kramerius.rest.api.utils.ExceptionJSONObjectUtils;
import cz.incad.kramerius.rest.api.utils.ExceptionXMLObjectUtils;

public class AbstractRestXMLException extends WebApplicationException{

    public AbstractRestXMLException(String message, int status) {
        super(Response.status(status).entity(ExceptionXMLObjectUtils.fromMessageString(message, status).toString()).type(MediaType.APPLICATION_XML).build());
    }

    /*
    public AbstractRestXMLException(String message,Exception ex, int status) {
        super(Response.status(status).entity(ExceptionXMLObjectUtils.fromMessageString(message, status,ex).toString()).type(MediaType.APPLICATION_XML).build());
    }*/

}
