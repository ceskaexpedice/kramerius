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
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.generic.ResourceTool;

/**
 *
 * @author alberto
 */
@DefaultKey("css")
public class CSSTool extends ResourceTool {
    
    public static final Logger LOGGER = Logger.getLogger(CSSTool.class.getName());
HttpServletRequest req;
    HttpServletResponse resp;

    @Override
    public void configure(Map props) {
        setDefaultBundle((String)props.get("bundles"));
        req = (HttpServletRequest) props.get("request");
        if(req.getRequestURI().endsWith(".css")){
            resp = (HttpServletResponse) props.get("response");
            resp.setContentType("text/css");
        }
        
    }
}
