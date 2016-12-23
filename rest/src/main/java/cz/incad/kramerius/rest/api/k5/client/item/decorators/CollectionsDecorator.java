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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.rest.api.k5.client.utils.RELSEXTDecoratorUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class CollectionsDecorator extends AbstractItemDecorator {

    public static final Logger LOGGER = Logger.getLogger(ItemSolrRootModelDecorate.class.getName());

    public static final String COLLECTIONS_DECORATOR_KEY = AbstractItemDecorator.key("COLLECTIONS");

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Override
    public String getKey() {
        return COLLECTIONS_DECORATOR_KEY;
    }

    public static List<Element> findCollections(Document doc) {
        List<Element> retval = new ArrayList<Element>();
        Element rdfElm = XMLUtils.findElement(doc.getDocumentElement(), "RDF", FedoraNamespaces.RDF_NAMESPACE_URI);
        if (rdfElm != null) {
            Element description = XMLUtils.findElement(rdfElm, "Description", FedoraNamespaces.RDF_NAMESPACE_URI);
            if (description != null) {
                List<Element> elements = XMLUtils.getElements(description, new XMLUtils.ElementsFilter() {

                    @Override
                    public boolean acceptElement(Element element) {
                        return (element.getLocalName().equals("isMemberOfCollection")
                                && element.getNamespaceURI().equals(FedoraNamespaces.RDF_NAMESPACE_URI));
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
    public void decorate(JSONObject jsonObject, Map<String, Object> runtimeContext) {

        try {
            if (jsonObject.has("pid")) {
                String pid = jsonObject.getString("pid");
                if (!PIDSupport.isComposedPID(pid)) {
                    Document relsExtDoc = RELSEXTDecoratorUtils.getRELSEXTPidDocument(pid, context, this.fedoraAccess);
                    List<Element> collections = findCollections(relsExtDoc);
                    if (!collections.isEmpty()) {
                        JSONArray collectionsJSON = new JSONArray();
                        for (Element colElm : collections) {
                            String collectionPid = colElm.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI,
                                    "resource");
                            
                            if (StringUtils.isAnyString(collectionPid)) {
                                try {
                                    PIDParser pidParser = new PIDParser(collectionPid);
                                    if (collectionPid.startsWith(PIDParser.INFO_FEDORA_PREFIX)) {
                                        pidParser.disseminationURI();
                                    } else {
                                        pidParser.objectPid();
                                    }
                                    collectionsJSON.put(pidParser.getObjectPid());
                                } catch (LexerException e) {
                                    LOGGER.severe("cannot parse collection pid " + collectionPid);
                                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                                }
                            }
                        }
                        jsonObject.put("collections", collectionsJSON);
                    }
                }
            }
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
