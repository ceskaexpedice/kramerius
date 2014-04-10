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
package cz.incad.Kramerius.views.inc.home;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class RSSHomeViewObject {

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    MostDesirable mostDesirable;
    

    @Inject
    KConfiguration configuration;
    public String getChannelURL() {
        HttpServletRequest request = this.requestProvider.get();
        String urlString = request.getRequestURL().toString();
        String query = request.getQueryString();
        return urlString + ((query != null && query.length() > 0) ?  "?"+query : "");
    }

    
    public List<String> getMostDesirables() {
        return  this.mostDesirable.getMostDesirable(18, 0, null);
    }
    
    public String getApplicationURL() {
        return ApplicationURL.applicationURL(this.requestProvider.get());
    }
    
    public KConfiguration getConfiguration() {
        return configuration;
    }
}

