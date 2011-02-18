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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class SearchFormViewObject {

    @Inject
    Provider<HttpServletRequest> requestProvider;
    
    public String getRequestedAddress() throws UnsupportedEncodingException {
        String retValue  =  this.requestProvider.get().getRequestURL().toString();
        String queryString = this.requestProvider.get().getQueryString();
        
        if (queryString != null) {
            retValue =  retValue + "?" +queryString;
        }
        
        return URLEncoder.encode(retValue, "UTF-8");
    }
    
    
}
