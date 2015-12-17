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

import static cz.incad.kramerius.client.tools.K5Configuration.getK5ConfigurationInstance;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.velocity.tools.config.DefaultKey;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 *
 * @author alberto
 */
@DefaultKey("conf")
public class ConfTool {

    public static final Logger LOGGER = Logger.getLogger(ConfTool.class.getName());

    Configuration _conf;

    public void configure(Map props) {
        try {
            _conf = KConfiguration.getInstance().getConfiguration();
        } catch (Exception ex) {
            Logger.getLogger(ConfTool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getString(String key) {
        return _conf.getString(key);
    }

    public boolean getBoolean(String key) {
        return _conf.getBoolean(key);
    }

    public String getIndexConfig() {
        try {
            return IndexConfig.getInstance().getJSON().toString();
        } catch (IOException ex) {
            Logger.getLogger(ConfTool.class.getName()).log(Level.SEVERE, null, ex);
            return "{}";
        } catch (JSONException ex) {
            Logger.getLogger(ConfTool.class.getName()).log(Level.SEVERE, null, ex);
            return "{}";
        }
    }
    
    public boolean resetIndexConfig(){
        try {
            IndexConfig.resetInstance();
            return true;
        } catch (Exception ex) {
            Logger.getLogger(ConfTool.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
