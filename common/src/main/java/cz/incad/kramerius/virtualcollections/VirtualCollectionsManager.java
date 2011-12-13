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

import java.io.IOException;
import java.util.List;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringEscapeUtils;
import org.fedora.api.RelationshipTuple;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VirtualCollectionsManager {

    private static final Logger logger = Logger.getLogger(VirtualCollectionsManager.class.getName());
    static final String SPARQL_NS = "http://www.w3.org/2001/sw/DataAccess/rf1/result";
    static final String TEXT_DS_PREFIX = "TEXT_";

    public static VirtualCollection getVirtualCollection(FedoraAccess fedoraAccess, String collection, String[] langs) {
        try {
            IResourceIndex g = ResourceIndexService.getResourceIndexImpl();
            Document doc = g.getFedoraObjectsFromModelExt("collection", 1000, 0, "title", "");

            NodeList nodes = doc.getDocumentElement().getElementsByTagNameNS(SPARQL_NS, "result");
            NodeList children;
            Node child;
            String name;
            String pid;

            for (int i = 0; i < nodes.getLength(); i++) {
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
                    }
                }
                if (name != null && pid != null && pid.equals(collection)) {
                    VirtualCollection vc = new VirtualCollection(name, pid);
                    for (int k = 0; k < langs.length; k++) {
                        String text = IOUtils.readAsString(fedoraAccess.getDataStream(pid, TEXT_DS_PREFIX + langs[++k]), Charset.forName("UTF-8"), true);
                        vc.addDescription(langs[k], text);
                    }
                    return vc;
                }
            }
            return null;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error getting virtual collections", ex);
            return null;
        }
    }

    public static List<VirtualCollection> getVirtualCollections(FedoraAccess fedoraAccess, String[] langs) throws Exception {
        try {
            IResourceIndex g = ResourceIndexService.getResourceIndexImpl();
            Document doc = g.getFedoraObjectsFromModelExt("collection", 1000, 0, "title", "");

            NodeList nodes = doc.getDocumentElement().getElementsByTagNameNS(SPARQL_NS, "result");
            NodeList children;
            Node child;
            String name;
            String pid;
            List<VirtualCollection> vcs = new ArrayList<VirtualCollection>();
            for (int i = 0; i < nodes.getLength(); i++) {
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
                    }
                }
                if (name != null && pid != null) {
                    VirtualCollection vc = new VirtualCollection(name, pid);
                    for (int k = 0; k < langs.length; k++) {
                        try{
                            String text = IOUtils.readAsString(fedoraAccess.getDataStream(pid, TEXT_DS_PREFIX + langs[++k]), Charset.forName("UTF-8"), true);
                            vc.addDescription(langs[k], text);
                        }catch(Exception e){
                            logger.log(Level.WARNING, "Error getting datastream", e);
                        }
                    }
                    vcs.add(vc);

                }
            }
            return vcs;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error getting virtual collections", ex);
            throw new Exception(ex);
        }
    }

    public static void create(String label, String pid, FedoraAccess fedoraAccess) throws IOException {
        InputStream is = VirtualCollectionsManager.class.getResourceAsStream("vc.xml");
        String s = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
        s = s.replaceAll("##title##", StringEscapeUtils.escapeXml(label)).replaceAll("##pid##", pid);
        fedoraAccess.getAPIM().ingest(s.getBytes(), "info:fedora/fedora-system:FOXML-1.1", "Create virtual collection");
    }

    public static void delete(String pid, FedoraAccess fedoraAccess) throws Exception {
        removeDocumentsFromCollection(pid, fedoraAccess);
        fedoraAccess.getAPIM().purgeObject(pid, "Virtual collection deleted", true);
        startIndexer(pid, "reindexCollection", "Reindex docs in collection");
    }

    public static void removeDocumentsFromCollection(String collection, FedoraAccess fedoraAccess) throws Exception {
        final String predicate = FedoraNamespaces.RDF_NAMESPACE_URI + "isMemberOfCollection";
        final String fedoraColl = collection.startsWith("info:fedora/") ? collection : "info:fedora/" + collection;
        IResourceIndex g = ResourceIndexService.getResourceIndexImpl();
        ArrayList<String> pids = g.getObjectsInCollection(collection, 1000, 0);
        for(String pid : pids){
            String fedoraPid = pid.startsWith("info:fedora/") ? pid : "info:fedora/" + pid;
            fedoraAccess.getAPIM().purgeRelationship(fedoraPid, predicate, fedoraColl, false, null);
            logger.log(Level.INFO, pid + " removed from collection " + collection);
        }
    }

    public static void modify(String pid, String label, FedoraAccess fedoraAccess) throws IOException {
        fedoraAccess.getAPIM().modifyObject(pid, "A", label, "K4", "Virtual collection modified");
        String dcContent = "<oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" "
                + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\"> "
                + "<dc:title>" + StringEscapeUtils.escapeXml(label) + "</dc:title><dc:identifier>" + pid + "</dc:identifier>"
                + "</oai_dc:dc>";
        fedoraAccess.getAPIM().modifyDatastreamByValue(pid, "DC", null, "Dublin Core Record for this object", "text/xml", null, dcContent.getBytes(), "DISABLED", null, "Virtual collection modified", true);
    }

    public static void modifyDatastream(String pid, String lang, String ds, FedoraAccess fedoraAccess, String k4url) throws IOException {
        String dsName = TEXT_DS_PREFIX + lang;
        String url = k4url + "?action=TEXT&content=" + URLEncoder.encode(ds, "UTF8");
        if (!fedoraAccess.isStreamAvailable(pid, dsName)) {
            fedoraAccess.getAPIM().addDatastream(pid, dsName, null, "Description " + lang, false, "text/plain", null, url, "M", "A", "DISABLED", null, "Add text description");
            System.out.println("Datastream added");
        } else {
            fedoraAccess.getAPIM().modifyDatastreamByReference(pid, dsName, null, "Description " + lang, "text/plain", null, url, "DISABLED", null, "Change text description", true);
        }
    }

    public static boolean isInCollection(String pid, String collection, final FedoraAccess fedoraAccess) throws IOException {
        String fedoraPid = pid.startsWith("info:fedora/") ? pid : "info:fedora/" + pid;
        String fedoraColl = collection.startsWith("info:fedora/") ? collection : "info:fedora/" + collection;
        List<RelationshipTuple> rels = fedoraAccess.getAPIM().getRelationships(fedoraPid, FedoraNamespaces.RDF_NAMESPACE_URI + "isMemberOfCollection");
        for (RelationshipTuple rel : rels) {
            if (rel.getObject().equals(fedoraColl)) {
                return true;
            }
        }
        return false;
    }

    public static void addToCollection(String pid, String collection, final FedoraAccess fedoraAccess) throws IOException {
        final String predicate = FedoraNamespaces.RDF_NAMESPACE_URI + "isMemberOfCollection";
        final String fedoraColl = collection.startsWith("info:fedora/") ? collection : "info:fedora/" + collection;


        try {
            fedoraAccess.processSubtree(pid, new TreeNodeProcessor() {

                boolean breakProcess = false;
                int previousLevel = 0;

                @Override
                public boolean breakProcessing(String pid, int level) {
                    return breakProcess;
                }

                @Override
                public void process(String pid, int level) throws ProcessSubtreeException {
                    try {
                        String fedoraPid = pid.startsWith("info:fedora/") ? pid : "info:fedora/" + pid;
                        fedoraAccess.getAPIM().addRelationship(fedoraPid, predicate, fedoraColl, false, null);
                        logger.log(Level.INFO, pid + " added to collection " + fedoraColl);
                    } catch (Exception e) {
                        throw new ProcessSubtreeException(e);
                    }
                }
            });
        } catch (ProcessSubtreeException e) {
            throw new IOException(e);
        }
    }

    public static void removeFromCollection(String pid, String collection, final FedoraAccess fedoraAccess) throws IOException {
        final String predicate = FedoraNamespaces.RDF_NAMESPACE_URI + "isMemberOfCollection";
        final String fedoraColl = collection.startsWith("info:fedora/") ? collection : "info:fedora/" + collection;
        try {
            fedoraAccess.processSubtree(pid, new TreeNodeProcessor() {

                boolean breakProcess = false;
                int previousLevel = 0;

                @Override
                public boolean breakProcessing(String pid, int level) {
                    return breakProcess;
                }

                @Override
                public void process(String pid, int level) throws ProcessSubtreeException {
                    try {
                        String fedoraPid = pid.startsWith("info:fedora/") ? pid : "info:fedora/" + pid;
                        fedoraAccess.getAPIM().purgeRelationship(fedoraPid, predicate, fedoraColl, false, null);
                        logger.log(Level.INFO, pid + " removed from collection " + fedoraColl);
                    } catch (Exception e) {
                        throw new ProcessSubtreeException(e);
                    }
                }
            });
        } catch (ProcessSubtreeException e) {
            throw new IOException(e);
        }
    }

    public static void removeCollections(String pid, final FedoraAccess fedoraAccess) throws Exception {
        final String predicate = FedoraNamespaces.RDF_NAMESPACE_URI + "isMemberOfCollection";
        try {
            fedoraAccess.processSubtree(pid, new TreeNodeProcessor() {

                boolean breakProcess = false;
                int previousLevel = 0;

                @Override
                public boolean breakProcessing(String pid, int level) {
                    return breakProcess;
                }

                @Override
                public void process(String pid, int level) throws ProcessSubtreeException {
                    try {
                        String fedoraPid = pid.startsWith("info:fedora/") ? pid : "info:fedora/" + pid;
                        fedoraAccess.getAPIM().purgeRelationship(fedoraPid, predicate, null, true, null);
                    } catch (Exception e) {
                        throw new ProcessSubtreeException(e);
                    }
                }
            });
        } catch (ProcessSubtreeException e) {
            throw new IOException(e);
        }
    }

    public static void startIndexer(String pid, String action, String title) throws Exception{
        String base = ProcessUtils.getLrServlet();

        if (base == null || pid == null) {
            logger.severe("Cannot start long running process");
            return;
        }
        String url = base + "?action=start&def=reindex&out=text&params="+action+"," +
                URLEncoder.encode(pid, "UTF8") + "," + URLEncoder.encode(title, "UTF8") +
                "&token=" + System.getProperty(ProcessStarter.TOKEN_KEY);

        logger.info("indexer URL:" + url);
        try {
            ProcessStarter.httpGet(url);
        } catch (Exception e) {
            logger.severe("Error starting indexer for " + pid + ":" + e);
        }
    }

    public static void main(String[] args) throws Exception {
        logger.log(Level.INFO, "process args: {0}", Arrays.toString(args));
        FedoraAccess fa = new FedoraAccessImpl(KConfiguration.getInstance());
        String action = args[0];
        String pid = args[1];
        String collection = args[2];

        if (action.equals("remove")) {
            ProcessStarter.updateName("Remove " + pid + " from collection " + collection);
            VirtualCollectionsManager.removeFromCollection(pid, collection, fa);
            startIndexer(pid, "fromKrameriusModel", "Reindex doc "+pid);
        } else if (action.equals("add")) {
            ProcessStarter.updateName("Add " + pid + " to collection " + collection);
            VirtualCollectionsManager.addToCollection(pid, collection, fa);
            startIndexer(pid, "fromKrameriusModel", "Reindex doc "+pid);
        }else if (action.equals("removecollection")) {
            ProcessStarter.updateName("Remove collection " + collection);
            VirtualCollectionsManager.delete(collection, fa);
        }else{
            logger.log(Level.INFO, "Unsupported action: {0}", action);
            return;
        }

        logger.log(Level.INFO, "Finished");

    }
}
