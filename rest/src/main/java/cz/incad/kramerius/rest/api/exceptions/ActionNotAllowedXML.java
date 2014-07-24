package cz.incad.kramerius.rest.api.exceptions;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class ActionNotAllowedXML  extends AbstractRestXMLException {

    public ActionNotAllowedXML(String message) {
        super(message,HttpServletResponse.SC_FORBIDDEN);
    }
}
