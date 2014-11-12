/*
 * Copyright (C) 2014 alberto
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package cz.incad.kramerius.client.tools;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.generic.ResourceTool;
import org.apache.velocity.tools.view.ViewToolContext;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.client.i18n.BundleContent;
import cz.incad.kramerius.client.i18n.BundlesApplicationSingleton;
import cz.incad.kramerius.client.tools.utils.LocalesDisect;

/**
 *
 * @author alberto
 */
@DefaultKey("i18n")
public class I18NTool  {

    public static final Logger LOGGER = Logger.getLogger(I18NTool.class.getName());

    private HttpServletRequest req;
    private  BundlesApplicationSingleton _instance =  BundlesApplicationSingleton.getInstance();

    private Locale locale;

    public void configure(Map props) {
        req = (HttpServletRequest) props.get("request");
        this.locale = LocalesDisect.findLocale(this.req);
    }

    public String getLanguage() {
        return this.locale.getLanguage();
    }
    
    public String getCountry() {
        return this.locale.getCountry();
    }
    
    public Object get(String key) {
        try {
            Object retval = _instance.get(this.locale, key);
            return retval;
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "!"+key+"!";
        }
    }
}
