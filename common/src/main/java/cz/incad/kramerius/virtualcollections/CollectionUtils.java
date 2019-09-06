package cz.incad.kramerius.virtualcollections;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringEscapeUtils;
import org.fedora.api.RelationshipTuple;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.virtualcollections.Collection.Description;
import cz.incad.kramerius.virtualcollections.impl.fedora.FedoraCollectionsManagerImpl;

public class CollectionUtils {
    
    private static final int WAIT_TIMENOUT = 300;
    private static final int MAX_WAIT_ITERATION = 20;
    
    public static final Logger LOGGER = Logger.getLogger(Collection.class.getName());
    
    
    public static class CollectionManagerWait extends  CollectionWait {
        
        public static final Logger LOGGER = Logger.getLogger(CollectionManagerWait.class.getName());
        
        private CollectionsManager man;

        public CollectionManagerWait(CollectionsManager man) {
            super();
            this.man = man;
            if (!man.getClass().equals(FedoraCollectionsManagerImpl.class)) {
                throw new IllegalArgumentException("must be fedora implementation");
            }
        }

        @Override
        public boolean condition(String pid) {
            try {
                List<Collection> collections2 = man.getCollections();
                for (Collection c : collections2) {
                    if (c.getPid().equals(pid)) return true;
                }
            } catch (CollectionException e) {
                LOGGER.fine("Problem with checking collections from collection manager; waiting for next iteration");
            }
            LOGGER.fine("Returning false");
            return false;
        }
    }
    
    /**
     * Allows user to be sure that collection already exist
     * @author pstastny
     */
    public static abstract class CollectionWait {
        
        public CollectionWait() {
            super();
        }

        public abstract boolean condition(String pid);
    }
    
    
    /** Methods bellow are mostly moved from previous implementation of virtual collections */
    public static String create(FedoraAccess fedoraAccess,String title,boolean canLeaveFlag, Map<String, String>plainTexts, CollectionWait wait) throws IOException, InterruptedException {

        Map<String, String> encodedTexts = new HashMap<String, String>();
        for (String k : plainTexts.keySet()) {
            String encoded = Base64.encodeBase64String(plainTexts.get(k).getBytes("UTF-8"));;

            encodedTexts.put(k, encoded);
        }
        String pid = "vc:" + UUID.randomUUID().toString();
        InputStream stream = CollectionUtils.class.getResourceAsStream("vc_template.stg");
        String string = IOUtils.readAsString(stream, Charset.forName("UTF-8"), true);
        StringTemplateGroup grp = new StringTemplateGroup(new StringReader(string), DefaultTemplateLexer.class);
        StringTemplate template = grp.getInstanceOf("foxml");
        template.setAttribute("pid", pid);
        template.setAttribute("title", title != null ? title : pid);
        template.setAttribute("canLeave", canLeaveFlag);
        template.setAttribute("text", encodedTexts);
        
        fedoraAccess.getAPIM().ingest(template.toString().getBytes("UTF-8"), "info:fedora/fedora-system:FOXML-1.1", "Create virtual collection");
        
        if (wait != null) {
            LOGGER.log(Level.INFO, "Waiting until condition is true");
            int counter = 0;
            while(! wait.condition(pid)) {
                counter+=1;
                Thread.sleep(WAIT_TIMENOUT);
                // there is counter which prevent waiting forever
                if (counter == MAX_WAIT_ITERATION) break;
            }
        }
        return pid;
    }

    public static String create(FedoraAccess fedoraAccess) throws IOException {
        String pid = "vc:" + UUID.randomUUID().toString();
        InputStream is = CollectionUtils.class.getResourceAsStream("vc.xml");
        String s = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
        s = s.replaceAll("##title##", StringEscapeUtils.escapeXml(pid)).replaceAll("##pid##", pid);
        fedoraAccess.getAPIM().ingest(s.getBytes(), "info:fedora/fedora-system:FOXML-1.1", "Create virtual collection");
        return pid;
    }

    public static void deleteWOIndexer(String pid, FedoraAccess fedoraAccess) throws Exception {
        CollectionUtils.removeDocumentsFromCollection(pid, fedoraAccess);
        fedoraAccess.getAPIM().purgeObject(pid, "Virtual collection deleted", true);
    }

    public static void delete(String pid, FedoraAccess fedoraAccess) throws Exception {
        CollectionUtils.removeDocumentsFromCollection(pid, fedoraAccess);
        fedoraAccess.getAPIM().purgeObject(pid, "Virtual collection deleted", true);
        CollectionUtils.startIndexer(pid, "reindexCollection", "Reindex docs in collection");
    }

    public static void removeDocumentsFromCollection(String collection, FedoraAccess fedoraAccess) throws Exception {
        final String predicate = FedoraNamespaces.RDF_NAMESPACE_URI + "isMemberOfCollection";
        final String fedoraColl = collection.startsWith("info:fedora/") ? collection : "info:fedora/" + collection;
        IResourceIndex g = ResourceIndexService.getResourceIndexImpl();
        ArrayList<String> pids = g.getObjectsInCollection(collection, 1000, 0);
        for (String pid : pids) {
            String fedoraPid = pid.startsWith("info:fedora/") ? pid : "info:fedora/" + pid;
            fedoraAccess.getAPIM().purgeRelationship(fedoraPid, predicate, fedoraColl, false, null);
            LOGGER.log(Level.INFO, "{0} removed from collection {1}", new Object[]{pid, collection});
        }
    }

    public static void modify(String pid, String label, boolean canLeave, FedoraAccess fedoraAccess) throws IOException {
        fedoraAccess.getAPIM().modifyObject(pid, "A", label, "K4", "Virtual collection modified");
        String dcContent = "<oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" "
                + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\"> "
                + "<dc:title>" + StringEscapeUtils.escapeXml(label) + "</dc:title><dc:identifier>" + pid + "</dc:identifier>"
                + "<dc:type>canLeave:" + canLeave + "</dc:type>"
                + "</oai_dc:dc>";
        System.out.println(dcContent);
        fedoraAccess.getAPIM().modifyDatastreamByValue(pid, "DC", null, "Dublin Core Record for this object", "text/xml", null, dcContent.getBytes(), "DISABLED", null, "Virtual collection modified", true);
    }


    public static void modifyImageDatastream(String pid, String streamName, String mimeType, byte[] data, FedoraAccess fedoraAccess) throws IOException {
        //String url = k4url + "?action=TEXT&content=" + URLEncoder.encode(ds, "UTF8");
        File tmpFile = File.createTempFile("collections", "content");
        tmpFile.createNewFile();
        IOUtils.saveToFile(data, tmpFile);
        if (!fedoraAccess.isStreamAvailable(pid, streamName)) {
            fedoraAccess.getAPIM().addDatastream(pid, streamName, null, "Description " +streamName, false, mimeType, null, tmpFile.toURI().toString(), "M", "A", "DISABLED", null, "Add image ("+streamName+")");
            LOGGER.log(Level.INFO, "Datastream added");
        } else {
            fedoraAccess.getAPIM().modifyDatastreamByReference(pid, streamName, null, "Description " + streamName, mimeType, null, tmpFile.toURI().toString(), "DISABLED", null, "Change image ("+streamName+")", true);
            LOGGER.log(Level.INFO, "Datastream modified");
        }
    }

    public static void modifyLangDatastream(String pid, String lang, String ds, FedoraAccess fedoraAccess) throws IOException {
        String dsName = VirtualCollectionsManager.TEXT_DS_PREFIX + lang;
        modifyLangDatastream(pid, lang, dsName, ds, fedoraAccess);
    }

    public static void modifyLangDatastream(String pid, String lang,String dsName, String ds, FedoraAccess fedoraAccess) throws IOException {
        File tmpFile = File.createTempFile("collections", "content");
        tmpFile.createNewFile();
        IOUtils.saveToFile(ds.getBytes("UTF-8"), tmpFile);
        if (!fedoraAccess.isStreamAvailable(pid, dsName)) {
            fedoraAccess.getAPIM().addDatastream(pid, dsName, null, "Description " + lang, false, "text/plain", null, tmpFile.toURI().toString(), "M", "A", "DISABLED", null, "Add text description");
            LOGGER.log(Level.INFO, "Datastream added");
        } else {
            fedoraAccess.getAPIM().modifyDatastreamByReference(pid, dsName, null, "Description " + lang, "text/plain", null, tmpFile.toURI().toString(), "DISABLED", null, "Change text description", true);
            LOGGER.log(Level.INFO, "Datastream modified");
        }
    }

    public static void modifyTexts(String pid, FedoraAccess fedoraAccess, Map<String, String> textsMap) throws IOException {
    
        String texts = "<texts>";
        for (String lang : textsMap.keySet()) {
            String text = textsMap.get(lang);
            if (text != null) {
                texts += String.format("<text language=\"%s\">%s</text>", lang, text, lang);
            }
        }
        texts += "</texts>";
        fedoraAccess.getAPIM().modifyDatastreamByValue(pid, "TEXT", null, "Localized texts for this object", "text/plain", null, texts.getBytes(), "DISABLED", null, "Change text description", true);
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

    public static void addPidToCollection(String pid, String collection, final FedoraAccess fedoraAccess) throws IOException {
        final String predicate = FedoraNamespaces.RDF_NAMESPACE_URI + "isMemberOfCollection";
        final String fedoraColl = collection.startsWith("info:fedora/") ? collection : "info:fedora/" + collection;
    
        try {
    
            String fedoraPid = pid.startsWith("info:fedora/") ? pid : "info:fedora/" + pid;
            fedoraAccess.getAPIM().addRelationship(fedoraPid, predicate, fedoraColl, false, null);
            LOGGER.log(Level.INFO, "{0} added to collection {1}", new Object[]{pid, fedoraColl});
    
        } catch (Exception e) {
            throw new IOException(e);
        }
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
                        LOGGER.log(Level.INFO, pid + " added to collection " + fedoraColl);
                    } catch (Exception e) {
                        throw new ProcessSubtreeException(e);
                    }
                }
    
                @Override
                public boolean skipBranch(String pid, int level) {
                    return false;
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
                        LOGGER.log(Level.INFO, pid + " removed from collection " + fedoraColl);
                    } catch (Exception e) {
                        throw new ProcessSubtreeException(e);
                    }
                }
    
                @Override
                public boolean skipBranch(String pid, int level) {
                    return false;
                }
            });
        } catch (ProcessSubtreeException e) {
            throw new IOException(e);
        }
    }

    public static void startIndexer(String pid, String action, String title) throws Exception {
        String base = ProcessUtils.getLrServlet();
    
        if (base == null || pid == null) {
            LOGGER.severe("Cannot start long running process");
            return;
        }
        String url = base + "?action=start&def=reindex&out=text&params=" + action + ","
                + URLEncoder.encode(pid, "UTF8") + "," + URLEncoder.encode(title, "UTF8")
                + "&token=" + System.getProperty(ProcessStarter.TOKEN_KEY);
    
        LOGGER.info("indexer URL:" + url);
        try {
            ProcessStarter.httpGet(url);
        } catch (Exception e) {
            LOGGER.severe("Error starting indexer for " + pid + ":" + e);
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
    
                @Override
                public boolean skipBranch(String pid, int level) {
                    return false;
                }
            });
        } catch (ProcessSubtreeException e) {
            throw new IOException(e);
        }
    }

    
    public static JSONObject virtualCollectionTOJSON(Collection vc) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pid", vc.getPid());
        jsonObj.put("label", vc.getLabel());
        jsonObj.put("canLeave", vc.isCanLeaveFlag());
        jsonObj.put("numberOfDocs", vc.getNumberOfDocs());
        
        
        JSONObject descsMap = new JSONObject();
        JSONObject longDescsMap = new JSONObject();

        List<Description> descs = vc.getDescriptions();
        for (Description d : descs) {
            descsMap.put(d.getLangCode(), d.getText());
            if (d.hasLongtext()) {
                longDescsMap.put(d.getLangCode(), d.getLongText());
            }
        }

        jsonObj.put("descs", descsMap);
        if (!longDescsMap.keySet().isEmpty()) {
            jsonObj.put("longDescs", longDescsMap);
        }
        return jsonObj;
    }

}
