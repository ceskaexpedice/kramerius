package cz.incad.kramerius.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class ResourceBundleCache {
    private static Map<String, Locale> supportedLocales = new HashMap<>();

    public static void initSupportedLocales(List<String> localeList) {
        for (String s : localeList) {
            supportedLocales.put(s, new Locale(s));
        }
    }

    public static Locale resolveSupportedLocale(Locale locale) {
        Locale retval = supportedLocales.get(locale.getLanguage());
        if (retval == null) {
            return Locale.ROOT;
        } else {
            return retval;
        }
    }

}
