/*
 * Copyright (C) 2010 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.Kramerius.views.social;

import java.util.Locale;



import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class FacebookLikeItButton extends AbstractSocialButton {
    
    public static final String DEFAULT_LOCALE_STRING="cs_CZ";
    
    @Inject
    Provider<Locale> localeProvider;

    @Inject
    KConfiguration configuration;
    
    public String getLocale() {
        Locale locale = this.localeProvider.get();
        String country = locale.getCountry();
        String language = locale.getLanguage();
        if (!emptyString(country) && (!emptyString(language))) {
            return language+"_"+country;
        } else return DEFAULT_LOCALE_STRING;
    }

    
    @Override
    public boolean isButtonEnabled() {
        boolean fbEnabled = configuration.getConfiguration().getBoolean("facebook.likeit");
        return fbEnabled && (this.isItemPage() || isHomePage());
    }
}
