package cz.incad.kramerius;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.impl.SolrAccessImplNewIndex;
import cz.incad.kramerius.processes.new_api.IndexationScheduler;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.KrameriusRepositoryApiImpl;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.Dom4jUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.kramerius.searchIndex.indexer.SolrConfig;
import cz.kramerius.searchIndex.indexer.SolrIndexAccess;
import org.apache.solr.client.solrj.SolrServerException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Prepsana logika z ParametrizedLabelSetDNNTFlag a ParametrizedLabelUnsetDNNTFlag, bez zpracovani CSV
 *
 * @see cz.incad.kramerius.workers.DNNTLabelWorker
 */
public class SetLicenseProcess {
    public enum Action {
        ADD, REMOVE
    }

    private static final Logger LOGGER = Logger.getLogger(SetLicenseProcess.class.getName());

    private static String RELS_EXT_RELATION_LICENSE = "license";
    private static String RELS_EXT_RELATION_CONTAINS_LICENSE = "containsLicense";
    private static String[] RELS_EXT_RELATION_LICENSE_DEPRECATED = new String[]{
            "licenses",
            "licence", "licences",
            "dnnt-label", "dnnt-labels"
    };
    private static String[] RELS_EXT_RELATION_CONTAINS_LICENSE_DEPRECATED = new String[]{
            "containsLicenses",
            "containsLicence", "containsLicences",
            "contains-license", "contains-licenses",
            "contains-licence", "contains-licenses",
            "contains-dnnt-label", "contains-dnnt-labels",
    };

    private static String SOLR_FIELD_LICENSES = "licenses";
    private static String SOLR_FIELD_CONTAINS_LICENSES = "contains_licenses";


    /**
     * args[0] - target (pid:uuid:123, or pidlist:uuid:123;uuid:345;uuid:789, or pidlist_file:/home/kramerius/.kramerius/import-dnnt/grafiky.txt
     * In case of pidlist pids must be separated with ';'. Convenient separator ',' won't work due to way how params are stored in database and transfered to process.
     * <p>
     * args[1] - licence ('dnnt', 'dnnto', 'public_domain', etc.)
     */
    public static void main(String[] args) throws IOException, SolrServerException, RepositoryException {

        //args
        /*LOGGER.info("args: " + Arrays.asList(args));
        for (String arg : args) {
            System.out.println(arg);
        }*/

        int argsIndex = 0;
        //params from lp.st
        Action action = Action.valueOf(args[argsIndex++]);
        //auth
        IndexationScheduler.ProcessCredentials credentials = new IndexationScheduler.ProcessCredentials();
        //token for keeping possible following processes in same batch
        credentials.authToken = args[argsIndex++]; //auth token always first, but still suboptimal solution, best would be if it was outside the scope of this as if ProcessHelper.scheduleProcess() similarly to changing name (ProcessStarter)
        //Kramerius
        credentials.krameriusApiAuthClient = args[argsIndex++];
        credentials.krameriusApiAuthUid = args[argsIndex++];
        credentials.krameriusApiAuthAccessToken = args[argsIndex++];
        //process params
        String license = args[argsIndex++];
        String target = args[argsIndex++];

        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        KrameriusRepositoryApi repository = injector.getInstance(Key.get(KrameriusRepositoryApiImpl.class)); //FIXME: hardcoded implementation
        SolrAccess searchIndex = injector.getInstance(Key.get(SolrAccessImplNewIndex.class)); //FIXME: hardcoded implementation
        SolrIndexAccess indexerAccess = new SolrIndexAccess(new SolrConfig(KConfiguration.getInstance()));

        switch (action) {
            case ADD:
                ProcessStarter.updateName(String.format("Přidání licence '%s' pro %s", license, target));
                for (String pid : extractPids(target)) {
                    addLicense(license, pid, repository, searchIndex, indexerAccess);
                }
                break;
            case REMOVE:
                ProcessStarter.updateName(String.format("Odebrání licence '%s' pro %s", license, target));
                for (String pid : extractPids(target)) {
                    removeLicense(license, pid, repository, searchIndex, indexerAccess);
                }
                break;
        }
    }

    private static List<String> extractPids(String target) {
        if (target.startsWith("pid:")) {
            String pid = target.substring("pid:".length());
            List<String> result = new ArrayList<>();
            result.add(pid);
            return result;
        } else if (target.startsWith("pidlist:")) {
            List<String> pids = Arrays.stream(target.substring("pidlist:".length()).split(";")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            return pids;
        } else if (target.startsWith("pidlist_file:")) {
            //TODO: implement parsing PIDS from text file, also in ProcessResource change root dir from KConfiguration.getInstance().getProperty("convert.directory") to new location like pidlist.directory
            //pidlist.directory will contain simple text files (each line containing only pid) for batch manipulations - like adding/removing license here, setting public/private with SetPolicyProcess, adding to collection etc.
            throw new RuntimeException("target pidlist_file not supported yet");
        } else {
            throw new RuntimeException("invalid target " + target);
        }
    }

    private static void addLicense(String license, String targetPid, KrameriusRepositoryApi repository, SolrAccess searchIndex, SolrIndexAccess indexerAccess) throws RepositoryException, IOException {
        LOGGER.info(String.format("Adding license '%s' to %s", license, targetPid));

        //1. Do rels-ext ciloveho objektu se doplni license=L, pokud uz tam neni. Nejprve se ale normalizuji stare zapisy licenci (dnnt-label=L => license=L)
        LOGGER.info("updating RELS-EXT record of the target object " + targetPid);
        addRelsExtRelationAfterNormalization(targetPid, RELS_EXT_RELATION_LICENSE, RELS_EXT_RELATION_LICENSE_DEPRECATED, license, repository);

        //2. Do rels-ext predku se doplni containsLicense=L, pokud uz tam neni
        LOGGER.info("updating RELS-EXT record of all the ancestors of the target object " + targetPid);
        List<String> pidsOfAncestors = getPidsOfAllAncestors(targetPid, searchIndex);
        for (String ancestorPid : pidsOfAncestors) {
            addRelsExtRelationAfterNormalization(ancestorPid, RELS_EXT_RELATION_CONTAINS_LICENSE, RELS_EXT_RELATION_CONTAINS_LICENSE_DEPRECATED, license, repository);
        }

        //3. Aktualizuje se index predku atomic updatem (prida se contains_licenses=L)
        LOGGER.info("updating search index for all the ancestors");
        if (!pidsOfAncestors.isEmpty()) {
            indexerAccess.addSingleFieldValueForMultipleObjects(pidsOfAncestors, SOLR_FIELD_CONTAINS_LICENSES, license, false);
        }

        //4. Aktualizuje se index ciloveho objektu a vsech potomku atomic updaty (prida se licenses=L) po davkach (muzou to byt az stovky tisic objektu)
        LOGGER.info("updating search index for the target object and all it's descendants");
        PidsOfDescendantsProducer iterator = new PidsOfDescendantsProducer(targetPid, searchIndex);
        while (iterator.hasNext()) {
            List<String> pids = iterator.next();
            boolean explicitCommit = false;
            if (!iterator.hasNext()) {
                explicitCommit = true;
                pids.add(targetPid);//don't forget target
            }
            indexerAccess.addSingleFieldValueForMultipleObjects(pids, SOLR_FIELD_LICENSES, license, explicitCommit);
            LOGGER.info(String.format("Indexed: %d/%d", iterator.getReturned(), iterator.getTotal()));
        }
    }

    private static List<String> getPidsOfAllAncestors(String targetPid, SolrAccess searchIndex) throws IOException {
        Set<String> result = new HashSet<>();
        ObjectPidsPath[] pidPaths = searchIndex.getPidPaths(targetPid);
        for (ObjectPidsPath pidPath : pidPaths) {
            String[] pathFromRootToLeaf = pidPath.getPathFromRootToLeaf();
            for (String pathPid : pathFromRootToLeaf) {
                if (!targetPid.equals(pathPid)) {
                    result.add(pathPid);
                }
            }
        }
        return new ArrayList<>(result);
    }

    private static boolean addRelsExtRelationAfterNormalization(String pid, String relationName, String[] wrongRelationNames, String value, KrameriusRepositoryApi repository) throws RepositoryException, IOException {
        if (!repository.isRelsExtAvailable(pid)) {
            throw new RepositoryException("RDF record (datastream RELS-EXT) not found for " + pid);
        }
        Document relsExt = repository.getRelsExt(pid, true);
        Element rootEl = (Element) Dom4jUtils.buildXpath("/rdf:RDF/rdf:Description").selectSingleNode(relsExt);
        boolean relsExtNeedsToBeUpdated = false;

        //normalize relations with deprecated/incorrect name, possibly including relation we want to add
        relsExtNeedsToBeUpdated |= normalizeIncorrectRelationNotation(wrongRelationNames, relationName, rootEl, pid);

        //add new relation if not there already
        List<Node> relationEls = Dom4jUtils.buildXpath("rel:" + relationName).selectNodes(rootEl);
        boolean relationFound = false;
        for (Node relationEl : relationEls) {
            String content = relationEl.getText();
            if (content.equals(value)) {
                relationFound = true;
            }
        }
        if (!relationFound) {
            Element newRelationEl = rootEl.addElement(relationName, Dom4jUtils.getNamespaceUri("rel"));
            newRelationEl.addText(value);
            relsExtNeedsToBeUpdated = true;
            LOGGER.info(String.format("adding relation '%s' into RELS-EXT of %s", relationName, pid));
        } else {
            LOGGER.info(String.format("relation '%s' already found in RELS-EXT of %s", relationName, pid));
        }

        //update RELS-EXT in repository if there was a change
        if (relsExtNeedsToBeUpdated) {
            //System.out.println(Dom4jUtils.docToPrettyString(relsExt));
            repository.updateRelsExt(pid, relsExt);
            LOGGER.info(String.format("RELS-EXT of %s has been updated", pid));
        }
        return relsExtNeedsToBeUpdated;
    }

    private static void removeLicense(String license, String targetPid, KrameriusRepositoryApi repository, SolrAccess searchIndex, SolrIndexAccess indexerAccess) throws RepositoryException, IOException {
        LOGGER.info(String.format("Removing license '%s' from %s", license, targetPid));

        //1. Z rels-ext ciloveho objektu se odeber license=L, pokud tam je. Nejprve se ale normalizuji stare zapisy licenci (dnnt-label=L => license=L)
        LOGGER.info("updating RELS-EXT record of the target object " + targetPid);
        removeRelsExtRelationAfterNormalization(targetPid, RELS_EXT_RELATION_LICENSE, RELS_EXT_RELATION_LICENSE_DEPRECATED, license, repository);

        //2. Z rels-ext predku se odebere containsLicence=L, pokud tam je. A pokud neexistuje jiny zdroj pro licenci (objekt ze stromu predka, ktery neni v stromu cilove objektu)
        LOGGER.info("updating RELS-EXT record of all the ancestors of the target object " + targetPid);
        List<String> pidsOfRelevantAncestors = getPidsOfAllAncestorsThatDontHaveLicenceFromDifferentDescendant(targetPid, searchIndex, license);
        for (String ancestorPid : pidsOfRelevantAncestors) {
            removeRelsExtRelationAfterNormalization(ancestorPid, RELS_EXT_RELATION_CONTAINS_LICENSE, RELS_EXT_RELATION_CONTAINS_LICENSE_DEPRECATED, license, repository);
        }

        //3. Aktualizuje se index (relevantnich) predku atomic updatem (odebere se contains_licenses=L)
        LOGGER.info("updating search index for all the ancestors");
        if (!pidsOfRelevantAncestors.isEmpty()) {
            indexerAccess.removeSingleFieldValueFromMultipleObjects(pidsOfRelevantAncestors, SOLR_FIELD_CONTAINS_LICENSES, license, false);
        }

        //4. Aktualizuje se index ciloveho objektu a vsech potomku atomic updaty (odebere se licenses=L) po davkach (muzou to byt az stovky tisic objektu)
        LOGGER.info("updating search index for the target object and all it's descendants");
        PidsOfDescendantsProducer iterator = new PidsOfDescendantsProducer(targetPid, searchIndex);
        while (iterator.hasNext()) {
            List<String> pids = iterator.next();
            boolean explicitCommit = false;
            if (!iterator.hasNext()) {
                explicitCommit = true;
                pids.add(targetPid);//don't forget target
            }
            indexerAccess.removeSingleFieldValueFromMultipleObjects(pids, SOLR_FIELD_LICENSES, license, explicitCommit);
            LOGGER.info(String.format("Indexed: %d/%d", iterator.getReturned(), iterator.getTotal()));
        }
    }

    private static List<String> getPidsOfAllAncestorsThatDontHaveLicenceFromDifferentDescendant(String targetPid, SolrAccess searchIndex, String license) throws IOException {
        List<String> ancestorsAll = getPidsOfAllAncestors(targetPid, searchIndex);
        List<String> ancestorsWithoutLicenceFromAnotherDescendant = new ArrayList<>();
        for (String ancestorPid : ancestorsAll) {
            //hledaji se objekty, jejichz pid_path obsahuje ancestorPid, ale ne targetPid. Takze jiny zdroj stejne licence nekde ve strome ancestra, mimo strom od targeta
            //pokud nejsou, priznak containsLicense/contains_licenses muze byt z ancestra odstranen
            //jedine kdyby byl treba rocnik R1, co ma v rels-ext containsLicense=L a obsahuje nektere issue Ix, coma ma taky containsLicense=L, tak to se pri odstranovani L z R1 nedetekuje (spatne)
            //tim padem bude nepravem odebrano containsLicense/contains_licenses predkum R1, prestoze by na to meli narok kvuli Ix
            String ancestorPidEscaped = ancestorPid.replace(":", "\\:");
            String targetPidEscaped = targetPid.replace(":", "\\:");
            String q = String.format(
                    "licenses:%s AND (pid_paths:%s/* OR pid_paths:*/%s/*) AND -pid:%s AND -pid_paths:%s/* AND -pid_paths:*/%s/*",
                    license, ancestorPidEscaped, ancestorPidEscaped, targetPidEscaped, targetPidEscaped, targetPidEscaped);
            String query = "fl=pid&rows=0&q=" + URLEncoder.encode(q, "UTF-8");
            JSONObject jsonObject = searchIndex.requestWithSelectReturningJson(query);
            JSONObject response = jsonObject.getJSONObject("response");
            if (response.getInt("numFound") == 0) {
                ancestorsWithoutLicenceFromAnotherDescendant.add(ancestorPid);
            }
        }
        return ancestorsWithoutLicenceFromAnotherDescendant;
    }

    private static boolean removeRelsExtRelationAfterNormalization(String pid, String relationName, String[] wrongRelationNames, String value, KrameriusRepositoryApi repository) throws RepositoryException, IOException {
        if (!repository.isRelsExtAvailable(pid)) {
            throw new RepositoryException("RDF record (datastream RELS-EXT) not found for " + pid);
        }
        Document relsExt = repository.getRelsExt(pid, true);
        Element rootEl = (Element) Dom4jUtils.buildXpath("/rdf:RDF/rdf:Description").selectSingleNode(relsExt);
        boolean relsExtNeedsToBeUpdated = false;

        //normalize relations with deprecated/incorrect names, possibly including relation we want to remove
        relsExtNeedsToBeUpdated |= normalizeIncorrectRelationNotation(wrongRelationNames, relationName, rootEl, pid);

        //remove relation if found (even multiple times with same licence)
        List<Node> relationEls = Dom4jUtils.buildXpath("rel:" + relationName).selectNodes(rootEl);
        for (Node relationEl : relationEls) {
            String content = relationEl.getText();
            if (content.equals(value)) {
                LOGGER.info(String.format("removing relation '%s' from RELS-EXT of %s", relationName, pid));
                relationEl.detach();
                relsExtNeedsToBeUpdated = true;
            }
        }

        //update RELS-EXT in repository if there was a change
        if (relsExtNeedsToBeUpdated) {
            //System.out.println(Dom4jUtils.docToPrettyString(relsExt));
            repository.updateRelsExt(pid, relsExt);
            LOGGER.info(String.format("RELS-EXT of %s has been updated", pid));
        }
        return relsExtNeedsToBeUpdated;
    }

    /*
    Normalizuje vazby v nactenem rels-ext. Napr. nahradi vsechny relace dnnt-labels za license. Dalsi zpracovani (pridavani/odebirani) uz ma korektne zapsana data.
     */
    private static boolean normalizeIncorrectRelationNotation(String[] wrongRelationNames, String correctRelationName, Element rootEl, String pid) {
        boolean updated = false;
        for (String wrongRelationName : wrongRelationNames) {
            List<Node> deprecatedRelationEls = Dom4jUtils.buildXpath("rel:" + wrongRelationName).selectNodes(rootEl);
            for (Node relationEl : deprecatedRelationEls) {
                String valueOfRelationBeingFixed = relationEl.getText();
                LOGGER.info(String.format("found incorrect notation (%s) in RELS-EXT of object %s and value '%s', fixing by replacing with %s", wrongRelationName, pid, valueOfRelationBeingFixed, correctRelationName));
                relationEl.detach(); //setName() pracuje spatne s jmenymi prostory - zpusobi duplikaci atributu xmlns
                Element newRelationEl = rootEl.addElement(correctRelationName, Dom4jUtils.getNamespaceUri("rel"));
                newRelationEl.addText(valueOfRelationBeingFixed);
                updated = true;
            }
        }
        return updated;
    }

    /**
     * Vraci PIDy všech potomku v davkach
     */
    static class PidsOfDescendantsProducer implements Iterator<List<String>> {
        private static final int BATCH_SIZE = 1000;

        private final SolrAccess searchIndex;
        private final String q;
        private String cursorMark = null;
        private String nextCursorMark = "*";
        private int total = 0;
        private int returned = 0;

        public PidsOfDescendantsProducer(String targetPid, SolrAccess searchIndex) {
            this.searchIndex = searchIndex;
            String pidEscaped = targetPid.replace(":", "\\:");
            this.q = String.format("pid_paths:%s/* OR pid_paths:*/%s/* ", pidEscaped, pidEscaped);
        }

        public int getTotal() {
            return total;
        }

        public int getReturned() {
            return returned;
        }

        @Override
        public boolean hasNext() {
            if (total > 0 && returned == total) {
                return false;
            } else {//obrana proti zacykleni, kdyby se solr zachoval divne a nektery slibeny objekt nevratil
                return !nextCursorMark.equals(cursorMark);
            }
        }

        @Override
        public List<String> next() {
            try {
                List<String> result = new ArrayList<>();
                cursorMark = nextCursorMark;
                String query = String.format("fl=pid&sort=pid+asc&rows=%d&q=%s&cursorMark=%s", BATCH_SIZE, URLEncoder.encode(q, "UTF-8"), cursorMark);
                JSONObject jsonObject = searchIndex.requestWithSelectReturningJson(query);
                JSONObject response = jsonObject.getJSONObject("response");
                nextCursorMark = jsonObject.getString("nextCursorMark");
                total = response.getInt("numFound");
                if (total != 0) {
                    JSONArray docs = response.getJSONArray("docs");
                    for (int i = 0; i < docs.length(); i++) {
                        String pid = docs.getJSONObject(i).getString("pid");
                        result.add(pid);
                    }
                }
                returned += result.size();
                return result;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
