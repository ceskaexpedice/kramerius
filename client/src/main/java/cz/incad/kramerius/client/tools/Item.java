/*
 * Copyright (C) 2013 Alberto Hernandez
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
package cz.incad.kramerius.client.tools;

import static cz.incad.kramerius.client.tools.K5Configuration.getK5ConfigurationInstance;
import cz.incad.kramerius.client.RESTHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.view.ViewToolContext;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
@DefaultKey("search")
public class Item {

    public static final Logger LOGGER = Logger.getLogger(Item.class.getName());

    HttpServletRequest req;
    String pid;
    JSONObject basic;
    private String host = "";

    public void configure(Map props) {
        try {
            req = (HttpServletRequest) props.get("request");
            pid = req.getParameter("pid");
            ViewToolContext vc = (ViewToolContext) props.get("velocityContext");
            host = KConfiguration.getInstance().getConfiguration().getString("api.point")+"/item/";
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }
    

    private String getUrl(String url) throws IOException {
        LOGGER.info("requesting url " + host + pid + url);
        InputStream inputStream = RESTHelper.inputStream(host + pid + url, this.req, new HashMap<String, String>());
        StringWriter sw = new StringWriter();
        org.apache.commons.io.IOUtils.copy(inputStream, sw, "UTF-8");
        return sw.toString();
    }

    public String getField(String f) {
        try {
            if(basic == null){
            String url = "";
            basic = new JSONObject(getUrl(url));
            }
            return basic.getString(f);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
}
