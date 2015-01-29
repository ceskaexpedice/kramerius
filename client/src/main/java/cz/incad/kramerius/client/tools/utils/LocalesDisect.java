package cz.incad.kramerius.client.tools.utils;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.LocaleUtils;

public class LocalesDisect {

    public static Locale findLocale(HttpServletRequest req) {
        Locale retval = req.getLocale();
        HttpSession httpSession = req.getSession(true);
        if (httpSession.getAttribute(SESSION_PARAM) != null) {
            retval = (Locale) httpSession.getAttribute(SESSION_PARAM);
        }
        String language = req.getParameter(LocalesDisect.LANGUAGE_PARAM);
        if (language != null) {
            retval = LocaleUtils.toLocale(language);
        }
        httpSession.setAttribute(SESSION_PARAM, retval);
        return retval;
    }

    public static final String LANGUAGE_PARAM = "language";
    public static final String SESSION_PARAM = "locale";

}
