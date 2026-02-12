package org.kramerius.genpdf;

import com.google.inject.Provider;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class ArgumentLocalesProvider implements Provider<Locale> {

    private static final Locale DEFAULT_LOCALE = new Locale("cs");

    public static final String LOCALE_PROPERTY_KEY = "locale";

    private String localeFromArgument;

    public ArgumentLocalesProvider() {
        this.localeFromArgument = System.getProperty(LOCALE_PROPERTY_KEY);
    }


    @Override
    public Locale get() {
        if (StringUtils.isBlank(localeFromArgument)) {
            return DEFAULT_LOCALE;
        }
        try {
            return Locale.forLanguageTag(localeFromArgument);
        } catch (IllegalArgumentException e) {
            return DEFAULT_LOCALE;
        }
    }
}
