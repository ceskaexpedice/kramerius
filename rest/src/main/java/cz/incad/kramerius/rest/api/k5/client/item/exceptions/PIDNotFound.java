package cz.incad.kramerius.rest.api.k5.client.item.exceptions;

import javax.servlet.http.HttpServletResponse;

import cz.incad.kramerius.rest.api.exceptions.AbstractRestJSONException;

//TODO: Merge with ObjectNotFound
public class PIDNotFound extends AbstractRestJSONException {

    public PIDNotFound(String message) {
        super(message,HttpServletResponse.SC_NOT_FOUND);
    }
}
