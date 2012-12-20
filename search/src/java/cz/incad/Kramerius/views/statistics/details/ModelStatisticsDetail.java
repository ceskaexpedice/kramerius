/*
 * Copyright (C) 2012 Pavel Stastny
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
/**
 * 
 */
package cz.incad.Kramerius.views.statistics.details;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.utils.JSONUtils;
import cz.incad.Kramerius.views.AbstractPrintViewObject;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.model.DCConent;
import cz.incad.kramerius.document.model.utils.DCContentUtils;
import cz.incad.kramerius.document.model.utils.DescriptionUtils;
import cz.incad.kramerius.service.ResourceBundleService;

/**
 * @author pavels
 */
public class ModelStatisticsDetail {

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    @Inject
    SolrAccess solrAccess;
    
    @Inject
    Provider<HttpServletRequest> requestProvider;
    
    @Inject
    Provider<Locale> localesProvider;

    @Inject
    ResourceBundleService resourceBundleService;
    

    public String getTitle() throws IOException {
        ResourceBundle resBundle = resourceBundleService.getResourceBundle("labels", localesProvider.get());
        
        String pid = requestProvider.get().getParameter("pid");
        Map<String, List<DCConent>> dcs = DCContentUtils.getDCS(fedoraAccess, solrAccess, Arrays.asList(pid));
        
        StringBuilder builder = new StringBuilder();
        List<DCConent> contents = dcs.get(pid);
        for (int i = 0,ll=contents.size(); i < ll; i++) {
            DCConent dcConent = contents.get(i);
            String model = dcConent.getType();
            String i18n = null;
            String resBundleKey = "fedora.model."+model;
            if (resBundle.containsKey(resBundleKey)) {
                i18n = resBundle.getString(model);
            } else {
                i18n = model;
            }
            if (i > 0) builder.append(" | ");
            builder.append(i18n).append(":").append(JSONUtils.escaped(dcConent.getTitle()));
        }
        
        return builder.toString();
    }
    
    public String[] getDescriptions() throws IOException {
        String pid = requestProvider.get().getParameter("pid");
        Map<String, List<DCConent>> dcs = DCContentUtils.getDCS(fedoraAccess, solrAccess, Arrays.asList(pid));
        String[] descriptions = DescriptionUtils.getDescriptions(this.resourceBundleService.getResourceBundle("labels", localesProvider.get()), dcs, Arrays.asList(pid));
        for (int i = 0; i < descriptions.length; i++) {
            descriptions[i] = JSONUtils.escaped(descriptions[i]);
        }
        return descriptions;
    }
}
