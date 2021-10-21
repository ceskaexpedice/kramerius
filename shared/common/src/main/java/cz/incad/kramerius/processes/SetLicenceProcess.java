package cz.incad.kramerius.processes;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.processes.new_api.IndexationScheduler;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SetLicenceProcess {

    public enum Action {
        ADD, REMOVE
    }

    public static final Logger LOGGER = Logger.getLogger(SetLicenceProcess.class.getName());

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
        String licence = args[argsIndex++];
        String target = args[argsIndex++];

        switch (action) {
            case ADD:
                ProcessStarter.updateName(String.format("Přidání licence '%s' pro %s", licence, target));
                for (String pid : extractPids(target)) {
                    addLicence(licence, pid);
                }
                break;
            case REMOVE:
                ProcessStarter.updateName(String.format("Odebrání licence '%s' pro %s", licence, target));
                for (String pid : extractPids(target)) {
                    removeLicence(licence, pid);
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
            //pidlist.directory will contain simple text files (each line containing only pid) for batch manipulations - like adding/removing licence here, setting public/private with SetPolicyProcess, adding to collection etc.
            throw new RuntimeException("target pidlist_file not supported yet");
        } else {
            throw new RuntimeException("invalid target " + target);
        }
    }

    private static void addLicence(String licence, String pid) {
        System.out.println(String.format("todo: add licence %s to %s", licence, pid));
    }

    private static void removeLicence(String licence, String pid) {
        System.out.println(String.format("todo: remove licence %s from %s", licence, pid));
    }
}
