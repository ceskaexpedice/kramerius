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
package cz.incad.Kramerius.admins;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.LocalesProvider;
import cz.incad.Kramerius.security.RightsServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.IOUtils;

public abstract class AdminCommand {

    @Inject
    protected Provider<HttpServletRequest> requestProvider;

    @Inject
    protected Provider<HttpServletResponse> responseProvider;

    
    @Inject
    protected ResourceBundleService resourceBundleService;

    @Inject
    protected LocalesProvider localesProvider;

    @Inject
    @Named("securedFedoraAccess")
    protected transient FedoraAccess fedoraAccess;

    public abstract void doCommand();

    protected static StringTemplateGroup stFormsGroup() throws IOException {
        InputStream stream = AdministratorsActions.class.getResourceAsStream("adm.stg");
        String string = IOUtils.readAsString(stream, Charset.forName("UTF-8"), true);
        StringTemplateGroup group = new StringTemplateGroup(new StringReader(string), DefaultTemplateLexer.class);
        return group;
    }

    public Map<String, String> bundleToMap() throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        ResourceBundle bundle = this.resourceBundleService.getResourceBundle("labels", localesProvider.get());
        Set<String> keySet = bundle.keySet();
        for (String key : keySet) {
            map.put(key, bundle.getString(key));
        }
        return map;
    }
}
