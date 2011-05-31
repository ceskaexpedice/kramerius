package cz.incad.kramerius.rights.server.utils;

import java.util.Locale;

import org.aplikator.server.Context;
import org.aplikator.server.descriptor.Application;

public class I18NUtils {

    public static String getLocalizedString(String key, Context ctx) {
        return Application.get().getLocalizedString(key, ctx != null ? ctx.getUserLocale() : Locale.getDefault());
    }

}
