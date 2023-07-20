package cz.incad.kramerius.processes;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.impl.AkubraDOManager;
import cz.incad.kramerius.processes.new_api.ProcessScheduler;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.KrameriusRepositoryApiImpl;
import cz.incad.kramerius.repository.RepositoryApi;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.Dom4jUtils;
import cz.incad.kramerius.utils.java.Pair;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Deklarace procesu je v shared/common/src/main/java/cz/incad/kramerius/processes/res/lp.st (set_policy)
 * Nahrazuje cz.incad.kramerius.service.impl.PolicyServiceImpl.main()
 */
public class RemovePolicyProcess {

    private static final String PIDLIST_FILE_PREFIX = "pidlist_file:";
    
    public static final Logger LOGGER = Logger.getLogger(RemovePolicyProcess.class.getName());

    /**
     * args[0] - authToken
     * args[1] - scope (OBJECT/TREE)
     * args[2] - policy (PRIVATE/PUBLIC)
     * args[3] - pid of root object, for example "uuid:df693396-9d3f-4b3b-bf27-3be0aaa2aadf"
     * args[4-...] - optional title of the root object
     */
    public static void main(String[] args) throws IOException, SolrServerException, RepositoryException {
        LOGGER.info("Process parameters "+Arrays.asList(args));
        //args
        /*LOGGER.info("args: " + Arrays.asList(args));
        for (String arg : args) {
            System.out.println(arg);
        }*/
        if (args.length < 4) {
            throw new RuntimeException("Not enough arguments.");
        }
        int argsIndex = 0;
        //token for keeping possible following processes in same batch
        String authToken = args[argsIndex++]; //auth token always first, but still suboptimal solution, best would be if it was outside the scope of this as if ProcessHelper.scheduleProcess() similarly to changing name (ProcessStarter)
        //process params
        Scope scope = Scope.valueOf(args[argsIndex++]);
        //Policy policy = Policy.valueOf(args[argsIndex++]);
        String pidArg = args[argsIndex++];
        // TODO: Support pidlist 
        
        List<String> pids = extractPids(pidArg);

        String title = shortenIfTooLong(mergeArraysEnd(args, argsIndex), 256);
        String scopeDesc = scope == Scope.OBJECT ? "jen objekt" : "objekt včetně potomků";
        if (pidArg.startsWith("pidlist_file")) {
            ProcessStarter.updateName(title != null
                    ? String.format("Odebrání příznaku viditelnosti %s (%s,  %s)", title, pidArg.substring(PIDLIST_FILE_PREFIX.length()),  scopeDesc)
                    : String.format("Odebrání příznaku viditelnosti %s (%s)", pidArg.substring(PIDLIST_FILE_PREFIX.length()), scopeDesc)
            );
            
        } else {
            ProcessStarter.updateName(title != null
                    ? String.format("Odebrání příznaku viditelnosti %s (%s,  %s)", title, shortenIfTooLong(pids.toString(),256),  scopeDesc)
                    : String.format("Odebrání příznaku viditelnosti %s (%s)", shortenIfTooLong(pids.toString(),256), scopeDesc)
            );
            
        }
        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        KrameriusRepositoryApi repository = injector.getInstance(Key.get(KrameriusRepositoryApiImpl.class)); //FIXME: hardcoded implementation
        List<Boolean> errors = new ArrayList<>();
        for (String pid : pids) {
            //check object exists in repository
            if (!repository.getLowLevelApi().objectExists(pid)) {
                throw new RuntimeException(String.format("object %s not found in repository", pid));
            }
            //boolean includingDescendants = scope == Scope.TREE;
            boolean noErrors = removePolicy(pid, true, repository);
            LOGGER.info(String.format("Remove policy %b for object %s", noErrors, pid));
            errors.add(noErrors);
        }
        if (pidArg != null && pidArg.startsWith(PIDLIST_FILE_PREFIX)) {
            ProcessScheduler.scheduleIndexation(new File(pidArg.substring(PIDLIST_FILE_PREFIX.length())), title, true, authToken);
            
        } else {
            ProcessScheduler.scheduleIndexation(pids, title, true, authToken);
        }
        
        Optional<Boolean> findAny = errors.stream().filter(b-> {return !b; }).findAny();
        
        if (findAny.isPresent()) {
            throw new WarningException("failed to set policy for some objects");
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
        } else if (target.startsWith(PIDLIST_FILE_PREFIX)) {
            String filePath  = target.substring(PIDLIST_FILE_PREFIX.length());
            File file = new File(filePath);
            if (file.exists()) {
                try {
                    return IOUtils.readLines(new FileInputStream(file), Charset.forName("UTF-8"));
                } catch (IOException e) {
                    throw new RuntimeException("IOException " + e.getMessage());
                }
            } else {
                throw new RuntimeException("file " + file.getAbsolutePath()+" doesnt exist ");
            }
        } else {
            // expecting list of pids tokenized by ;
            List<String> tokens = new ArrayList<>();
            StringTokenizer tokenizer = new StringTokenizer(target,";");
            while(tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                tokens.add(token);
            }
            return tokens;
        }
        
    }

    /**
     * @return true if all objects where processed without problems, false otherwise
     */
    private static boolean removePolicy(String pid, boolean includingDescendants, KrameriusRepositoryApi repository) {
        //LOGGER.info(String.format("Setting policy (to %s) for %s", policy, pid));
        Lock writeLock = AkubraDOManager.getWriteLock(pid);
        try {
            removePolicyDC(pid,  repository); //TODO: zatim takto, at nevznikaji zmatky, dokud neni datastream DC uplne odstranen
            removePolicyRELS_EXT(pid,  repository);

            boolean noErros = true;
            if (includingDescendants) {
                Pair<List<RepositoryApi.Triplet>, List<RepositoryApi.Triplet>> children = repository.getChildren(pid);
                if (children.getFirst() != null && !children.getFirst().isEmpty()) {
                    for (RepositoryApi.Triplet triplet : children.getFirst()) {
                        String childPid = triplet.target;
                        noErros &= removePolicy(childPid, includingDescendants, repository);
                    }
                }
            }
            return noErros;
        } catch (Exception ex) {
            LOGGER.warning("Cannot set policy for object " + pid + ", skipping ");
            ex.printStackTrace();
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    private static void removePolicyRELS_EXT(String pid,  KrameriusRepositoryApi repository) throws RepositoryException, IOException {
        if (!repository.isRelsExtAvailable(pid)) {
            throw new RepositoryException("RDF record (datastream RELS-EXT) not found for " + pid);
        }
        Document relsExt = repository.getRelsExt(pid, true);
        Element rootEl = (Element) Dom4jUtils.buildXpath("/rdf:RDF/rdf:Description").selectSingleNode(relsExt);
        List<Node> policyEls = Dom4jUtils.buildXpath("rel:policy").selectNodes(rootEl);
        for (Node policyEl : policyEls) {
            String content = policyEl.getText();
            if (content.startsWith("policy:")) {
                policyEl.detach();
            }
        }
        repository.updateRelsExt(pid, relsExt);
    }

    private static void removePolicyDC(String pid,  KrameriusRepositoryApi repository) throws RepositoryException, IOException {
        if (!repository.isDublinCoreAvailable(pid)) {
            LOGGER.info("Dublin Core record (datastream DC) not found for " + pid);
            return;
        }
        Document dc = repository.getDublinCore(pid, true);
        Element rootEl = (Element) Dom4jUtils.buildXpath("//oai_dc:dc").selectSingleNode(dc);
        List<Node> policyEls = Dom4jUtils.buildXpath("dc:rights").selectNodes(rootEl);
        for (Node policyEl : policyEls) { //da se cekat, ze budou v datech duplikovne informace
            String content = policyEl.getText();
            if (content.startsWith("policy:")) {
                policyEl.detach();
            }
        }
        repository.updateDublinCore(pid, dc);
    }

    //FIXME: duplicate code (same method in NewIndexerProcessIndexObject, SetPolicyProcess), use abstract/utility class, but not before bigger cleanup in process scheduling
    //["Quartet A minor", " op. 51", " no. 2. Andante moderato"] => "Quartet A minor, op. 51, no. 2 Andante moderato"
    private static String mergeArraysEnd(String[] args, int argsIndex) {
        String result = "";
        for (int i = argsIndex; i < args.length; i++) {
            String arg = args[i];
            if (arg != null && !"null".equals(arg)) {
                result += args[i];
                if (i != args.length - 1) {
                    result += ",";
                }
            }
        }
        result = result.trim();
        return result.isEmpty() ? null : result;
    }

    //FIXME: duplicate code (same method in NewIndexerProcessIndexObject, SetPolicyProcess), use abstract/utility class, but not before bigger cleanup in process scheduling
    private static String shortenIfTooLong(String string, int maxLength) {
        if (string == null || string.isEmpty() || string.length() <= maxLength) {
            return string;
        } else {
            String suffix = "...";
            return string.substring(0, maxLength - suffix.length()) + suffix;
        }
    }

    public enum Scope {
        OBJECT, TREE
    }

    public enum Policy {
        PRIVATE, PUBLIC
    }
}
