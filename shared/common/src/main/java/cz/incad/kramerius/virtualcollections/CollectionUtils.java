package cz.incad.kramerius.virtualcollections;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.fedora.utils.Fedora4Utils;
import cz.incad.kramerius.utils.FedoraUtils;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexService;

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
    public static String create(FedoraAccess fedoraAccess,String title,boolean canLeaveFlag, Map<String, String>plainTexts, CollectionWait wait) throws IOException, InterruptedException, RepositoryException {
        final List<String> retvals =new ArrayList<>();
        Fedora4Utils.doWithProcessingIndexCommit(fedoraAccess.getInternalAPI(), (repo)->{
            String pid = null;
            try {
                Map<String, String> encodedTexts = new HashMap<String, String>();
                for (String k : plainTexts.keySet()) {
                    //String encoded = Base64.encodeBase64String(plainTexts.get(k).getBytes("UTF-8"));;
                    encodedTexts.put(k, plainTexts.get(k));
                }
                pid = "vc:" + UUID.randomUUID().toString();
                InputStream stream = CollectionUtils.class.getResourceAsStream("vc_template.stg");
                String content = IOUtils.toString(stream, Charset.forName("UTF-8"));

                StringTemplateGroup grp = new StringTemplateGroup(new StringReader(content), DefaultTemplateLexer.class);

                RepositoryObject collection = repo.createOrFindObject(pid);

                StringTemplate dcTemplate = grp.getInstanceOf("dc");
                dcTemplate.setAttribute("pid", pid);
                dcTemplate.setAttribute("title", title != null ? title : pid);
                dcTemplate.setAttribute("canLeave", canLeaveFlag);
                collection.createStream(FedoraUtils.DC_STREAM, "text/xml", new ByteArrayInputStream(dcTemplate.toString().getBytes(Charset.forName("UTF-8"))));

                if (plainTexts.containsKey("cs")) {
                    byte[] textCsBytes = plainTexts.get("cs").getBytes(Charset.forName("UTF-8"));
                    collection.createStream("TEXT_cs", "text/xml", new ByteArrayInputStream(textCsBytes));
                }

                if (plainTexts.containsKey("en")) {
                    byte[] textEnBytes = plainTexts.get("en").getBytes(Charset.forName("UTF-8"));
                    collection.createStream("TEXT_en", "text/xml", new ByteArrayInputStream(textEnBytes));
                }

                StringTemplate relsextTemplate = grp.getInstanceOf("relsext");
                relsextTemplate.setAttribute("pid", pid);
                collection.createStream(FedoraUtils.RELS_EXT_STREAM, "text/xml", new ByteArrayInputStream(relsextTemplate.toString().getBytes(Charset.forName("UTF-8"))));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            retvals.add(pid);
        });

        if (!retvals.isEmpty()) {
            return retvals.get(0);
        } else throw new RepositoryException("Cannot create collection");
    }

    public static void deleteWOIndexer(String pid, FedoraAccess fedoraAccess) throws Exception {
        Fedora4Utils.doWithProcessingIndexCommit(fedoraAccess.getInternalAPI(),(repo)->{
            try {
                CollectionUtils.removeDocumentsFromCollection(pid, repo);
                repo.deleteobject(pid);
            } catch (Exception e) {
                throw new RepositoryException(e);
            }
        });

    }

    public static void delete(String pid, FedoraAccess fedoraAccess) throws Exception {
        Fedora4Utils.doWithProcessingIndexCommit(fedoraAccess.getInternalAPI(),(repo)->{
            try {
                CollectionUtils.removeDocumentsFromCollection(pid, repo);
                repo.deleteobject(pid);
            } catch (Exception e) {
                throw new RepositoryException(e);
            }
        });
        CollectionUtils.startIndexer(pid, "reindexCollection", "Reindex docs in collection");
    }

    public static void removeDocumentsFromCollection(String collection, Repository repo) throws Exception {
        IResourceIndex g = ResourceIndexService.getResourceIndexImpl();
        List<String> allPids = new ArrayList<>();

        int offset = 0;
        List<String> pids = g.getObjectsInCollection(collection, 1000, offset);
        do {
            allPids.addAll(pids);
            pids = g.getObjectsInCollection(collection, 1000, offset);
        }while(!pids.isEmpty());

        for (String pid : allPids) {
            repo.getObject(pid).removeRelation("isMemberOfCollection", FedoraNamespaces.RDF_NAMESPACE_URI,collection);
            LOGGER.log(Level.INFO, "{0} removed from collection {1}", new Object[]{pid, collection});
        }

    }

    public static void modify(String pid, String label, boolean canLeave, FedoraAccess fedoraAccess) throws IOException, RepositoryException {
        String dcContent = "<oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" "
                + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\"> "
                + "<dc:title>" + StringEscapeUtils.escapeXml(label) + "</dc:title><dc:identifier>" + pid + "</dc:identifier>"
                + "<dc:type>canLeave:" + canLeave + "</dc:type>"
                + "</oai_dc:dc>";

        Repository repo = fedoraAccess.getInternalAPI();
        if (repo.getObject(pid).streamExists(FedoraUtils.DC_STREAM)) {
                repo.getObject(pid).deleteStream(FedoraUtils.DC_STREAM);
        }
        repo.getObject(pid).createStream(FedoraUtils.DC_STREAM, "text/xml", new ByteArrayInputStream(dcContent.getBytes(Charset.forName("UTF-8"))));
    }


    public static void modifyDatastream(String pid, String streamName, String mimeType, byte[] data, FedoraAccess fedoraAccess) throws IOException, RepositoryException {
        //String url = k4url + "?action=TEXT&content=" + URLEncoder.encode(ds, "UTF8");
        if (fedoraAccess.isStreamAvailable(pid, streamName)) {
            Repository repo = fedoraAccess.getInternalAPI();
            repo.getObject(pid).deleteStream(streamName);
            repo.getObject(pid).createStream(streamName, mimeType, new ByteArrayInputStream(data));
        } else {
            Repository repo = fedoraAccess.getInternalAPI();
            repo.getObject(pid).createStream(streamName, mimeType, new ByteArrayInputStream(data));
        }

    }

    public static void modifyLangDatastream(String pid, String lang, String ds, FedoraAccess fedoraAccess) throws IOException, RepositoryException {
        String dsName = VirtualCollectionsManager.TEXT_DS_PREFIX + lang;
        modifyLangDatastream(pid, lang, dsName, ds, fedoraAccess);
    }

    public static void modifyLangDatastream(String pid, String lang,String dsName, String ds, FedoraAccess fedoraAccess) throws IOException, RepositoryException {
        byte[] bytes = ds.getBytes("UTF-8");
        modifyDatastream(pid, dsName, "text/plain",bytes, fedoraAccess);
    }

    public static void modifyTexts(String pid, FedoraAccess fedoraAccess, Map<String, String> textsMap) throws IOException, RepositoryException {
    
        String texts = "<texts>";
        for (String lang : textsMap.keySet()) {
            String text = textsMap.get(lang);
            if (text != null) {
                texts += String.format("<text language=\"%s\">%s</text>", lang, text, lang);
            }
        }
        texts += "</texts>";

        final byte[] data = texts.getBytes(Charset.forName("UTF-8"));
        if (fedoraAccess.isStreamAvailable(pid, "TEXT")) {
            Repository repo = fedoraAccess.getInternalAPI();
            repo.getObject(pid).deleteStream("TEXT");
            repo.getObject(pid).createStream("TEXT", "text/plain", new ByteArrayInputStream(data));
        } else {
            Repository repo = fedoraAccess.getInternalAPI();
            repo.getObject(pid).createStream("TEXT", "text/plain", new ByteArrayInputStream(data));
        }
    }

    public static boolean isInCollection(String pid, String collection, final FedoraAccess fedoraAccess) throws IOException, RepositoryException {
        return fedoraAccess.getInternalAPI().getObject(pid).relationExists("isMemberOfCollection", FedoraNamespaces.RDF_NAMESPACE_URI,collection);
    }

    public static void addPidToCollection(String pid, String collection, final FedoraAccess fedoraAccess) throws IOException {
        final String predicate = FedoraNamespaces.RDF_NAMESPACE_URI + "isMemberOfCollection";
        final String fedoraColl = collection.startsWith("info:fedora/") ? collection : "info:fedora/" + collection;
        try {
            fedoraAccess.getInternalAPI().getObject(pid).addRelation("rdf:isMemberOfCollection", FedoraNamespaces.RDF_NAMESPACE_URI,  collection);
            LOGGER.log(Level.INFO, "{0} added to collection {1}", new Object[]{pid, fedoraColl});
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static void addToCollection(String pid, String collection, final FedoraAccess fedoraAccess) throws IOException {
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
                        Repository repo = fedoraAccess.getInternalAPI();
                        if (!repo.getObject(pid).relationExists("isMemberOfCollection",FedoraNamespaces.RDF_NAMESPACE_URI,  collection)) {
                            repo.getObject(pid).addRelation("rdf:isMemberOfCollection", FedoraNamespaces.RDF_NAMESPACE_URI,  collection);
                        }
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

                        Repository repo = fedoraAccess.getInternalAPI();
                        repo.getObject(pid).removeRelation("isMemberOfCollection", FedoraNamespaces.RDF_NAMESPACE_URI,  collection);

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
            ProcessUtils.httpGet(url);
        } catch (Exception e) {
            LOGGER.severe("Error starting indexer for " + pid + ":" + e);
        }
    }

//    public static void removeCollections(String pid, final FedoraAccess fedoraAccess) throws Exception {
//        final String predicate = FedoraNamespaces.RDF_NAMESPACE_URI + "isMemberOfCollection";
//        try {
//            fedoraAccess.processSubtree(pid, new TreeNodeProcessor() {
//                boolean breakProcess = false;
//                int previousLevel = 0;
//
//                @Override
//                public boolean breakProcessing(String pid, int level) {
//                    return breakProcess;
//                }
//
//                @Override
//                public void process(String pid, int level) throws ProcessSubtreeException {
//                    try {
//                        String fedoraPid = pid.startsWith("info:fedora/") ? pid : "info:fedora/" + pid;
//                        fedoraAccess.getAPIM().purgeRelationship(fedoraPid, predicate, null, true, null);
//                    } catch (Exception e) {
//                        throw new ProcessSubtreeException(e);
//                    }
//                }
//
//                @Override
//                public boolean skipBranch(String pid, int level) {
//                    return false;
//                }
//            });
//        } catch (ProcessSubtreeException e) {
//            throw new IOException(e);
//        }
//    }

    
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
