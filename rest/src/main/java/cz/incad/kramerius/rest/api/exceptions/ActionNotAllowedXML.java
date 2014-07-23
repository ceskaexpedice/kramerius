package cz.incad.kramerius.rest.api.exceptions;

import javax.servlet.http.HttpServletResponse;

public class ActionNotAllowedXML  extends AbstractRestXMLException {

    public ActionNotAllowedXML(String message) {
        super(message,HttpServletResponse.SC_FORBIDDEN);
    }
}
