/*
 * Copyright (C) 2013 Pavel Stastny
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
package cz.incad.kramerius.rest.api.k5.client.item.decorators.display;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.rest.api.k5.client.utils.RELSEXTDecoratorUtils;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.RelsExtHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ZoomDecorate extends AbstractDisplayDecorate {

    public static final Logger LOGGER = Logger.getLogger(ZoomDecorate.class
            .getName());

    public static final String ZOOM_KEY = AbstractDisplayDecorate.key("ZOOM");

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    KConfiguration kconf;
    
    @Override
    public String getKey() {
        return ZOOM_KEY;
    }

    JSONObject zoom(String vpid, String confObject, String appUrl) throws JSONException {
        //String zoomType = KConfiguration.getInstance().getProperty("zoom.viewer","zoomify");
//        String appUrl = ApplicationURL.applicationURL(this.requestProvider.get())
//                .toString() + (zoomType.equals("zoomify") ? "/zoomify/" : "/deepZoom/");
        
        JSONObject options = new JSONObject();
        String url = appUrl + vpid;
        options.put("url", url);
        options.put(
                "type",
                this.kconf.getProperty("zoom.viewer",
                        "zoomify"));
        return options;
    }

    @Override
    public void decorate(JSONObject jsonObject, Map<String, Object> context) {
        try {
            if (containsPidInJSON(jsonObject)) {
                String pid = getPidFromJSON(jsonObject);
                if (!PIDSupport.isComposedPID(pid)) {
                    Document relsExt = RELSEXTDecoratorUtils
                            .getRELSEXTPidDocument(pid, context,
                                    this.fedoraAccess);
                    String url = RelsExtHelper.getRelsExtTilesUrl(relsExt);
                    if (url != null) {
                        String zoomType = this.kconf.getProperty("zoom.viewer","zoomify");
                        String appUrl = ApplicationURL.applicationURL(this.requestProvider.get())
                              .toString() + (zoomType.equals("zoomify") ? "/zoomify/" : "/deepZoom/");
                    	jsonObject.put("zoom", zoom(pid, zoomType, appUrl));
                        String iiifUrl = ApplicationURL.applicationURL(this.requestProvider.get())
                                .toString() + "/iiif/";
                        jsonObject.put("iiif", iiifUrl + pid);
                    }
                }
            }
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        }
    }

    @Override
    public boolean apply(JSONObject jsonObject, String context) {
        TokenizedPath tpath = super.itemContext(tokenize(context));
        return (tpath.isParsed() && tpath.getRestPath().isEmpty());
    }

}
