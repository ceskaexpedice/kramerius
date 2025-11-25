package cz.inovatika.kramerius.services.config;

import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Element;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResponseHandlingParserUtil {

    private static final Logger LOGGER = Logger.getLogger(ResponseHandlingParserUtil.class.getName());


    public static ResponseHandlingConfig parse(Element parentElement) {
        
        Element rhElm = XMLUtils.findElement(parentElement, "response-handling");
        
        if (rhElm == null) return new ResponseHandlingConfig.Builder().build();

        ResponseHandlingConfig.Builder builder = new ResponseHandlingConfig.Builder();

        Element delayElm =   XMLUtils.findElement(rhElm, "delay");
        if (delayElm != null) {
            try {
                builder.delayMs(Integer.parseInt(delayElm.getTextContent()));
            } catch (NumberFormatException e) { 
                LOGGER.log(Level.WARNING, "Invalid number for delay.ms, using default 0.", e);
            }
        }
        
        Element retryCodes = XMLUtils.findElement(rhElm, "retry-status-codes");
        if (retryCodes != null) builder.retryStatusCodes(retryCodes.getTextContent());

        Element retriesElm = XMLUtils.findElement(rhElm, "max-retries");
        if (retriesElm != null) {
            try {
                builder.maxRetries(Integer.parseInt(retriesElm.getTextContent()));
            } catch (NumberFormatException e) { 
                LOGGER.log(Level.WARNING, "Invalid number for max-retries, using default 0.", e);
            }
        }

        return builder.build();
    }
}