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

import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import net.sf.json.JSONObject;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.AbstractDecorator.TokenizedPath;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.AbstractItemDecorator;
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

    @Override
    public String getKey() {
        return ZOOM_KEY;
    }

    private Object zoom(String vpid) {
        JSONObject options = new JSONObject();
        String url = ApplicationURL.applicationURL(this.requestProvider.get())
                .toString() + "/deepZoom/" + vpid;
        options.put("url", url);
        options.put(
                "type",
                KConfiguration.getInstance().getProperty("zoom.viewer",
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
                    String url = RelsExtHelper.getRelsExtTilesUrl(relsExt,
                            fedoraAccess);
                    if (url != null) {
                        jsonObject.put("zoom", zoom(pid));
                    }
                }
            }
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public boolean apply(JSONObject jsonObject, String context) {
        TokenizedPath tpath = super.itemContext(tokenize(context));
        return (tpath.isParsed() && tpath.getRestPath().isEmpty());
    }

}
