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
package cz.incad.Kramerius.views.inc.details.tabs;

import java.io.IOException;
import java.util.Locale;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;

public class ImageViewObject {

    
    private static final String RIGHT_MSG = "rightMsg";

    @Inject
    TextsService textsService;

    @Inject
    ResourceBundleService resourceBundleService;
    
    @Inject
    Provider<Locale> localeProvider;
    
    public String getNotAllowedMessageText() throws IOException {
        Locale locale = this.localeProvider.get();
        if (textsService.isAvailable(RIGHT_MSG, locale)) {
            return textsService.getText(RIGHT_MSG, locale);
        } else return this.resourceBundleService.getResourceBundle("labels", locale).getString(RIGHT_MSG);
    }
}
