/*
 * Copyright (C) 2011 Alberto Hernandez
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
package cz.incad.kramerius.virtualcollections;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.json.JSONArray;

/** TODO: REMOVE !! */
@Deprecated
public class VirtualCollectionsManager {

    static final Logger logger = Logger.getLogger(VirtualCollectionsManager.class.getName());
    static final String SPARQL_NS = "http://www.w3.org/2001/sw/DataAccess/rf1/result";
    static final String TEXT_DS_PREFIX = "TEXT_";

    
    public static VirtualCollection getVirtualCollection(FedoraAccess fedoraAccess, String collection, ArrayList<String> langs) {
        try {
            return doVC(collection, fedoraAccess, langs);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error getting virtual collections", ex);
            return null;
        }
    }

    public static VirtualCollection doVC(String pid, FedoraAccess fedoraAccess, ArrayList<String> languages) {
        try {
            String xPathStr;
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression expr;
    
            ArrayList<String> langs = new ArrayList<String>();
            if (languages == null || languages.isEmpty()) {
                String[] ls = KConfiguration.getInstance().getPropertyList("interface.languages");
                for (int i = 0; i < ls.length; i++) {
                    String lang = ls[++i];
                    langs.add(lang);
                }
            } else {
                langs = new ArrayList<String>(languages);
            }
            String name = "";
            boolean canLeave = true;
            fedoraAccess.getDC(pid);
            Document doc = fedoraAccess.getDC(pid);
            xPathStr = "//dc:title/text()";
            expr = xpath.compile(xPathStr);
            Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (node != null) {
                name = StringEscapeUtils.escapeXml(node.getNodeValue());
            }

            xPathStr = "//dc:type/text()";
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (node != null) {
                canLeave = Boolean.parseBoolean(StringEscapeUtils.escapeXml(node.getNodeValue()));
            }
            VirtualCollection vc = new VirtualCollection(name, pid, canLeave);

            for (String lang : langs) {
                String dsName = TEXT_DS_PREFIX + lang;
                String value = IOUtils.readAsString(fedoraAccess.getDataStream(pid, dsName), Charset.forName("UTF8"), true);
                vc.addDescription(lang, value);
            }
            return vc;
        } catch (Exception vcex) {
            logger.log(Level.WARNING, "Could not get virtual collection for  " + pid + ": " + vcex.toString());
            return null;
        }
    }
    
    public static List<VirtualCollection> getVirtualCollectionsFromFedora(FedoraAccess fedoraAccess, ArrayList<String> languages) throws Exception {
        try {
            IResourceIndex g = ResourceIndexService.getResourceIndexImpl();
            Document doc = g.getVirtualCollections();

            NodeList nodes = doc.getDocumentElement().getElementsByTagNameNS(SPARQL_NS, "result");
            NodeList children;
            Node child;
            String name;
            String pid;
            boolean canLeave;
            
            
            ArrayList<String> langs = new ArrayList<String>();
            
            if(languages == null || languages.isEmpty()){
                String[] ls = KConfiguration.getInstance().getPropertyList("interface.languages");
                for (int i = 0; i < ls.length; i++) {
                            String lang = ls[++i];
                    langs.add(lang);
                }
            }else{
                langs = new ArrayList<String>(languages);
            }
            
            List<VirtualCollection> vcs = new ArrayList<VirtualCollection>();
            for (int i = 0; i < nodes.getLength(); i++) {
                canLeave = false;
                name = null;
                pid = null;
                Node node = nodes.item(i);
                children = node.getChildNodes();
                for (int j = 0; j < children.getLength(); j++) {
                    child = children.item(j);
                    if ("title".equals(child.getLocalName())) {
                        name = child.getFirstChild().getNodeValue();
                    } else if ("object".equals(child.getLocalName())) {
                        pid = ((Element) child).getAttribute("uri").replaceAll("info:fedora/", "");
                    } else if ("canLeave".equals(child.getLocalName())) {
                        canLeave = Boolean.parseBoolean(child.getFirstChild().getNodeValue().replaceAll("\"", "").substring(("canLeave:").length()));
                    }
                }

                if (name != null && pid != null) {
                    try {
                        VirtualCollection vc = new VirtualCollection(name, pid, canLeave);

                        for (String lang : langs) {
                            String dsName = TEXT_DS_PREFIX + lang;
                            String value = IOUtils.readAsString(fedoraAccess.getDataStream(pid, dsName), Charset.forName("UTF8"), true);
                            vc.addDescription(lang, value);
                        }
                        vcs.add(vc);
                    } catch (Exception vcex) {
                        logger.log(Level.WARNING, "Could not get virtual collection for  " + pid + ": " + vcex.toString());

                    }
                }
            }
            return vcs;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error getting virtual collections", ex);
            throw new Exception(ex);
        }
    }

    public static List<VirtualCollection> getVirtualCollections(FedoraAccess fedoraAccess, ArrayList<String> languages) throws Exception {
        try {
            List<VirtualCollection> vcs = new ArrayList<VirtualCollection>();
            String query = "/terms?terms=true&terms.fl=collection&terms.limit=1000&terms.sort=index&wt=json";
            String solrHost = KConfiguration.getInstance().getSolrHost();
            String uri = solrHost + query;
            InputStream inputStream = RESTHelper.inputStream(uri, "<no_user>", "<no_pass>");

            JSONObject json = new JSONObject(IOUtils.readAsString(inputStream, Charset.forName("UTF-8"), true));
            JSONArray ja = json.getJSONObject("terms").getJSONArray("collection");
            String pid = "";
            for (int i = 0; i < ja.length(); i = i + 2) {
                try {
                    pid = ja.getString(i);
                    if(!"".equals(pid)){
                        VirtualCollection vc = doVC(pid, fedoraAccess, languages);
                        if(vc != null){
                            vcs.add(vc);
                        }
                    }
                } catch (Exception vcex) {
                    logger.log(Level.WARNING, "Could not get virtual collection for  " + pid + ": " + vcex.toString());
                    
                }
            }
            return vcs;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error getting virtual collections", ex);
            throw new Exception(ex);
        }
    }
    
    public static void main(String[] args) throws Exception {
        logger.log(Level.INFO, "process args: {0}", Arrays.toString(args));
        FedoraAccess fa = new FedoraAccessImpl(KConfiguration.getInstance(), null);
        String action = args[0];
        String pid = args[1];
        String collection = args[2];

        if (action.equals("remove")) {
            ProcessStarter.updateName("Remove " + pid + " from collection " + collection);
            CollectionUtils.removeFromCollection(pid, collection, fa);
            CollectionUtils.startIndexer(pid, "fromKrameriusModel", "Reindex doc " + pid);
        } else if (action.equals("add")) {
            ProcessStarter.updateName("Add " + pid + " to collection " + collection);
            CollectionUtils.addToCollection(pid, collection, fa);
            CollectionUtils.startIndexer(pid, "fromKrameriusModel", "Reindex doc " + pid);
        } else if (action.equals("removecollection")) {
            ProcessStarter.updateName("Remove collection " + collection);
            CollectionUtils.delete(collection, fa);
        } else {
            logger.log(Level.INFO, "Unsupported action: {0}", action);
            return;
        }

        logger.log(Level.INFO, "Finished");

    }
}
