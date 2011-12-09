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
package cz.incad.Kramerius.views;

import java.io.IOException;
import java.util.logging.Level;

import com.google.inject.Inject;

import cz.incad.kramerius.service.GoogleAnalytics;

public class GoogleAnalyticsViewObject {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GoogleAnalyticsViewObject.class.getName());

    
    @Inject
    GoogleAnalytics googleAnalytics;


    public boolean isReady() {
        return googleAnalytics.isWebPropertyIdDefined();
    }
    
    public boolean getWebPropertyIdDefined() {
        return googleAnalytics.isWebPropertyIdDefined();
    }

    public boolean isWebPropertyIdDefined() {
        return googleAnalytics.isWebPropertyIdDefined();
    }
    
    public String getWebPropertyId() {
        return googleAnalytics.getWebPropertyId();
    }
    
}
