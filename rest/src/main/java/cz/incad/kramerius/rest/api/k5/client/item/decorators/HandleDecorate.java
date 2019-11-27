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
package cz.incad.kramerius.rest.api.k5.client.item.decorators;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.utils.JSONUtils;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class HandleDecorate extends AbstractItemDecorator {

    public static final Logger LOGGER = Logger.getLogger(HandleDecorate.class.getName());
    
    private static final String KEY = "HREF";

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void decorate(JSONObject jsonObject, Map<String, Object> context) {
        if (containsPidInJSON(jsonObject)) {
            try {
                String pidFromJSON = getPidFromJSON(jsonObject);
                PIDParser pidParser = new PIDParser(pidFromJSON);
                pidParser.objectPid();
                pidParser.getObjectId();
                String namespaceId = pidParser.getNamespaceId();
                if (namespaceId.equals("uuid")) {
                    String str = ApplicationURL.applicationURL(
                            this.requestProvider.get()).toString()
                            + "/handle/" + pidFromJSON;
                    JSONUtils.link(jsonObject, "handle", str);
                }
            } catch (LexerException e) {
                LOGGER.log(Level.WARNING,e.getMessage(),e);
                throw new GenericApplicationException(e.getMessage());
            } catch (JSONException e) {
                LOGGER.log(Level.WARNING,e.getMessage(),e);
                throw new GenericApplicationException(e.getMessage());
            }
        }
    }

    @Override
    public boolean apply(JSONObject jsonObject, String context) {
        TokenizedPath tpath = super.itemContext(tokenize(context));
        return (tpath.isParsed() && tpath.getRestPath().isEmpty());
    }
}
