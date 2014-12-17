package cz.incad.kramerius.rest.api.k5.client.pdf;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import cz.incad.kramerius.rest.api.exceptions.AbstractRestJSONException;

public class PDFResourceNotFound extends AbstractRestJSONException {

    public static final Logger LOGGER = Logger.getLogger(PDFResourceNotReadyException.class.getName());
    
    public PDFResourceNotFound(String message) {
        super(message,HttpServletResponse.SC_NOT_FOUND);
    }
}
