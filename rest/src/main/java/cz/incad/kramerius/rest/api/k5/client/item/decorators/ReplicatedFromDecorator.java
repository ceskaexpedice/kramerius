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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.rest.api.k5.client.utils.RELSEXTDecoratorUtils;
import cz.incad.kramerius.utils.XMLUtils;

public class ReplicatedFromDecorator extends AbstractItemDecorator {

    public static final Logger LOGGER = Logger
            .getLogger(ReplicatedFromDecorator.class.getName());

    public static final String REPLICATIONS_DECORATOR_KEY = AbstractItemDecorator
            .key("REPLICATIONS");

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Override
    public String getKey() {
        return REPLICATIONS_DECORATOR_KEY;
    }

    public static List<Element> findReplicatedFrom(Document doc) {
        List<Element> retval = new ArrayList<Element>();
        Element rdfElm = XMLUtils.findElement(doc.getDocumentElement(), "RDF",
                FedoraNamespaces.RDF_NAMESPACE_URI);
        if (rdfElm != null) {
            Element description = XMLUtils.findElement(rdfElm, "Description",
                    FedoraNamespaces.RDF_NAMESPACE_URI);
            if (description != null) {
                List<Element> elements = XMLUtils.getElements(description,
                        new XMLUtils.ElementsFilter() {

                            @Override
                            public boolean acceptElement(Element element) {
                                return (element.getLocalName().equals(
                                        "replicatedFrom") && element
                                        .getNamespaceURI().equals(
                                                FedoraNamespaces.KRAMERIUS_URI));
                            }
                        });
                for (Element el : elements) {
                    retval.add(el);
                }
            }
        }
        return retval;
    }

    @Override
    public void decorate(JSONObject jsonObject,
            Map<String, Object> runtimeContext) {

        try {
            if (jsonObject.has("pid")) {
                String pid = jsonObject.getString("pid");
                if (!PIDSupport.isComposedPID(pid)) {
                    Document relsExtDoc = RELSEXTDecoratorUtils
                            .getRELSEXTPidDocument(pid, context,
                                    this.fedoraAccess);
                    List<Element> replicated = findReplicatedFrom(relsExtDoc);
                    if (!replicated.isEmpty()) {
                        JSONArray replicatedJSON = new JSONArray();
                        for (Element colElm : replicated) {
                            String rep = colElm.getTextContent();
                            replicatedJSON.put(rep);
                        }
                        jsonObject.put("replicatedFrom", replicatedJSON);
                    }
                }
            }
        } catch (DOMException e) {
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
        return tpath.isParsed() && tpath.getRestPath().isEmpty();
    }
}
